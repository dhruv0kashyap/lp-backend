package com.linkedin.repository;

import com.linkedin.entity.Post;
import com.linkedin.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Post> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, PostStatus status, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.user.id IN :userIds " +
           "AND (p.status = 'PUBLISHED' OR (p.status = 'SCHEDULED' AND p.scheduledAt <= :now)) " +
           "ORDER BY p.createdAt DESC")
    Page<Post> findFeedPostsWithScheduled(@Param("userIds") List<Long> userIds,
                                          @Param("now") LocalDateTime now,
                                          Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.user.id IN :userIds AND p.status = 'PUBLISHED' ORDER BY p.createdAt DESC")
    Page<Post> findFeedPosts(@Param("userIds") List<Long> userIds, Pageable pageable);
}
