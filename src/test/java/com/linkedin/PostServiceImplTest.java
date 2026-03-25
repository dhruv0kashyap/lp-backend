


package com.linkedin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.linkedin.dto.request.CommentRequest;
import com.linkedin.dto.request.PostRequest;
import com.linkedin.dto.response.CommentResponse;
import com.linkedin.dto.response.PostResponse;
import com.linkedin.entity.Comment;
import com.linkedin.entity.Hashtag;
import com.linkedin.entity.Like;
import com.linkedin.entity.Notification;
import com.linkedin.entity.Post;
import com.linkedin.entity.User;
import com.linkedin.enums.PostStatus;
import com.linkedin.enums.PostType;
import com.linkedin.exception.BadRequestException;
import com.linkedin.exception.ResourceNotFoundException;
import com.linkedin.exception.UnauthorizedException;
import com.linkedin.repository.CommentRepository;
import com.linkedin.repository.ConnectionRepository;
import com.linkedin.repository.HashtagRepository;
import com.linkedin.repository.LikeRepository;
import com.linkedin.repository.NotificationRepository;
import com.linkedin.repository.PostRepository;
import com.linkedin.repository.UserRepository;
import com.linkedin.serviceImpl.PostServiceImpl;
@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

@Mock
private PostRepository postRepository;
@Mock
private UserRepository userRepository;
@Mock
private LikeRepository likeRepository;
@Mock
private CommentRepository commentRepository;
@Mock
private HashtagRepository hashtagRepository;
@Mock
private ConnectionRepository connectionRepository;
@Mock
private NotificationRepository notificationRepository;

@InjectMocks
private PostServiceImpl postService;

private User buildUser(Long id, String email, String firstName, String lastName) {
    return User.builder()
            .id(id)
            .email(email)
            .firstName(firstName)
            .lastName(lastName)
            .headline("Software Engineer")
            .profilePhotoUrl("photo.jpg")
            .build();
}

private Post buildPost(Long id, User user, PostStatus status) {
    return Post.builder()
            .id(id)
            .user(user)
            .content("Test post content")
            .postType(PostType.POST)
            .status(status)
            .hashtags(new ArrayList<>())
            .imageUrl("img.jpg")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
}

private Comment buildComment(Long id, Post post, User user, String content) {
    return Comment.builder()
            .id(id)
            .post(post)
            .user(user)
            .content(content)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
}

@Test
void createPost_Success_WithHashtags() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");

    PostRequest request = new PostRequest();
    request.setContent("Hello world");
    request.setPostType(PostType.POST);
    request.setStatus(PostStatus.PUBLISHED);
    request.setImageUrl("img.jpg");
    request.setHashtags(List.of("java", "spring"));

    Hashtag h1 = Hashtag.builder().id(1L).tag("java").build();
    Hashtag h2 = Hashtag.builder().id(2L).tag("spring").build();

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(hashtagRepository.findByTag("java")).thenReturn(Optional.empty());
    when(hashtagRepository.findByTag("spring")).thenReturn(Optional.empty());
    when(hashtagRepository.save(any(Hashtag.class))).thenReturn(h1, h2);
    when(postRepository.save(any(Post.class))).thenAnswer(inv -> {
        Post p = inv.getArgument(0);
        p.setId(10L);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        return p;
    });
    when(likeRepository.countByPostId(10L)).thenReturn(0L);
    when(commentRepository.countByPostId(10L)).thenReturn(0L);
    when(likeRepository.existsByPostIdAndUserId(10L, 1L)).thenReturn(false);

    PostResponse response = postService.createPost("john@example.com", request);

    assertNotNull(response);
    assertEquals("Hello world", response.getContent());
    assertEquals(2, response.getHashtags().size());
    verify(postRepository).save(any(Post.class));
}

@Test
void createPost_UserNotFound_ThrowsException() {
    PostRequest request = new PostRequest();
    request.setContent("Hello");

    when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
            () -> postService.createPost("missing@example.com", request));
}

