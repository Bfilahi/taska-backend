package com.filahi.taska.response;

import java.time.LocalDate;

public record NoteResponse(
        long id,
        String note,
        LocalDate createdAt
) {
}
