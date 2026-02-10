package com.hackathon.quiz.service.impl;

import com.hackathon.quiz.dto.UserDTO;
import com.hackathon.quiz.entity.User;
import com.hackathon.quiz.exception.ResourceNotFoundException;
import com.hackathon.quiz.mapper.UserMapper;
import com.hackathon.quiz.repository.UserRepository;
import com.hackathon.quiz.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDTO getUser(Long id) {
        log.debug("Fetching user by id={}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + id));
        return userMapper.toDto(user);
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        log.info("Creating user with email={}", userDTO.getEmail());
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + userDTO.getEmail());
        }
        User entity = userMapper.toEntity(userDTO);
        if (entity.getReadinessScore() == null) {
            entity.setReadinessScore(0.0);
        }
        User saved = userRepository.save(entity);
        return userMapper.toDto(saved);
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.info("Updating user id={}", id);
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + id));

        // If email changed, enforce uniqueness
        if (userDTO.getEmail() != null && !userDTO.getEmail().equalsIgnoreCase(existing.getEmail())) {
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + userDTO.getEmail());
            }
            existing.setEmail(userDTO.getEmail());
        }

        if (userDTO.getName() != null) {
            existing.setName(userDTO.getName());
        }
        if (userDTO.getTargetRole() != null) {
            existing.setTargetRole(userDTO.getTargetRole());
        }
        if (userDTO.getReadinessScore() != null) {
            existing.setReadinessScore(userDTO.getReadinessScore());
        }

        User saved = userRepository.save(existing);
        return userMapper.toDto(saved);
    }

    @Override
    public void deleteUser(Long id) {
        log.warn("Deleting user id={}", id);
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found: id=" + id);
        }
        userRepository.deleteById(id);
    }
}
