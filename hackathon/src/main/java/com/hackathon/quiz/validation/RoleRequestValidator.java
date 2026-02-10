package com.hackathon.quiz.validation;

import com.hackathon.quiz.dto.request.RoleAnalyzeRequest;
import com.hackathon.quiz.exception.ValidationException;
import com.hackathon.quiz.util.Constants;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Programmatic validator for RoleAnalyzeRequest beyond annotation-based checks.
 * Keeps controllers thin and provides rich error details for clients.
 */
@Component
public class RoleRequestValidator {

    /**
     * Validates role analysis request. Throws ValidationException with details on failure.
     */
    public void validate(RoleAnalyzeRequest request) {
        List<String> details = new ArrayList<>();

        if (request == null) {
            throw new ValidationException("VALIDATION_ERROR", "Request must not be null", null);
        }

        String roleName = request.getRoleName();
        if (roleName == null || roleName.trim().isEmpty()) {
            details.add("roleName: must not be blank");
        } else if (roleName.length() > 128) {
            details.add("roleName: length must be <= 128");
        }

        Integer topN = request.getTopN();
        if (topN != null) {
            if (topN <= 0) {
                details.add("topN: must be > 0");
            } else if (topN > Constants.DEFAULT_TOP_SKILLS) {
                details.add("topN: must be <= " + Constants.DEFAULT_TOP_SKILLS);
            }
        }

        if (!details.isEmpty()) {
            throw new ValidationException("VALIDATION_ERROR", "Invalid role analysis request", details);
        }
    }
}
