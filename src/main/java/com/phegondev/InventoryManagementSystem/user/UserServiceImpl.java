package com.phegondev.InventoryManagementSystem.user;

import com.phegondev.InventoryManagementSystem.auth.LoginRequest;
import com.phegondev.InventoryManagementSystem.auth.RegisterRequest;
import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.user.UserDTO;
import com.phegondev.InventoryManagementSystem.user.User;
import com.phegondev.InventoryManagementSystem.enums.UserRole;
import com.phegondev.InventoryManagementSystem.exceptions.InvalidCredentialsException;
import com.phegondev.InventoryManagementSystem.exceptions.NameValueRequiredException;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import com.phegondev.InventoryManagementSystem.user.UserRepository;
import com.phegondev.InventoryManagementSystem.security.JwtUtils;
import com.phegondev.InventoryManagementSystem.security.UserDetailsCache;
import com.phegondev.InventoryManagementSystem.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private static final String INVALID_LOGIN_MESSAGE = "Invalid email or password";

    /** 8-72 chars, at least one letter and one digit. BCrypt ignores input past 72 bytes. */
    private static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$";
    private static final String PASSWORD_MESSAGE =
            "Password must be at least 8 characters and contain both letters and numbers";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final JwtUtils jwtUtils;
    private final UserDetailsCache userDetailsCache;

    /**
     * A valid BCrypt hash of a throwaway value. When the email does not exist we still
     * run one encoder.matches() round so the response time matches the wrong-password
     * path; otherwise the fast 404-vs-400 difference alone reveals whether an email
     * is registered.
     */
    private volatile String timingEqualizerHash;

    private String getTimingEqualizerHash() {
        if (timingEqualizerHash == null) {
            synchronized (this) {
                if (timingEqualizerHash == null) {
                    timingEqualizerHash = passwordEncoder.encode("timing-equalizer");
                }
            }
        }
        return timingEqualizerHash;
    }

    @Override
    public Response registerUser(RegisterRequest registerRequest) {

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new NameValueRequiredException("An account with that email already exists");
        }

        // Self-service registration is a PUBLIC endpoint. The role from the request
        // body is deliberately ignored — honouring it previously let any anonymous
        // caller POST {"role":"ADMIN"} and take over the system.
        //
        // The sole exception is bootstrapping: on a completely empty user table the
        // first account created becomes ADMIN, because otherwise a fresh database
        // has no way to reach any admin-only endpoint. Once that account exists this
        // branch can never be taken again, so it is not an escalation path — every
        // later signup is a MANAGER, and promoting one is an admin-only action via
        // PUT /api/users/update/{id}.
        boolean isFirstUser = userRepository.count() == 0;
        UserRole role = isFirstUser ? UserRole.SUPER_ADMIN : UserRole.MANAGER;

        if (isFirstUser) {
            log.info("Bootstrapping first account '{}' as SUPER_ADMIN (user table was empty)",
                    registerRequest.getEmail());
        }

        User userToSave = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .phoneNumber(registerRequest.getPhoneNumber())
                .role(role)
                .branchId(null) // super admin global; branch assignment via admin
                .organizationId(1L)
                .status("ACTIVE")
                .build();

        userRepository.save(userToSave);

        return Response.builder()
                .status(200)
                .message("user created successfully")
                .build();
    }

    @Override
    public Response loginUser(LoginRequest loginRequest) {
        // Same message and same status for unknown email and wrong password — a
        // distinct "Email not Found" 404 let attackers enumerate registered users.
       User user = userRepository.findByEmail(loginRequest.getEmail())
               .orElse(null);

        if (user == null) {
            passwordEncoder.matches(loginRequest.getPassword(), getTimingEqualizerHash());
            throw new InvalidCredentialsException(INVALID_LOGIN_MESSAGE);
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(INVALID_LOGIN_MESSAGE);
        }
        String token = jwtUtils.generateToken(user.getEmail(), user.getBranchId(), user.getRole() != null ? user.getRole().name() : null);

        // The client reads `user` off the login response to identify the session.
        // Without it every session silently fell back to id "1".
        UserDTO userDTO = toSummaryDTO(user);

        return Response.builder()
                .status(200)
                .message("user logged in successfully")
                .role(user.getRole())
                .user(userDTO)
                .token(token)
                .expirationTime(jwtUtils.getExpirationDescription())
                .build();
    }

    @Override
    public Response getAllUsers() {
        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        // Built by hand rather than via ModelMapper: mapping the entity walks the lazy
        // `transactions` collection, which throws LazyInitializationException outside a
        // session. This endpoint returned 500 for every caller before that was fixed.
        List<UserDTO> userDTOS = users.stream().map(this::toSummaryDTO).toList();

        return Response.builder()
                .status(200)
                .message("success")
                .users(userDTOS)
                .build();
    }

    @Override
    public User getCurrentLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new InvalidCredentialsException("Not authenticated");
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new NotFoundException("User Not Found"));

        user.setTransactions(null);

        return user;
    }

    /**
     * Controller-facing variant. Never returns the User entity itself — that would
     * serialise the BCrypt password hash and the whole transaction graph to the client.
     */
    @Override
    public Response getCurrentUserProfile() {
        return Response.builder()
                .status(200)
                .message("success")
                .user(toSummaryDTO(getCurrentLoggedInUser()))
                .build();
    }

    /**
     * Identity fields only. Built by hand rather than via ModelMapper because the
     * lazy `transactions` collection cannot be touched outside a session
     * (spring.jpa.open-in-view=false), and it has no business in an auth response.
     */
    private UserDTO toSummaryDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole());
        dto.setBranchId(user.getBranchId());
        dto.setOrganizationId(user.getOrganizationId());
        dto.setTransactions(null);
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    /** True if this user is an ADMIN and no other ADMIN exists. */
    private boolean isSoleAdmin(User user) {
        return user.getRole() == UserRole.ADMIN && userRepository.countByRole(UserRole.ADMIN) <= 1;
    }

    private boolean isSoleSuperAdmin(User user) {
        return user.getRole() == UserRole.SUPER_ADMIN && userRepository.countByRole(UserRole.SUPER_ADMIN) <= 1;
    }

    @Override
    public Response updateUser(Long id, UserDTO userDTO) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("User Not Found"));

        // Nanba: SUPER_ADMIN is only one — never allow a second, and never demote/delete the sole one
        if (userDTO.getRole() == UserRole.SUPER_ADMIN
                && existingUser.getRole() != UserRole.SUPER_ADMIN
                && userRepository.countByRole(UserRole.SUPER_ADMIN) >= 1) {
            throw new NameValueRequiredException("Only one SUPER_ADMIN allowed — super admin is single daa");
        }
        if (userDTO.getRole() != null
                && userDTO.getRole() != existingUser.getRole()
                && isSoleSuperAdmin(existingUser)) {
            throw new NameValueRequiredException("Cannot change the role of the only SUPER_ADMIN account");
        }
        // Without this the last ADMIN could demote themselves, leaving a system with
        // no admin path to promote anyone back.
        if (userDTO.getRole() != null
                && userDTO.getRole() != existingUser.getRole()
                && isSoleAdmin(existingUser)) {
            throw new NameValueRequiredException("Cannot change the role of the only ADMIN account");
        }

        if (userDTO.getEmail() != null) existingUser.setEmail(userDTO.getEmail());
        if (userDTO.getName() != null) existingUser.setName(userDTO.getName());
        if (userDTO.getPhoneNumber() != null) existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        if (userDTO.getRole() != null) existingUser.setRole(userDTO.getRole());
        if (userDTO.getBranchId() != null) existingUser.setBranchId(userDTO.getBranchId());
        if (userDTO.getOrganizationId() != null) existingUser.setOrganizationId(userDTO.getOrganizationId());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            if (!userDTO.getPassword().matches(PASSWORD_PATTERN)) {
                throw new NameValueRequiredException(PASSWORD_MESSAGE);
            }
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        userRepository.save(existingUser);

        // Role/password/email changes must reach the auth filter immediately, not
        // after the 30s UserDetailsCache TTL.
        userDetailsCache.evict(existingUser.getEmail());

        return Response.builder()
                .status(200)
                .message("User Successfully updated")
                .build();
    }

    @Override
    public Response deleteUser(Long id) {

         User existingUser = userRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("User Not Found"));

        if (isSoleSuperAdmin(existingUser)) {
            throw new NameValueRequiredException("Cannot delete the only SUPER_ADMIN account — super admin is only one daa");
        }
        // Deleting the only ADMIN would leave no account able to grant the role again.
        if (isSoleAdmin(existingUser)) {
            throw new NameValueRequiredException("Cannot delete the only ADMIN account");
        }

         userRepository.deleteById(existingUser.getId());
        userDetailsCache.evict(existingUser.getEmail());

        return Response.builder()
                .status(200)
                .message("User Successfully Deleted")
                .build();
    }

    // readOnly transaction keeps the session open so the lazy `transactions`
    // collection — which this endpoint actually wants — can be initialised.
    @Transactional(readOnly = true)
    @Override
    public Response getUserTransactions(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("User Not Found"));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        userDTO.getTransactions().forEach(transactionDTO -> {
            transactionDTO.setUser(null);
            transactionDTO.setSupplier(null);
        });

        return Response.builder()
                .status(200)
                .message("success")
                .user(userDTO)
                .build();
    }


}
