package com.linkedin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.linkedin.controller.UserController;
import com.linkedin.dto.request.EducationRequest;
import com.linkedin.dto.request.ExperienceRequest;
import com.linkedin.dto.request.ProfileUpdateRequest;
import com.linkedin.dto.request.SkillRequest;
import com.linkedin.dto.response.ApiResponse;
import com.linkedin.dto.response.EducationResponse;
import com.linkedin.dto.response.ExperienceResponse;
import com.linkedin.dto.response.SkillResponse;
import com.linkedin.dto.response.UserResponse;
import com.linkedin.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

@Mock
private UserService userService;

@InjectMocks
private UserController userController;

private UserDetails getUserDetails() {
    return User.withUsername("john.doe@example.com")
            .password("password")
            .authorities(Collections.emptyList())
            .build();
}

@Test
void getCurrentUser_Success() {
    UserDetails userDetails = getUserDetails();
    UserResponse userResponse = new UserResponse();

    when(userService.getCurrentUser("john.doe@example.com")).thenReturn(userResponse);

    ResponseEntity<ApiResponse<UserResponse>> response =
            userController.getCurrentUser(userDetails);

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("User fetched successfully", response.getBody().getMessage());
    assertEquals(userResponse, response.getBody().getData());
}

@Test
void getUserById_Success() {
    UserResponse userResponse = new UserResponse();

    when(userService.getUserById(1L)).thenReturn(userResponse);

    ResponseEntity<ApiResponse<UserResponse>> response =
            userController.getUserById(1L);

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("User fetched successfully", response.getBody().getMessage());
    assertEquals(userResponse, response.getBody().getData());
}

@Test
void updateProfile_Success() {
    UserDetails userDetails = getUserDetails();
    ProfileUpdateRequest request = new ProfileUpdateRequest();
    UserResponse userResponse = new UserResponse();

    when(userService.updateProfile("john.doe@example.com", request)).thenReturn(userResponse);

    ResponseEntity<ApiResponse<UserResponse>> response =
            userController.updateProfile(userDetails, request);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Profile updated successfully", response.getBody().getMessage());
    assertEquals(userResponse, response.getBody().getData());
}

@Test
void searchUsers_FiltersCurrentLoggedInUser() {
    UserDetails userDetails = getUserDetails();

    UserResponse currentUser = new UserResponse();
    currentUser.setEmail("john.doe@example.com");

    UserResponse otherUser = new UserResponse();
    otherUser.setEmail("jane.doe@example.com");

    when(userService.searchUsers("john")).thenReturn(List.of(currentUser, otherUser));

    ResponseEntity<ApiResponse<List<UserResponse>>> response =
            userController.searchUsers(userDetails, "john");

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("Users fetched successfully", response.getBody().getMessage());
    assertEquals(1, response.getBody().getData().size());
    assertEquals("jane.doe@example.com", response.getBody().getData().get(0).getEmail());
}

@Test
void addEducation_Success() {
    UserDetails userDetails = getUserDetails();
    EducationRequest request = new EducationRequest();
    EducationResponse educationResponse = new EducationResponse();

    when(userService.addEducation("john.doe@example.com", request)).thenReturn(educationResponse);

    ResponseEntity<ApiResponse<EducationResponse>> response =
            userController.addEducation(userDetails, request);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Education added successfully", response.getBody().getMessage());
    assertEquals(educationResponse, response.getBody().getData());
}

@Test
void updateEducation_Success() {
    UserDetails userDetails = getUserDetails();
    EducationRequest request = new EducationRequest();
    EducationResponse educationResponse = new EducationResponse();

    when(userService.updateEducation("john.doe@example.com", 1L, request)).thenReturn(educationResponse);

    ResponseEntity<ApiResponse<EducationResponse>> response =
            userController.updateEducation(userDetails, 1L, request);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Education updated successfully", response.getBody().getMessage());
    assertEquals(educationResponse, response.getBody().getData());
}

@Test
void deleteEducation_Success() {
    UserDetails userDetails = getUserDetails();

    doNothing().when(userService).deleteEducation("john.doe@example.com", 1L);

    ResponseEntity<ApiResponse<Void>> response =
            userController.deleteEducation(userDetails, 1L);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Education deleted successfully", response.getBody().getMessage());
    verify(userService).deleteEducation("john.doe@example.com", 1L);
}

@Test
void addExperience_Success() {
    UserDetails userDetails = getUserDetails();
    ExperienceRequest request = new ExperienceRequest();
    ExperienceResponse experienceResponse = new ExperienceResponse();

    when(userService.addExperience("john.doe@example.com", request)).thenReturn(experienceResponse);

    ResponseEntity<ApiResponse<ExperienceResponse>> response =
            userController.addExperience(userDetails, request);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Experience added successfully", response.getBody().getMessage());
    assertEquals(experienceResponse, response.getBody().getData());
}

@Test
void updateExperience_Success() {
    UserDetails userDetails = getUserDetails();
    ExperienceRequest request = new ExperienceRequest();
    ExperienceResponse experienceResponse = new ExperienceResponse();

    when(userService.updateExperience("john.doe@example.com", 1L, request)).thenReturn(experienceResponse);

    ResponseEntity<ApiResponse<ExperienceResponse>> response =
            userController.updateExperience(userDetails, 1L, request);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Experience updated successfully", response.getBody().getMessage());
    assertEquals(experienceResponse, response.getBody().getData());
}

@Test
void deleteExperience_Success() {
    UserDetails userDetails = getUserDetails();

    doNothing().when(userService).deleteExperience("john.doe@example.com", 1L);

    ResponseEntity<ApiResponse<Void>> response =
            userController.deleteExperience(userDetails, 1L);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Experience deleted successfully", response.getBody().getMessage());
    verify(userService).deleteExperience("john.doe@example.com", 1L);
}

@Test
void addSkill_Success() {
    UserDetails userDetails = getUserDetails();
    SkillRequest request = new SkillRequest();
    SkillResponse skillResponse = new SkillResponse();

    when(userService.addSkill("john.doe@example.com", request)).thenReturn(skillResponse);

    ResponseEntity<ApiResponse<SkillResponse>> response =
            userController.addSkill(userDetails, request);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Skill added successfully", response.getBody().getMessage());
    assertEquals(skillResponse, response.getBody().getData());
}

@Test
void deleteSkill_Success() {
    UserDetails userDetails = getUserDetails();

    doNothing().when(userService).deleteSkill("john.doe@example.com", 1L);

    ResponseEntity<ApiResponse<Void>> response =
            userController.deleteSkill(userDetails, 1L);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Skill deleted successfully", response.getBody().getMessage());
    verify(userService).deleteSkill("john.doe@example.com", 1L);
}

}
