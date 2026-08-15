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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final JwtUtils jwtUtils;


    @Override
    public Response registerUser(RegisterRequest registerRequest) {

        // Self-service registration is a PUBLIC endpoint. The requested role is
        // deliberately ignored here — honouring it previously let any anonymous
        // caller POST {"role":"ADMIN"} and take over the system. Role changes are
        // an administrative action: see UserController.updateUser (ADMIN only).
        UserRole role = UserRole.MANAGER;

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new NameValueRequiredException("An account with that email already exists");
        }

        User userToSave = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .phoneNumber(registerRequest.getPhoneNumber())
                .role(role)
                .build();

        userRepository.save(userToSave);

        return Response.builder()
                .status(200)
                .message("user created successfully")
                .build();
    }

    @Override
    public Response loginUser(LoginRequest loginRequest) {
       User user = userRepository.findByEmail(loginRequest.getEmail())
               .orElseThrow(()-> new NotFoundException("Email not Found"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("password does not match");
        }
        String token = jwtUtils.generateToken(user.getEmail());

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
        dto.setTransactions(null);
        return dto;
    }

    @Override
    public Response updateUser(Long id, UserDTO userDTO) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("User Not Found"));

        if (userDTO.getEmail() != null) existingUser.setEmail(userDTO.getEmail());
        if (userDTO.getName() != null) existingUser.setName(userDTO.getName());
        if (userDTO.getPhoneNumber() != null) existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        if (userDTO.getRole() != null) existingUser.setRole(userDTO.getRole());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        userRepository.save(existingUser);

        return Response.builder()
                .status(200)
                .message("User Successfully updated")
                .build();
    }

    @Override
    public Response deleteUser(Long id) {

         userRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("User Not Found"));
         userRepository.deleteById(id);

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
