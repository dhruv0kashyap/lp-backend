package com.linkedin.service;

import com.linkedin.dto.request.CommentRequest;
import com.linkedin.dto.request.PostRequest;
import com.linkedin.dto.response.CommentResponse;
import com.linkedin.dto.response.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PostService {
    PostResponse createPost(String email, PostRequest request);
    PostResponse updatePost(String email, Long postId, PostRequest request);
    void deletePost(String email, Long postId);
    PostResponse getPostById(String email, Long postId);
    Page<PostResponse> getFeedPosts(String email, Pageable pageable);
    Page<PostResponse> getUserPosts(String email, Long userId, Pageable pageable);
    Page<PostResponse> getDraftPosts(String email, Pageable pageable);
    boolean toggleLike(String email, Long postId);
    CommentResponse addComment(String email, Long postId, CommentRequest request);
    Page<CommentResponse> getComments(Long postId, Pageable pageable);
    void deleteComment(String email, Long commentId);
    PostResponse sharePost(String email, Long postId, String additionalText);
    PostResponse publishDraft(String email, Long postId);
}
