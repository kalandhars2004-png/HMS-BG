package com.phegondev.InventoryManagementSystem.backup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.phegondev.InventoryManagementSystem.branch.Branch;
import com.phegondev.InventoryManagementSystem.branch.BranchRepository;
import com.phegondev.InventoryManagementSystem.user.User;
import com.phegondev.InventoryManagementSystem.user.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.EntityType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class BackupServiceImpl implements BackupService {

    private static final String DRIVE_UPLOAD_URL = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart";
    private static final String DRIVE_FILES_URL = "https://www.googleapis.com/drive/v3/files";
    private static final String DRIVE_TOKEN_URL = "https://oauth2.googleapis.com/token";

    private final BackupRepository backupRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final BackupProperties properties;
    private final ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    private final HttpClient http = HttpClient.newHttpClient();

    @Override
    @Transactional
    public BackupDTO createBackup(Long branchId) {
        User current = currentUser();
        List<Branch> branches = branchId != null
                ? branchRepository.findAll().stream().filter(b -> b.getId().equals(branchId)).toList()
                : branchRepository.findAll();
        String label = branchId != null
                ? (branches.isEmpty() ? "Branch " + branchId : branches.get(0).getName())
                : "All branches";

        Backup backup = Backup.builder()
                .branchId(branchId)
                .label(label)
                .status("RUNNING")
                .progressPct(0)
                .createdBy(current != null ? current.getId() : null)
                .build();
        backup = backupRepository.save(backup);
        final Long backupId = backup.getId();

        runBackupAsync(backupId);

        return toDTO(backup);
    }

    @Async
    public void runBackupAsync(Long backupId) {
        try {
            Backup backup = backupRepository.findById(backupId).orElse(null);
            if (backup == null) return;
            setProgress(backup, 5);

            // 1. Relational dump — every @Entity table read with native SQL and emitted
            //    as flat rows of {column:value}. FKs (branch_id, product_id, ...) are the
            //    relationships, so cross-branch integrity is preserved. Using raw values
            //    (not Hibernate object graphs) avoids lazy-loading and circular refs.
            List<EntityType<?>> entities = new ArrayList<>(entityManager.getMetamodel().getEntities());
            Map<String, List<Map<String, Object>>> tables = new LinkedHashMap<>();
            int total = entities.size();
            int done = 0;
            for (EntityType<?> et : entities) {
                String name = et.getName();
                done++;
                try {
                    jakarta.persistence.Tuple tuple = (jakarta.persistence.Tuple) entityManager
                            .createNativeQuery("select * from \"" + name + "\"", jakarta.persistence.Tuple.class)
                            .getResultList();
                    List<Map<String, Object>> rows = new ArrayList<>();
                    for (jakarta.persistence.Tuple element : tuple) {
                        if (element != null) {
                            rows.add(elementToMap(element));
                        }
                    }
                    tables.put(name, rows);
                } catch (Exception ex) {
                    log.debug("Skip table {}: {}", name, ex.getMessage());
                }
                setProgress(backup, (int) (5 + 70.0 * ((double) done / total)));
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("app", "InventoryManagementSystem");
            payload.put("backupId", backupId);
            payload.put("scope", backup.getBranchId() == null ? "all" : "branch-" + backup.getBranchId());
            payload.put("createdAt", LocalDateTime.now().toString());
            payload.put("tables", tables);

            String json = objectMapper.writeValueAsString(payload);

            // 2. Write gzipped JSON.
            Path dir = Path.of(properties.getDir()).toAbsolutePath();
            Files.createDirectories(dir);
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String scope = backup.getBranchId() == null ? "all-branches" : "branch-" + backup.getBranchId();
            String fileName = "backup-" + scope + "-" + stamp + ".json.gz";
            Path file = dir.resolve(fileName);
            try (GZIPOutputStream gz = new GZIPOutputStream(Files.newOutputStream(file));
                 OutputStreamWriter w = new OutputStreamWriter(gz, StandardCharsets.UTF_8)) {
                w.write(json);
            }

            long size = Files.size(file);
            backup.setFileName(fileName);
            backup.setSizeBytes(size);
            backup.setDetail("Stored locally");
            setProgress(backup, 85);

            // 3. Google Drive upload when configured.
            if (properties.driveConfigured()) {
                try {
                    String url = uploadToDrive(file, "backup-" + scope + "-" + stamp + ".json.gz");
                    backup.setDriveUrl(url);
                    backup.setDetail("Stored in Google Drive");
                } catch (Exception ex) {
                    log.error("Google Drive upload failed", ex);
                    backup.setDetail("Local only — Drive upload failed: " + ex.getMessage());
                }
            }

            backup.setStatus("COMPLETED");
            backup.setProgressPct(100);
            backup.setCompletedAt(LocalDateTime.now());
            backupRepository.save(backup);
        } catch (Exception ex) {
            log.error("Backup failed", ex);
            Backup b = backupRepository.findById(backupId).orElse(null);
            if (b != null) {
                b.setStatus("FAILED");
                b.setProgressPct(100);
                b.setCompletedAt(LocalDateTime.now());
                b.setDetail("Failed: " + ex.getMessage());
                backupRepository.save(b);
            }
        }
    }

    /** Automatic nightly backup of all branches (00:15 server time). */
    @Scheduled(cron = "0 15 0 * * *")
    public void nightlyBackup() {
        try {
            createBackup(null);
            log.info("Nightly all-branches backup triggered");
        } catch (Exception e) {
            log.error("Nightly backup failed", e);
        }
    }

    private void setProgress(Backup b, int pct) {
        b.setProgressPct(pct);
        backupRepository.save(b);
    }

    private Map<String, Object> elementToMap(jakarta.persistence.Tuple tuple) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (jakarta.persistence.TupleElement<?> el : tuple.getElements()) {
            String alias = el.getAlias();
            if (alias == null || alias.isBlank()) continue;
            Object v = tuple.get(alias);
            map.put(alias, v);
        }
        return map;
    }

    // ─── Google Drive (java.net.http only, no extra deps) ───

    private PrivateKey parsePrivateKey(String pkcs8) throws Exception {
        String base64 = pkcs8
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64));
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private String googleAccessToken() throws Exception {
        String email = properties.getGoogleClientEmail();
        long now = Instant.now().getEpochSecond();
        String assertion = Jwts.builder()
                .setHeaderParam("alg", "RS256")
                .setHeaderParam("typ", "JWT")
                .setIssuer(email)
                .setSubject(email)
                .setAudience(DRIVE_TOKEN_URL)
                .setIssuedAt(new java.util.Date(now * 1000))
                .setExpiration(new java.util.Date((now + 3600) * 1000))
                .claim("scope", "https://www.googleapis.com/auth/drive.file")
                .signWith(parsePrivateKey(properties.getGooglePrivateKey()), SignatureAlgorithm.RS256)
                .compact();

        String body = "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion="
                + URLEncoder.encode(assertion, StandardCharsets.UTF_8);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(DRIVE_TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IllegalStateException("Google OAuth failed: " + resp.statusCode() + " " + resp.body());
        }
        Map<String, Object> parsed = objectMapper.readValue(resp.body(), new TypeReference<Map<String, Object>>() {});
        return String.valueOf(parsed.get("access_token"));
    }

    private String findOrCreateDriveFolder(String token, String name) throws Exception {
        String q = "name='" + name.replace("'", "\\'") + "' and mimeType='application/vnd.google-apps.folder' and trashed=false";
        HttpRequest find = HttpRequest.newBuilder()
                .uri(URI.create(DRIVE_FILES_URL + "?q=" + URLEncoder.encode(q, StandardCharsets.UTF_8)
                        + "&fields=files(id,name)&spaces=drive"))
                .header("Authorization", "Bearer " + token)
                .GET().build();
        HttpResponse<String> findResp = http.send(find, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> parsed = objectMapper.readValue(findResp.body(), new TypeReference<Map<String, Object>>() {});
        Object filesObj = parsed.get("files");
        if (filesObj instanceof List<?> files && !files.isEmpty()) {
            Map<String, Object> first = (Map<String, Object>) ((List<?>) files).get(0);
            return String.valueOf(first.get("id"));
        }
        // create folder
        String meta = objectMapper.writeValueAsString(Map.of("name", name, "mimeType", "application/vnd.google-apps.folder"));
        HttpRequest create = HttpRequest.newBuilder()
                .uri(URI.create(DRIVE_FILES_URL))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(meta))
                .build();
        HttpResponse<String> createResp = http.send(create, HttpResponse.BodyHandlers.ofString());
        if (createResp.statusCode() != 200) {
            throw new IllegalStateException("Could not create Drive folder: " + createResp.body());
        }
        Map<String, Object> created = objectMapper.readValue(createResp.body(), new TypeReference<Map<String, Object>>() {});
        return String.valueOf(created.get("id"));
    }

    private String uploadToDrive(Path file, String name) throws Exception {
        String token = googleAccessToken();
        String folderId = findOrCreateDriveFolder(token, properties.getDriveFolder());
        String boundary = "----IMS" + Long.toHexString(System.nanoTime());
        byte[] fileBytes = Files.readAllBytes(file);

        String meta = "{\"name\":\"" + name.replace("\"", "\\\"") + "\",\"parents\":[\"" + folderId + "\"]}";

        ByteArrayJava body = new ByteArrayJava();
        body.append("--" + boundary + "\r\n");
        body.append("Content-Type: application/json; charset=UTF-8\r\n\r\n");
        body.append(meta + "\r\n");
        body.append("--" + boundary + "\r\n");
        body.append("Content-Type: application/gzip\r\n\r\n");
        body.append(fileBytes);
        body.append("\r\n--" + boundary + "--\r\n");

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(DRIVE_UPLOAD_URL))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "multipart/related; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body.toBytes()))
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200 && resp.statusCode() != 201) {
            throw new IllegalStateException("Drive upload failed: " + resp.statusCode() + " " + resp.body());
        }
        Map<String, Object> parsed = objectMapper.readValue(resp.body(), new TypeReference<Map<String, Object>>() {});
        return DRIVE_FILES_URL + "/" + parsed.get("id") + "?usp=drivesdk";
    }

    /** Tiny growable byte buffer for multipart bodies. */
    private static final class ByteArrayJava {
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();
        void append(String s) throws IOException { out.write(s.getBytes(StandardCharsets.UTF_8)); }
        void append(byte[] b) throws IOException { out.write(b); }
        byte[] toBytes() { return out.toByteArray(); }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BackupDTO> getHistory() {
        return backupRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BackupDTO getLatest() {
        List<Backup> list = backupRepository.findTop10ByOrderByCreatedAtDesc();
        return list.isEmpty() ? null : toDTO(list.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public String resolveFileName(Long backupId) {
        return backupRepository.findById(backupId).map(Backup::getFileName).orElse(null);
    }

    private User currentUser() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private BackupDTO toDTO(Backup b) {
        return BackupDTO.builder()
                .id(b.getId())
                .branchId(b.getBranchId())
                .fileName(b.getFileName())
                .label(b.getLabel())
                .status(b.getStatus())
                .progressPct(b.getProgressPct())
                .sizeBytes(b.getSizeBytes())
                .detail(b.getDetail())
                .driveUrl(b.getDriveUrl())
                .createdAt(b.getCreatedAt())
                .completedAt(b.getCompletedAt())
                .build();
    }
}
