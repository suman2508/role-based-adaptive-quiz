package com.hackathon.quiz.service;

import com.hackathon.quiz.dto.UserDTO;

public interface UserService {
    UserDTO getUser(Long id);
    UserDTO createUser(UserDTO userDTO);
    UserDTO updateUser(Long id, UserDTO userDTO);
    void deleteUser(Long id);
}
