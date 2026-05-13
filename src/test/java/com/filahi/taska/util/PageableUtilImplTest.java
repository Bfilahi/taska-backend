package com.filahi.taska.util;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class PageableUtilImplTest {

    @InjectMocks
    private PageableUtilImpl pageableUtil;

    private final int PAGE = 0;
    private final int SIZE = 10;


    @DisplayName("Should return pageable without sorting when sortBy is null")
    @Test
    public void shouldReturnPageableWithoutSortingWhenSortByIsNull() {
        String sortBy = null;
        String sortOrder = "ASC";

        Pageable result = pageableUtil.getPageable(PAGE, SIZE, sortBy, sortOrder);

        assertEquals(PAGE, result.getPageNumber());
        assertEquals(SIZE, result.getPageSize());
    }

    @DisplayName("Should return pageable without sorting when sortBy is blank")
    @Test
    public void shouldReturnPageableWithoutSortingWhenSortByIsBlank() {
        String sortBy = "";
        String sortOrder = "ASC";

        Pageable result = pageableUtil.getPageable(PAGE, SIZE, sortBy, sortOrder);

        assertEquals(PAGE, result.getPageNumber());
        assertEquals(SIZE, result.getPageSize());
    }

    @DisplayName("Should throw BAD_REQUEST when sortBy is not allowed")
    @Test
    public void shouldThrowExceptionWhenSortByIsInvalid() {
        String sortBy = "name";
        String sortOrder = "ASC";

        assertThrows(ResponseStatusException.class, () -> pageableUtil.getPageable(PAGE, SIZE, sortBy, sortOrder));
    }

    @DisplayName("Should return pageable sorted ascending when sortOrder is ASC")
    @Test
    public void shouldReturnPageableSortedAscending() {
        String sortBy = "dueDate";
        String sortOrder = "ASC";

        Pageable result = pageableUtil.getPageable(PAGE, SIZE, sortBy, sortOrder);
        Pageable expected = PageRequest.of(PAGE, SIZE, Sort.by(Sort.Direction.ASC, sortBy));

        assertEquals(expected, result);
    }

    @DisplayName("Should return pageable sorted descending when sortOrder is not ASC")
    @Test
    public void shouldReturnPageableSortedDescending() {
        String sortBy = "priority";
        String sortOrder = "DESC";

        Pageable result = pageableUtil.getPageable(0, 10, sortBy, sortOrder);
        Pageable expected = PageRequest.of(PAGE, SIZE, Sort.by(Sort.Direction.DESC, sortBy));

        assertEquals(expected, result);
    }
}
