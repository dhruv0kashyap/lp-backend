package com.linkedin.controller;

import com.linkedin.dto.request.CommentRequest;
import com.linkedin.dto.request.PostRequest;
import com.linkedin.dto.response.*;
import com.linkedin.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Post and article management APIs")
public class PostController {

    private final PostService postService;

    @PostMapping
    @Operation(summary = "Create a new post or article")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Post created successfully",
                        postService.createPost(userDetails.getUsername(), request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a post")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Post updated successfully",
                postService.updatePost(userDetails.getUsername(), id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a post")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        postService.deletePost(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Post deleted successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get post by ID")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Post fetched successfully",
                postService.getPostById(userDetails.getUsername(), id)));
    }

    @GetMapping("/feed")
    @Operation(summary = "Get feed posts (published + scheduled posts that are due)")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getFeedPosts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Feed fetched successfully",
                postService.getFeedPosts(userDetails.getUsername(),
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get posts by user")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getUserPosts(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Posts fetched successfully",
                postService.getUserPosts(userDetails.getUsername(), userId, PageRequest.of(page, size))));
    }

    @GetMapping("/drafts")
    @Operation(summary = "Get current user's draft posts")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getDrafts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Drafts fetched successfully",
                postService.getDraftPosts(userDetails.getUsername(), PageRequest.of(page, size))));
    }

    @PutMapping("/{id}/publish")
    @Operation(summary = "Publish a draft post")
    public ResponseEntity<ApiResponse<PostResponse>> publishDraft(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Post published successfully",
                postService.publishDraft(userDetails.getUsername(), id)));
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "Toggle like on a post")
    public ResponseEntity<ApiResponse<Boolean>> toggleLike(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        boolean liked = postService.toggleLike(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success(liked ? "Post liked" : "Post unliked", liked));
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Add a comment to a post")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added successfully",
                        postService.addComment(userDetails.getUsername(), id, request)));
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "Get all comments for a post")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success("Comments fetched successfully",
                postService.getComments(id, PageRequest.of(page, size))));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Delete a comment")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long commentId) {
        postService.deleteComment(userDetails.getUsername(), commentId);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully"));
    }

    @PostMapping("/{id}/share")
    @Operation(summary = "Share a post to your feed")
    public ResponseEntity<ApiResponse<PostResponse>> sharePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam(required = false) String text) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Post shared successfully",
                        postService.sharePost(userDetails.getUsername(), id, text)));
    }
}