@Test
void updatePost_Success() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    Post post = buildPost(10L, user, PostStatus.PUBLISHED);

    PostRequest request = new PostRequest();
    request.setContent("Updated content");
    request.setStatus(PostStatus.DRAFT);
    request.setImageUrl("newimg.jpg");
    request.setScheduledAt(LocalDateTime.now().plusDays(1));

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(postRepository.findById(10L)).thenReturn(Optional.of(post));
    when(postRepository.save(any(Post.class))).thenReturn(post);
    when(likeRepository.countByPostId(10L)).thenReturn(0L);
    when(commentRepository.countByPostId(10L)).thenReturn(0L);
    when(likeRepository.existsByPostIdAndUserId(10L, 1L)).thenReturn(false);

    PostResponse response = postService.updatePost("john@example.com", 10L, request);

    assertNotNull(response);
    verify(postRepository).save(post);
    assertEquals("Updated content", post.getContent());
    assertEquals(PostStatus.DRAFT, post.getStatus());
    assertEquals("newimg.jpg", post.getImageUrl());
}

@Test
void updatePost_PostNotFound_ThrowsException() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    PostRequest request = new PostRequest();
    request.setContent("Updated");

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(postRepository.findById(10L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
            () -> postService.updatePost("john@example.com", 10L, request));
}

@Test
void updatePost_Unauthorized_ThrowsException() {
    User currentUser = buildUser(1L, "john@example.com", "John", "Doe");
    User anotherUser = buildUser(2L, "jane@example.com", "Jane", "Doe");
    Post post = buildPost(10L, anotherUser, PostStatus.PUBLISHED);

    PostRequest request = new PostRequest();
    request.setContent("Updated");

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(currentUser));
    when(postRepository.findById(10L)).thenReturn(Optional.of(post));

    assertThrows(UnauthorizedException.class,
            () -> postService.updatePost("john@example.com", 10L, request));
}

@Test
void deletePost_Success() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    Post post = buildPost(10L, user, PostStatus.PUBLISHED);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(postRepository.findById(10L)).thenReturn(Optional.of(post));

    postService.deletePost("john@example.com", 10L);

    verify(postRepository).delete(post);
}

@Test
void deletePost_Unauthorized_ThrowsException() {
    User currentUser = buildUser(1L, "john@example.com", "John", "Doe");
    User anotherUser = buildUser(2L, "jane@example.com", "Jane", "Doe");
    Post post = buildPost(10L, anotherUser, PostStatus.PUBLISHED);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(currentUser));
    when(postRepository.findById(10L)).thenReturn(Optional.of(post));

    assertThrows(UnauthorizedException.class,
            () -> postService.deletePost("john@example.com", 10L));
}

@Test
void getPostById_Success() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    Post post = buildPost(10L, user, PostStatus.PUBLISHED);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(postRepository.findById(10L)).thenReturn(Optional.of(post));
    when(likeRepository.countByPostId(10L)).thenReturn(3L);
    when(commentRepository.countByPostId(10L)).thenReturn(2L);
    when(likeRepository.existsByPostIdAndUserId(10L, 1L)).thenReturn(true);

    PostResponse response = postService.getPostById("john@example.com", 10L);

    assertNotNull(response);
    assertEquals(10L, response.getId());
    assertEquals(3L, response.getLikeCount());
    assertEquals(2L, response.getCommentCount());
    assertTrue(response.isLikedByCurrentUser());
}

@Test
void getFeedPosts_Success() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    Post post = buildPost(10L, user, PostStatus.PUBLISHED);
    Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
    Page<Post> postPage = new PageImpl<>(List.of(post), pageable, 1);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(connectionRepository.findConnectionUserIds(1L)).thenReturn(new ArrayList<>(List.of(2L, 3L)));
    when(postRepository.findFeedPostsWithScheduled(anyList(), any(LocalDateTime.class), eq(pageable)))
            .thenReturn(postPage);
    when(likeRepository.countByPostId(10L)).thenReturn(1L);
    when(commentRepository.countByPostId(10L)).thenReturn(1L);
    when(likeRepository.existsByPostIdAndUserId(10L, 1L)).thenReturn(false);

    Page<PostResponse> response = postService.getFeedPosts("john@example.com", pageable);

    assertEquals(1, response.getContent().size());
    verify(connectionRepository).findConnectionUserIds(1L);
}

@Test
void getUserPosts_Success() {
    User currentUser = buildUser(1L, "john@example.com", "John", "Doe");
    User targetUser = buildUser(2L, "jane@example.com", "Jane", "Doe");
    Post post = buildPost(10L, targetUser, PostStatus.PUBLISHED);
    Pageable pageable = PageRequest.of(0, 10);
    Page<Post> page = new PageImpl<>(List.of(post), pageable, 1);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(currentUser));
    when(postRepository.findByUserIdOrderByCreatedAtDesc(2L, pageable)).thenReturn(page);
    when(likeRepository.countByPostId(10L)).thenReturn(0L);
    when(commentRepository.countByPostId(10L)).thenReturn(0L);
    when(likeRepository.existsByPostIdAndUserId(10L, 1L)).thenReturn(false);

    Page<PostResponse> response = postService.getUserPosts("john@example.com", 2L, pageable);

    assertEquals(1, response.getContent().size());
}

