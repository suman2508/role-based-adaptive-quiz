package com.hackathon.quiz.ai.model;

/**
 * Model returned by AI extraction for a role.
 * Using Java record to avoid Lombok dependency here.
 */
public record AiSkill(String name, int priorityScore) { }
