package com.filahi.taska.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Component
public class PageableUtilImpl implements PageableUtil{
    @Override
    public Pageable getPageable(int page, int size, String sortBy, String sortOrder) {
        Pageable pageable;
        Set<String> allowedFields = Set.of("dueDate", "priority");

        if(sortBy == null || sortBy.isBlank())
            pageable = PageRequest.of(page, size);
        else if(!allowedFields.contains(sortBy))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid sort field: " + sortBy);
        else{
            Sort.Direction sortDirection = sortOrder.equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Sort sort = Sort.by(sortDirection, sortBy);
            pageable = PageRequest.of(page, size, sort);
        }
        return pageable;
    }
}
