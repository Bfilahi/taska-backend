package com.filahi.taska.response;

public record ProjectsStatsResponse(
        long totalProjects,
        long completedProjects
) {
}
