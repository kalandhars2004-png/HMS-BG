package com.phegondev.InventoryManagementSystem.auth;

import com.phegondev.InventoryManagementSystem.auth.LoginRequest;
import com.phegondev.InventoryManagementSystem.auth.RegisterRequest;
import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.user.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Value("${jwt.expiration-minutes:120}")
    private long expirationMinutes;

    @PostMapping("/register")
    public ResponseEntity<Response> registerUser(@RequestBody @Valid RegisterRequest registerRequest){
        return ResponseEntity.ok(userService.registerUser(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<Response> loginUser(@RequestBody @Valid LoginRequest loginRequest, HttpServletResponse servletResponse){
        Response resp = userService.loginUser(loginRequest);
        // Defense-in-depth: also set httpOnly cookie so proxy/middleware can guard routes
        // without XSS-exfiltratable localStorage. Header auth still works.
        if (resp.getToken() != null) {
            ResponseCookie cookie = ResponseCookie.from("authToken", resp.getToken())
                    .httpOnly(true)
                    .secure(false) // set true behind HTTPS (prod should terminate TLS)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(Duration.ofMinutes(expirationMinutes))
                    .build();
            servletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/logout")
    public ResponseEntity<Response> logout(HttpServletResponse servletResponse){
        ResponseCookie clear = ResponseCookie.from("authToken", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        servletResponse.addHeader(HttpHeaders.SET_COOKIE, clear.toString());
        return ResponseEntity.ok(Response.builder().status(200).message("logged out").build());
    }
}
