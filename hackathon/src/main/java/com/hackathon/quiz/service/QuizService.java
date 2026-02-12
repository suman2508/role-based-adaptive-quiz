package com.hackathon.quiz.service;

import com.hackathon.quiz.dto.request.QuizStartRequest;
import com.hackathon.quiz.dto.request.QuizSubmitRequest;
import com.hackathon.quiz.dto.response.QuizQuestionResponse;
import com.hackathon.quiz.dto.response.QuizSubmitResponse;

import java.util.List;

public interface QuizService {
    List<QuizQuestionResponse> start(QuizStartRequest request);
    QuizSubmitResponse submit(QuizSubmitRequest request);
    QuizQuestionResponse nextAdaptive(Long userId);
}
