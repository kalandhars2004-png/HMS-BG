package com.phegondev.InventoryManagementSystem.user;

import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.user.UserDTO;
import com.phegondev.InventoryManagementSystem.user.User;
import com.phegondev.InventoryManagementSystem.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ADMIN only: this endpoint can change a user's role, email and password, so an
    // unguarded version let any authenticated user promote themselves to ADMIN or
    // take over another account outright.
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> updateUser(@PathVariable Long id,  @RequestBody UserDTO userDTO){
        return ResponseEntity.ok(userService.updateUser(id, userDTO));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> deleteUser(@PathVariable Long id){
        return ResponseEntity.ok(userService.deleteUser(id));
    }


    @GetMapping("/transactions/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> getUserAndTransactions(@PathVariable Long userId){
        return ResponseEntity.ok(userService.getUserTransactions(userId));
    }


    // Returns a UserDTO inside the standard envelope. Returning the User entity
    // directly serialised the BCrypt password hash to the browser.
    @GetMapping("/current")
    public ResponseEntity<Response> getCurrentUser(){
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }
}
