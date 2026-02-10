package com.hackathon.quiz.mapper;

import com.hackathon.quiz.dto.response.SkillResponse;
import com.hackathon.quiz.entity.Skill;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SkillMapper {
    SkillResponse toResponse(Skill skill);
}
