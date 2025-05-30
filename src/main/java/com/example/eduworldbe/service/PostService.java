package com.example.eduworldbe.service;

import com.example.eduworldbe.dto.*;
import com.example.eduworldbe.model.Course;
import com.example.eduworldbe.model.Post;
import com.example.eduworldbe.repository.CourseRepository;
import com.example.eduworldbe.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class PostService {
  @Autowired
  private PostRepository postRepository;

  @Autowired
  private CourseRepository courseRepository;

  @Autowired
  private UserService userService;

  @Autowired
  private FileUploadService fileUploadService;

  @Transactional
  public PostDTO createPost(CreatePostRequest request, String userId) {
    Course course = courseRepository.findById(request.getCourseId())
        .orElseThrow(() -> new RuntimeException("Course not found"));

    boolean isTeacher = userId.equals(course.getTeacherId());
    boolean isTeacherAssistant = course.getTeacherAssistantIds().contains(userId);
    boolean isStudent = course.getStudentIds().contains(userId);

    if (!isTeacher && !isTeacherAssistant && !isStudent) {
      throw new RuntimeException("Not authorized to post in this course");
    }

    if (isStudent && !course.isAllowStudentPost()) {
      throw new RuntimeException("Students are not allowed to post in this course");
    }

    Post post = new Post();
    post.setContent(request.getContent());
    post.setImageUrls(request.getImageUrls());
    post.setCreatedAt(LocalDateTime.now());
    post.setUserId(userId);
    post.setCourseId(request.getCourseId());

    boolean autoApprove = !course.isRequirePostApproval() || isTeacher || isTeacherAssistant;
    post.setApproved(autoApprove);

    Post savedPost = postRepository.save(post);
    return convertToDTO(savedPost);
  }

  @Transactional
  public PostDTO updatePost(String postId, UpdatePostRequest request, String userId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new RuntimeException("Post not found"));

    if (!post.getUserId().equals(userId)) {
      throw new RuntimeException("Not authorized to update this post");
    }

    // Xóa những ảnh không còn được sử dụng
    fileUploadService.deleteUnusedFiles(post.getImageUrls(), request.getImageUrls());

    post.setContent(request.getContent());
    post.setImageUrls(request.getImageUrls());

    Post updatedPost = postRepository.save(post);
    return convertToDTO(updatedPost);
  }

  @Transactional
  public void deletePost(String postId, String userId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new RuntimeException("Post not found"));

    Course course = courseRepository.findById(post.getCourseId())
        .orElseThrow(() -> new RuntimeException("Course not found"));

    boolean isTeacher = userId.equals(course.getTeacherId());
    boolean isTeacherAssistant = course.getTeacherAssistantIds().contains(userId);
    boolean isPostOwner = post.getUserId().equals(userId);

    if (!isTeacher && !isTeacherAssistant && !isPostOwner) {
      throw new RuntimeException("Not authorized to delete this post");
    }

    // Xóa tất cả các ảnh của post
    fileUploadService.deleteFiles(post.getImageUrls());

    postRepository.delete(post);
  }

  @Transactional
  public PostDTO approvePost(String postId, ApprovePostRequest request, String userId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new RuntimeException("Post not found"));

    Course course = courseRepository.findById(post.getCourseId())
        .orElseThrow(() -> new RuntimeException("Course not found"));

    boolean isTeacher = userId.equals(course.getTeacherId());
    boolean isTeacherAssistant = course.getTeacherAssistantIds().contains(userId);

    if (!isTeacher && !isTeacherAssistant) {
      throw new RuntimeException("Not authorized to approve posts");
    }

    if (!request.isApproved()) {
      deletePost(postId, userId);
      return null;
    }

    post.setApproved(request.isApproved());
    Post updatedPost = postRepository.save(post);
    return convertToDTO(updatedPost);
  }

  public PostPageResponse getPostsByCourse(String courseId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Post> postPage = postRepository.findByCourseIdAndApprovedOrderByCreatedAtDesc(courseId, true, pageable);

    PostPageResponse response = new PostPageResponse();
    response.setPosts(postPage.getContent().stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList()));
    response.setCurrentPage(postPage.getNumber());
    response.setTotalPages(postPage.getTotalPages());
    response.setTotalElements(postPage.getTotalElements());
    response.setPageSize(postPage.getSize());

    return response;
  }

  public PostPageResponse getPendingPosts(String courseId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Post> postPage = postRepository.findByCourseIdAndApprovedOrderByCreatedAtDesc(courseId, false, pageable);

    PostPageResponse response = new PostPageResponse();
    response.setPosts(postPage.getContent().stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList()));
    response.setCurrentPage(postPage.getNumber());
    response.setTotalPages(postPage.getTotalPages());
    response.setTotalElements(postPage.getTotalElements());
    response.setPageSize(postPage.getSize());

    return response;
  }

  private PostDTO convertToDTO(Post post) {
    PostDTO dto = new PostDTO();
    dto.setId(post.getId());
    dto.setContent(post.getContent());
    dto.setImageUrls(post.getImageUrls());
    dto.setCreatedAt(post.getCreatedAt());
    dto.setUser(userService.getUserInfo(post.getUserId()));
    dto.setCourseId(post.getCourseId());
    dto.setApproved(post.isApproved());
    return dto;
  }
}