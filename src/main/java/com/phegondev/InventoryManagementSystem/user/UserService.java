package com.phegondev.InventoryManagementSystem.user;

import com.phegondev.InventoryManagementSystem.auth.LoginRequest;
import com.phegondev.InventoryManagementSystem.auth.RegisterRequest;
import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.user.UserDTO;
import com.phegondev.InventoryManagementSystem.user.User;

public interface UserService {
    Response registerUser(RegisterRequest registerRequest);
    Response loginUser(LoginRequest loginRequest);
    Response getAllUsers();
    User getCurrentLoggedInUser();
    Response getCurrentUserProfile();
    Response updateUser(Long id, UserDTO userDTO);
    Response deleteUser(Long id);
    Response getUserTransactions(Long id);
    Response getBillers();
}