@Test
void getDraftPosts_Success() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    Post post = buildPost(10L, user, PostStatus.DRAFT);
    Pageable pageable = PageRequest.of(0, 10);
    Page<Post> page = new PageImpl<>(List.of(post), pageable, 1);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(postRepository.findByUserIdAndStatusOrderByCreatedAtDesc(1L, PostStatus.DRAFT, pageable)).thenReturn(page);
    when(likeRepository.countByPostId(10L)).thenReturn(0L);
    when(commentRepository.countByPostId(10L)).thenReturn(0L);
    when(likeRepository.existsByPostIdAndUserId(10L, 1L)).thenReturn(false);

    Page<PostResponse> response = postService.getDraftPosts("john@example.com", pageable);

    assertEquals(1, response.getContent().size());
    assertEquals(PostStatus.DRAFT, response.getContent().get(0).getStatus());
}

@Test
void publishDraft_Success() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    Post post = buildPost(10L, user, PostStatus.DRAFT);
    post.setScheduledAt(LocalDateTime.now().plusDays(1));

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(postRepository.findById(10L)).thenReturn(Optional.of(post));
    when(postRepository.save(post)).thenReturn(post);
    when(likeRepository.countByPostId(10L)).thenReturn(0L);
    when(commentRepository.countByPostId(10L)).thenReturn(0L);
    when(likeRepository.existsByPostIdAndUserId(10L, 1L)).thenReturn(false);

    PostResponse response = postService.publishDraft("john@example.com", 10L);

    assertNotNull(response);
    assertEquals(PostStatus.PUBLISHED, post.getStatus());
    assertNull(post.getScheduledAt());
}

@Test
void publishDraft_Unauthorized_ThrowsException() {
    User currentUser = buildUser(1L, "john@example.com", "John", "Doe");
    User anotherUser = buildUser(2L, "jane@example.com", "Jane", "Doe");
    Post post = buildPost(10L, anotherUser, PostStatus.DRAFT);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(currentUser));
    when(postRepository.findById(10L)).thenReturn(Optional.of(post));

    assertThrows(UnauthorizedException.class,
            () -> postService.publishDraft("john@example.com", 10L));
}

@Test
void publishDraft_NotDraft_ThrowsException() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    Post post = buildPost(10L, user, PostStatus.PUBLISHED);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(postRepository.findById(10L)).thenReturn(Optional.of(post));

    assertThrows(BadRequestException.class,
            () -> postService.publishDraft("john@example.com", 10L));
}

@Test
void toggleLike_WhenLikeExists_RemovesLikeAndReturnsFalse() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    Post post = buildPost(10L, user, PostStatus.PUBLISHED);
    Like like = Like.builder().id(100L).post(post).user(user).build();

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(postRepository.findById(10L)).thenReturn(Optional.of(post));
    when(likeRepository.findByPostIdAndUserId(10L, 1L)).thenReturn(Optional.of(like));

    boolean result = postService.toggleLike("john@example.com", 10L);

    assertFalse(result);
    verify(likeRepository).delete(like);
}

@Test
void toggleLike_WhenNoLikeExists_AddsLikeAndReturnsTrue() {
    User liker = buildUser(1L, "john@example.com", "John", "Doe");
    User owner = buildUser(2L, "jane@example.com", "Jane", "Doe");
    Post post = buildPost(10L, owner, PostStatus.PUBLISHED);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(liker));
    when(postRepository.findById(10L)).thenReturn(Optional.of(post));
    when(likeRepository.findByPostIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

    boolean result = postService.toggleLike("john@example.com", 10L);

    assertTrue(result);
    verify(likeRepository).save(any(Like.class));
    verify(notificationRepository).save(any(Notification.class));
}

@Test
void toggleLike_OwnPost_AddsLikeWithoutNotification() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    Post post = buildPost(10L, user, PostStatus.PUBLISHED);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(postRepository.findById(10L)).thenReturn(Optional.of(post));
    when(likeRepository.findByPostIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

    boolean result = postService.toggleLike("john@example.com", 10L);

    assertTrue(result);
    verify(likeRepository).save(any(Like.class));
    verify(notificationRepository, never()).save(any(Notification.class));
}

