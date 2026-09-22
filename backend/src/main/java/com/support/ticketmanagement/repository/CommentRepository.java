package com.support.ticketmanagement.repository;

import com.support.ticketmanagement.domain.entity.Comment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByTicket_IdOrderByCreatedAtAsc(UUID ticketId);
}
