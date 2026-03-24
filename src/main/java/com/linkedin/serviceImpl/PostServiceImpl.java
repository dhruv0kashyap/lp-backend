package com.linkedin.serviceImpl;

import com.linkedin.dto.request.CommentRequest;
import com.linkedin.dto.request.PostRequest;
import com.linkedin.dto.response.CommentResponse;
import com.linkedin.dto.response.PostResponse;
import com.linkedin.entity.*;
import com.linkedin.enums.NotificationType;
import com.linkedin.enums.PostStatus;
import com.linkedin.enums.PostType;
import com.linkedin.exception.BadRequestException;
import com.linkedin.exception.ResourceNotFoundException;
import com.linkedin.exception.UnauthorizedException;
import com.linkedin.repository.*;
import com.linkedin.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final HashtagRepository hashtagRepository;
    private final ConnectionRepository connectionRepository;
    private final NotificationRepository notificationRepository;

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public PostResponse createPost(String email, PostRequest request) {
        User user = getUserByEmail(email);

        List<Hashtag> hashtags = new ArrayList<>();
        if (request.getHashtags() != null) {
            for (String tag : request.getHashtags()) {
                Hashtag hashtag = hashtagRepository.findByTag(tag)
                        .orElseGet(() -> hashtagRepository.save(Hashtag.builder().tag(tag).build()));
                hashtags.add(hashtag);
            }
        }

        Post post = Post.builder()
                .user(user)
                .content(request.getContent())
                .postType(request.getPostType() != null ? request.getPostType() : PostType.POST)
                .status(request.getStatus() != null ? request.getStatus() : PostStatus.PUBLISHED)
                .scheduledAt(request.getScheduledAt())
                .imageUrl(request.getImageUrl())
                .hashtags(hashtags)
                .build();

        return mapToPostResponse(postRepository.save(post), user.getId());
    }

    @Override
    @Transactional
    public PostResponse updatePost(String email, Long postId, PostRequest request) {
        User user = getUserByEmail(email);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        if (!post.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to update this post");
        }
        post.setContent(request.getContent());
        if (request.getStatus() != null) post.setStatus(request.getStatus());
        if (request.getImageUrl() != null) post.setImageUrl(request.getImageUrl());
        if (request.getScheduledAt() != null) post.setScheduledAt(request.getScheduledAt());
        return mapToPostResponse(postRepository.save(post), user.getId());
    }

    @Override
    @Transactional
    public void deletePost(String email, Long postId) {
        User user = getUserByEmail(email);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        if (!post.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this post");
        }
        postRepository.delete(post);
        log.info("Post {} deleted by {}", postId, email);
    }

    @Override
    public PostResponse getPostById(String email, Long postId) {
        User user = getUserByEmail(email);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return mapToPostResponse(post, user.getId());
    }

    @Override
    public Page<PostResponse> getFeedPosts(String email, Pageable pageable) {
        User user = getUserByEmail(email);
        List<Long> connectionIds = connectionRepository.findConnectionUserIds(user.getId());
        connectionIds.add(user.getId());
        // Include PUBLISHED posts and SCHEDULED posts whose time has arrived
        return postRepository.findFeedPostsWithScheduled(connectionIds, LocalDateTime.now(), pageable)
                .map(p -> mapToPostResponse(p, user.getId()));
    }

    @Override
    public Page<PostResponse> getUserPosts(String email, Long userId, Pageable pageable) {
        User currentUser = getUserByEmail(email);
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(p -> mapToPostResponse(p, currentUser.getId()));
    }

    @Override
    public Page<PostResponse> getDraftPosts(String email, Pageable pageable) {
        User user = getUserByEmail(email);
        return postRepository.findByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), PostStatus.DRAFT, pageable)
                .map(p -> mapToPostResponse(p, user.getId()));
    }

    @Override
    @Transactional
    public PostResponse publishDraft(String email, Long postId) {
        User user = getUserByEmail(email);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        if (!post.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Not authorized");
        }
        if (post.getStatus() != PostStatus.DRAFT) {
            throw new BadRequestException("Post is not a draft");
        }
        post.setStatus(PostStatus.PUBLISHED);
        post.setScheduledAt(null);
        return mapToPostResponse(postRepository.save(post), user.getId());
    }

    @Override
    @Transactional
    public boolean toggleLike(String email, Long postId) {
        User user = getUserByEmail(email);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        return likeRepository.findByPostIdAndUserId(postId, user.getId())
                .map(like -> { likeRepository.delete(like); return false; })
                .orElseGet(() -> {
                    likeRepository.save(Like.builder().post(post).user(user).build());
                    if (!post.getUser().getId().equals(user.getId())) {
                        notificationRepository.save(Notification.builder()
                                .user(post.getUser())
                                .type(NotificationType.POST_LIKE)
                                .message(user.getFirstName() + " " + user.getLastName() + " liked your post")
                                .referenceId(postId)
                                .isRead(false)
                                .build());
                    }
                    return true;
                });
    }

    @Override
    @Transactional
    public CommentResponse addComment(String email, Long postId, CommentRequest request) {
        User user = getUserByEmail(email);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        Comment comment = Comment.builder()
                .post(post).user(user).content(request.getContent()).build();
        comment = commentRepository.save(comment);

        // Notify post owner
        if (!post.getUser().getId().equals(user.getId())) {
            notificationRepository.save(Notification.builder()
                    .user(post.getUser())
                    .type(NotificationType.POST_COMMENT)
                    .message(user.getFirstName() + " " + user.getLastName() + " commented on your post: \"" + request.getContent().substring(0, Math.min(50, request.getContent().length())) + "\"")
                    .referenceId(postId)
                    .isRead(false)
                    .build());
        }
        return mapToCommentResponse(comment);
    }

    @Override
    public Page<CommentResponse> getComments(Long postId, Pageable pageable) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable)
                .map(this::mapToCommentResponse);
    }

    @Override
    @Transactional
    public void deleteComment(String email, Long commentId) {
        User user = getUserByEmail(email);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this comment");
        }
        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public PostResponse sharePost(String email, Long postId, String additionalText) {
        User user = getUserByEmail(email);
        Post originalPost = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        String sharedContent = (additionalText != null && !additionalText.isBlank() ? additionalText + "\n\n" : "")
                + "🔁 Shared from " + originalPost.getUser().getFirstName() + " " + originalPost.getUser().getLastName() + ":\n"
                + originalPost.getContent();

        Post sharedPost = Post.builder()
                .user(user)
                .content(sharedContent)
                .postType(originalPost.getPostType())
                .status(PostStatus.PUBLISHED)
                .imageUrl(originalPost.getImageUrl())
                .hashtags(originalPost.getHashtags())
                .build();

        return mapToPostResponse(postRepository.save(sharedPost), user.getId());
    }

    private PostResponse mapToPostResponse(Post post, Long currentUserId) {
        List<String> hashtagNames = post.getHashtags() == null ? List.of() :
                post.getHashtags().stream().map(Hashtag::getTag).collect(Collectors.toList());
        return PostResponse.builder()
                .id(post.getId())
                .userId(post.getUser().getId())
                .userFirstName(post.getUser().getFirstName())
                .userLastName(post.getUser().getLastName())
                .userProfilePhotoUrl(post.getUser().getProfilePhotoUrl())
                .userHeadline(post.getUser().getHeadline())
                .content(post.getContent())
                .postType(post.getPostType())
                .status(post.getStatus())
                .scheduledAt(post.getScheduledAt())
                .imageUrl(post.getImageUrl())
                .hashtags(hashtagNames)
                .likeCount(likeRepository.countByPostId(post.getId()))
                .commentCount(commentRepository.countByPostId(post.getId()))
                .likedByCurrentUser(likeRepository.existsByPostIdAndUserId(post.getId(), currentUserId))
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    private CommentResponse mapToCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .userId(comment.getUser().getId())
                .userFirstName(comment.getUser().getFirstName())
                .userLastName(comment.getUser().getLastName())
                .userProfilePhotoUrl(comment.getUser().getProfilePhotoUrl())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
