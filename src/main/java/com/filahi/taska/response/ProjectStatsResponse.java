package com.filahi.taska.response;

public record ProjectStatsResponse(
        long totalTasks,
        long completedTasks,
        long tasksInProgress
) {
}