@Test
void addComment_Success_WithNotification() {
    User commenter = buildUser(1L, "john@example.com", "John", "Doe");
    User owner = buildUser(2L, "jane@example.com", "Jane", "Doe");
    Post post = buildPost(10L, owner, PostStatus.PUBLISHED);

    CommentRequest request = new CommentRequest();
    request.setContent("Great post, really helpful!");

    Comment comment = buildComment(20L, post, commenter, request.getContent());

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(commenter));
    when(postRepository.findById(10L)).thenReturn(Optional.of(post));
    when(commentRepository.save(any(Comment.class))).thenReturn(comment);

    CommentResponse response = postService.addComment("john@example.com", 10L, request);

    assertNotNull(response);
    assertEquals("Great post, really helpful!", response.getContent());
    verify(notificationRepository).save(any(Notification.class));
}

@Test
void addComment_OnOwnPost_NoNotification() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    Post post = buildPost(10L, user, PostStatus.PUBLISHED);

    CommentRequest request = new CommentRequest();
    request.setContent("My own comment");

    Comment comment = buildComment(20L, post, user, request.getContent());

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(postRepository.findById(10L)).thenReturn(Optional.of(post));
    when(commentRepository.save(any(Comment.class))).thenReturn(comment);

    CommentResponse response = postService.addComment("john@example.com", 10L, request);

    assertNotNull(response);
    verify(notificationRepository, never()).save(any(Notification.class));
}

@Test
void getComments_Success() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    Post post = buildPost(10L, user, PostStatus.PUBLISHED);
    Comment comment = buildComment(20L, post, user, "Nice post");

    Pageable pageable = PageRequest.of(0, 10);
    Page<Comment> page = new PageImpl<>(List.of(comment), pageable, 1);

    when(commentRepository.findByPostIdOrderByCreatedAtDesc(10L, pageable)).thenReturn(page);

    Page<CommentResponse> response = postService.getComments(10L, pageable);

    assertEquals(1, response.getContent().size());
    assertEquals("Nice post", response.getContent().get(0).getContent());
}

@Test
void deleteComment_Success() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    Post post = buildPost(10L, user, PostStatus.PUBLISHED);
    Comment comment = buildComment(20L, post, user, "Nice post");

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(commentRepository.findById(20L)).thenReturn(Optional.of(comment));

    postService.deleteComment("john@example.com", 20L);

    verify(commentRepository).delete(comment);
}

@Test
void deleteComment_Unauthorized_ThrowsException() {
    User currentUser = buildUser(1L, "john@example.com", "John", "Doe");
    User owner = buildUser(2L, "jane@example.com", "Jane", "Doe");
    Post post = buildPost(10L, owner, PostStatus.PUBLISHED);
    Comment comment = buildComment(20L, post, owner, "Nice post");

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(currentUser));
    when(commentRepository.findById(20L)).thenReturn(Optional.of(comment));

    assertThrows(UnauthorizedException.class,
            () -> postService.deleteComment("john@example.com", 20L));
}

@Test
void sharePost_Success_WithAdditionalText() {
    User sharer = buildUser(1L, "john@example.com", "John", "Doe");
    User originalOwner = buildUser(2L, "jane@example.com", "Jane", "Doe");
    Post originalPost = buildPost(10L, originalOwner, PostStatus.PUBLISHED);
    originalPost.setContent("Original content");
    originalPost.setHashtags(List.of(Hashtag.builder().id(1L).tag("java").build()));

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(sharer));
    when(postRepository.findById(10L)).thenReturn(Optional.of(originalPost));
    when(postRepository.save(any(Post.class))).thenAnswer(inv -> {
        Post p = inv.getArgument(0);
        p.setId(99L);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        return p;
    });
    when(likeRepository.countByPostId(99L)).thenReturn(0L);
    when(commentRepository.countByPostId(99L)).thenReturn(0L);
    when(likeRepository.existsByPostIdAndUserId(99L, 1L)).thenReturn(false);

    PostResponse response = postService.sharePost("john@example.com", 10L, "Must read");

    assertNotNull(response);
    assertTrue(response.getContent().contains("Must read"));
    assertTrue(response.getContent().contains("Original content"));
}

@Test
void sharePost_PostNotFound_ThrowsException() {
    User sharer = buildUser(1L, "john@example.com", "John", "Doe");

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(sharer));
    when(postRepository.findById(10L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
            () -> postService.sharePost("john@example.com", 10L, "text"));
}

}
