package com.phegondev.InventoryManagementSystem.backup;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Backup configuration. Backend writes a .json.gz into {@code dir} and, when a
 * Google service account is configured, uploads it to Drive. All values may come
 * from environment variables so no secrets are committed.
 */
@Configuration
@ConfigurationProperties(prefix = "app.backup")
public class BackupProperties {

    /** Directory where backup files are written (local). */
    private String dir = "backups";

    /** Google Drive upload folder name ("My Drive" root when blank). */
    private String driveFolder = "IMS-Backups";

    /** Content of a Google service-account JSON key, or the file path to it. */
    private String googleServiceAccount = "";

    /** Email of the Google service account. */
    private String googleClientEmail = "";

    /** Private key of the Google service account (used with client email). */
    private String googlePrivateKey = "";

    /** Upload to Drive only when this is "true". */
    private boolean driveEnabled = false;

    public String getDir() { return dir; }
    public void setDir(String dir) { this.dir = dir; }
    public String getDriveFolder() { return driveFolder; }
    public void setDriveFolder(String driveFolder) { this.driveFolder = driveFolder; }
    public String getGoogleServiceAccount() { return googleServiceAccount; }
    public void setGoogleServiceAccount(String googleServiceAccount) { this.googleServiceAccount = googleServiceAccount; }
    public String getGoogleClientEmail() { return googleClientEmail; }
    public void setGoogleClientEmail(String googleClientEmail) { this.googleClientEmail = googleClientEmail; }
    public String getGooglePrivateKey() { return googlePrivateKey; }
    public void setGooglePrivateKey(String googlePrivateKey) { this.googlePrivateKey = googlePrivateKey; }
    public boolean isDriveEnabled() { return driveEnabled; }
    public void setDriveEnabled(boolean driveEnabled) { this.driveEnabled = driveEnabled; }

    public boolean driveConfigured() {
        return driveEnabled
                && (googleServiceAccount != null && !googleServiceAccount.isBlank()
                    || (googleClientEmail != null && !googleClientEmail.isBlank()
                        && googlePrivateKey != null && !googlePrivateKey.isBlank()));
    }
}
