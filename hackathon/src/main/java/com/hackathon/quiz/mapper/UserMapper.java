package com.hackathon.quiz.mapper;

import com.hackathon.quiz.dto.UserDTO;
import com.hackathon.quiz.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserDTO toDto(User user);
    User toEntity(UserDTO dto);
}
