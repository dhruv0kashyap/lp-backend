package com.linkedin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.linkedin.controller.PostController;
import com.linkedin.dto.request.CommentRequest;
import com.linkedin.dto.request.PostRequest;
import com.linkedin.dto.response.ApiResponse;
import com.linkedin.dto.response.CommentResponse;
import com.linkedin.dto.response.PostResponse;
import com.linkedin.service.PostService;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

@Mock
private PostService postService;

@InjectMocks
private PostController postController;

private UserDetails getUserDetails() {
    return User.withUsername("john.doe@example.com")
            .password("password")
            .authorities(Collections.emptyList())
            .build();
}

@Test
void createPost_Success() {
    UserDetails userDetails = getUserDetails();
    PostRequest request = new PostRequest();
    PostResponse postResponse = new PostResponse();

    when(postService.createPost("john.doe@example.com", request)).thenReturn(postResponse);

    ResponseEntity<ApiResponse<PostResponse>> response =
            postController.createPost(userDetails, request);

    assertEquals(201, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("Post created successfully", response.getBody().getMessage());
    assertEquals(postResponse, response.getBody().getData());
}

@Test
void updatePost_Success() {
    UserDetails userDetails = getUserDetails();
    PostRequest request = new PostRequest();
    PostResponse postResponse = new PostResponse();

    when(postService.updatePost("john.doe@example.com", 1L, request)).thenReturn(postResponse);

    ResponseEntity<ApiResponse<PostResponse>> response =
            postController.updatePost(userDetails, 1L, request);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Post updated successfully", response.getBody().getMessage());
    assertEquals(postResponse, response.getBody().getData());
}

@Test
void deletePost_Success() {
    UserDetails userDetails = getUserDetails();

    doNothing().when(postService).deletePost("john.doe@example.com", 1L);

    ResponseEntity<ApiResponse<Void>> response =
            postController.deletePost(userDetails, 1L);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Post deleted successfully", response.getBody().getMessage());
    verify(postService).deletePost("john.doe@example.com", 1L);
}

@Test
void getPostById_Success() {
    UserDetails userDetails = getUserDetails();
    PostResponse postResponse = new PostResponse();

    when(postService.getPostById("john.doe@example.com", 1L)).thenReturn(postResponse);

    ResponseEntity<ApiResponse<PostResponse>> response =
            postController.getPostById(userDetails, 1L);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Post fetched successfully", response.getBody().getMessage());
    assertEquals(postResponse, response.getBody().getData());
}

@Test
void getFeedPosts_Success() {
    UserDetails userDetails = getUserDetails();
    Page<PostResponse> page = new PageImpl<>(List.of(new PostResponse()));

    when(postService.getFeedPosts(eq("john.doe@example.com"), any())).thenReturn(page);

    ResponseEntity<ApiResponse<Page<PostResponse>>> response =
            postController.getFeedPosts(userDetails, 0, 10);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Feed fetched successfully", response.getBody().getMessage());
    assertEquals(page, response.getBody().getData());
}

@Test
void getUserPosts_Success() {
    UserDetails userDetails = getUserDetails();
    Page<PostResponse> page = new PageImpl<>(List.of(new PostResponse()));

    when(postService.getUserPosts(eq("john.doe@example.com"), eq(5L), any())).thenReturn(page);

    ResponseEntity<ApiResponse<Page<PostResponse>>> response =
            postController.getUserPosts(userDetails, 5L, 0, 10);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Posts fetched successfully", response.getBody().getMessage());
    assertEquals(page, response.getBody().getData());
}

@Test
void getDrafts_Success() {
    UserDetails userDetails = getUserDetails();
    Page<PostResponse> page = new PageImpl<>(List.of(new PostResponse()));

    when(postService.getDraftPosts(eq("john.doe@example.com"), any())).thenReturn(page);

    ResponseEntity<ApiResponse<Page<PostResponse>>> response =
            postController.getDrafts(userDetails, 0, 10);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Drafts fetched successfully", response.getBody().getMessage());
    assertEquals(page, response.getBody().getData());
}

@Test
void publishDraft_Success() {
    UserDetails userDetails = getUserDetails();
    PostResponse postResponse = new PostResponse();

    when(postService.publishDraft("john.doe@example.com", 3L)).thenReturn(postResponse);

    ResponseEntity<ApiResponse<PostResponse>> response =
            postController.publishDraft(userDetails, 3L);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Post published successfully", response.getBody().getMessage());
    assertEquals(postResponse, response.getBody().getData());
}

@Test
void toggleLike_Liked_Success() {
    UserDetails userDetails = getUserDetails();

    when(postService.toggleLike("john.doe@example.com", 2L)).thenReturn(true);

    ResponseEntity<ApiResponse<Boolean>> response =
            postController.toggleLike(userDetails, 2L);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Post liked", response.getBody().getMessage());
    assertTrue(response.getBody().getData());
}

@Test
void toggleLike_Unliked_Success() {
    UserDetails userDetails = getUserDetails();

    when(postService.toggleLike("john.doe@example.com", 2L)).thenReturn(false);

    ResponseEntity<ApiResponse<Boolean>> response =
            postController.toggleLike(userDetails, 2L);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Post unliked", response.getBody().getMessage());
    assertFalse(response.getBody().getData());
}

@Test
void addComment_Success() {
    UserDetails userDetails = getUserDetails();
    CommentRequest request = new CommentRequest();
    CommentResponse commentResponse = new CommentResponse();

    when(postService.addComment("john.doe@example.com", 1L, request)).thenReturn(commentResponse);

    ResponseEntity<ApiResponse<CommentResponse>> response =
            postController.addComment(userDetails, 1L, request);

    assertEquals(201, response.getStatusCode().value());
    assertEquals("Comment added successfully", response.getBody().getMessage());
    assertEquals(commentResponse, response.getBody().getData());
}

@Test
void getComments_Success() {
    Page<CommentResponse> page = new PageImpl<>(List.of(new CommentResponse()));

    when(postService.getComments(eq(1L), any())).thenReturn(page);

    ResponseEntity<ApiResponse<Page<CommentResponse>>> response =
            postController.getComments(1L, 0, 50);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Comments fetched successfully", response.getBody().getMessage());
    assertEquals(page, response.getBody().getData());
}

@Test
void deleteComment_Success() {
    UserDetails userDetails = getUserDetails();

    doNothing().when(postService).deleteComment("john.doe@example.com", 10L);

    ResponseEntity<ApiResponse<Void>> response =
            postController.deleteComment(userDetails, 10L);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Comment deleted successfully", response.getBody().getMessage());
    verify(postService).deleteComment("john.doe@example.com", 10L);
}

@Test
void sharePost_Success() {
    UserDetails userDetails = getUserDetails();
    PostResponse postResponse = new PostResponse();

    when(postService.sharePost("john.doe@example.com", 7L, "Nice post")).thenReturn(postResponse);

    ResponseEntity<ApiResponse<PostResponse>> response =
            postController.sharePost(userDetails, 7L, "Nice post");

    assertEquals(201, response.getStatusCode().value());
    assertEquals("Post shared successfully", response.getBody().getMessage());
    assertEquals(postResponse, response.getBody().getData());
}

}