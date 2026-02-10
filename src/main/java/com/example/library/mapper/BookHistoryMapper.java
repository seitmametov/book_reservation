package com.example.library.mapper;

import com.example.library.Dto.response.BookHistoryResponse;
import com.example.library.entity.BookHistory;
import org.springframework.stereotype.Component;

@Component
public class BookHistoryMapper {

    public BookHistoryResponse toResponse(BookHistory history) {
        return new BookHistoryResponse(
                history.getId(),
                history.getEventType(),
                history.getUser() != null ? history.getUser().getEmail() : null,
                history.getComment(),
                history.getCreatedAt()
        );
    }
}