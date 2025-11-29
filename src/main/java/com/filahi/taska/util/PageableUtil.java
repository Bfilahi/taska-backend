package com.filahi.taska.util;

import org.springframework.data.domain.Pageable;

public interface PageableUtil {
    Pageable getPageable(int page, int size, String sortBy, String sortOrder);
}
