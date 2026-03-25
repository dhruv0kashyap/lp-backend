package com.linkedin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.linkedin.dto.request.EducationRequest;
import com.linkedin.dto.request.ExperienceRequest;
import com.linkedin.dto.request.ProfileUpdateRequest;
import com.linkedin.dto.request.SkillRequest;
import com.linkedin.dto.response.EducationResponse;
import com.linkedin.dto.response.ExperienceResponse;
import com.linkedin.dto.response.SkillResponse;
import com.linkedin.dto.response.UserResponse;
import com.linkedin.entity.Education;
import com.linkedin.entity.Experience;
import com.linkedin.entity.Skill;
import com.linkedin.entity.User;
import com.linkedin.exception.ResourceNotFoundException;
import com.linkedin.exception.UnauthorizedException;
import com.linkedin.repository.EducationRepository;
import com.linkedin.repository.ExperienceRepository;
import com.linkedin.repository.SkillRepository;
import com.linkedin.repository.UserRepository;
import com.linkedin.serviceImpl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

@Mock
private UserRepository userRepository;
@Mock
private EducationRepository educationRepository;
@Mock
private ExperienceRepository experienceRepository;
@Mock
private SkillRepository skillRepository;
@Mock
private ModelMapper modelMapper;

@InjectMocks
private UserServiceImpl userService;

private User buildUser(Long id, String email) {
    return User.builder()
            .id(id)
            .email(email)
            .firstName("John")
            .lastName("Doe")
            .headline("Developer")
            .location("Bangalore")
            .summary("Test summary")
            .profilePhotoUrl("photo.jpg")
            .build();
}

private UserResponse buildUserResponse(Long id, String email) {
    UserResponse response = new UserResponse();
    response.setId(id);
    response.setEmail(email);
    response.setFirstName("John");
    response.setLastName("Doe");
    response.setHeadline("Developer");
    response.setLocation("Bangalore");
    response.setSummary("Test summary");
    response.setProfilePhotoUrl("photo.jpg");
    return response;
}

private Education buildEducation(Long id, User user) {
    return Education.builder()
            .id(id)
            .user(user)
            .school("RNSIT")
            .degree("B.E")
            .fieldOfStudy("CSE")
            .startYear(2021)
            .endYear(2025)
            .description("Engineering")
            .build();
}

private Experience buildExperience(Long id, User user) {
    return Experience.builder()
            .id(id)
            .user(user)
            .title("Intern")
            .company("ABC")
            .location("Bangalore")
            .startDate(LocalDate.of(2024, 1, 1))
            .endDate(LocalDate.of(2024, 6, 1))
            .isCurrent(false)
            .description("Worked on backend")
            .build();
}

private Skill buildSkill(Long id, User user) {
    return Skill.builder()
            .id(id)
            .user(user)
            .skillName("Java")
            .build();
}

@Test
void getCurrentUser_Success() {
    User user = buildUser(1L, "john@example.com");
    UserResponse mapped = buildUserResponse(1L, "john@example.com");

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(modelMapper.map(user, UserResponse.class)).thenReturn(mapped);
    when(educationRepository.findByUserIdOrderByEndYearDesc(1L)).thenReturn(List.of());
    when(experienceRepository.findByUserIdOrderByStartDateDesc(1L)).thenReturn(List.of());
    when(skillRepository.findByUserId(1L)).thenReturn(List.of());

    UserResponse response = userService.getCurrentUser("john@example.com");

    assertNotNull(response);
    assertEquals("john@example.com", response.getEmail());
}

@Test
void getCurrentUser_UserNotFound_ThrowsException() {
    when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
            () -> userService.getCurrentUser("missing@example.com"));
}

@Test
void getUserById_Success() {
    User user = buildUser(1L, "john@example.com");
    UserResponse mapped = buildUserResponse(1L, "john@example.com");

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(modelMapper.map(user, UserResponse.class)).thenReturn(mapped);
    when(educationRepository.findByUserIdOrderByEndYearDesc(1L)).thenReturn(List.of());
    when(experienceRepository.findByUserIdOrderByStartDateDesc(1L)).thenReturn(List.of());
    when(skillRepository.findByUserId(1L)).thenReturn(List.of());

    UserResponse response = userService.getUserById(1L);

    assertNotNull(response);
    assertEquals(1L, response.getId());
}

@Test
void getUserById_NotFound_ThrowsException() {
    when(userRepository.findById(10L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
            () -> userService.getUserById(10L));
}

@Test
void updateProfile_Success() {
    User user = buildUser(1L, "john@example.com");
    UserResponse mapped = buildUserResponse(1L, "john@example.com");

    ProfileUpdateRequest request = new ProfileUpdateRequest();
    request.setHeadline("Senior Developer");
    request.setLocation("Hyderabad");
    request.setSummary("Updated summary");
    request.setProfilePhotoUrl("newphoto.jpg");

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(userRepository.save(user)).thenReturn(user);
    when(modelMapper.map(user, UserResponse.class)).thenReturn(mapped);
    when(educationRepository.findByUserIdOrderByEndYearDesc(1L)).thenReturn(List.of());
    when(experienceRepository.findByUserIdOrderByStartDateDesc(1L)).thenReturn(List.of());
    when(skillRepository.findByUserId(1L)).thenReturn(List.of());

    UserResponse response = userService.updateProfile("john@example.com", request);

    assertNotNull(response);
    assertEquals("Senior Developer", user.getHeadline());
    assertEquals("Hyderabad", user.getLocation());
    assertEquals("Updated summary", user.getSummary());
    assertEquals("newphoto.jpg", user.getProfilePhotoUrl());
}

@Test
void searchUsers_Success() {
    User user1 = buildUser(1L, "john@example.com");
    User user2 = buildUser(2L, "jane@example.com");

    UserResponse response1 = buildUserResponse(1L, "john@example.com");
    UserResponse response2 = buildUserResponse(2L, "jane@example.com");

    when(userRepository.searchUsers("java")).thenReturn(List.of(user1, user2));
    when(modelMapper.map(user1, UserResponse.class)).thenReturn(response1);
    when(modelMapper.map(user2, UserResponse.class)).thenReturn(response2);
    when(educationRepository.findByUserIdOrderByEndYearDesc(anyLong())).thenReturn(List.of());
    when(experienceRepository.findByUserIdOrderByStartDateDesc(anyLong())).thenReturn(List.of());
    when(skillRepository.findByUserId(anyLong())).thenReturn(List.of());

    List<UserResponse> results = userService.searchUsers("java");

    assertEquals(2, results.size());
    verify(userRepository).searchUsers("java");
}

@Test
void searchUsers_NullKeyword_UsesEmptyString() {
    when(userRepository.searchUsers("")).thenReturn(List.of());

    List<UserResponse> results = userService.searchUsers(null);

    assertNotNull(results);
    assertTrue(results.isEmpty());
    verify(userRepository).searchUsers("");
}

@Test
void addEducation_Success() {
    User user = buildUser(1L, "john@example.com");
    Education education = buildEducation(11L, user);

    EducationRequest request = new EducationRequest();
    request.setSchool("RNSIT");
    request.setDegree("B.E");
    request.setFieldOfStudy("CSE");
    request.setStartYear(2021);
    request.setEndYear(2025);
    request.setDescription("Engineering");

    EducationResponse mapped = new EducationResponse();
    mapped.setId(11L);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(educationRepository.save(any(Education.class))).thenReturn(education);
    when(modelMapper.map(education, EducationResponse.class)).thenReturn(mapped);

    EducationResponse response = userService.addEducation("john@example.com", request);

    assertNotNull(response);
    assertEquals(11L, response.getId());
}

@Test
void updateEducation_Success() {
    User user = buildUser(1L, "john@example.com");
    Education education = buildEducation(11L, user);

    EducationRequest request = new EducationRequest();
    request.setSchool("IIT");
    request.setDegree("M.Tech");
    request.setFieldOfStudy("AI");
    request.setStartYear(2026);
    request.setEndYear(2028);
    request.setDescription("Masters");

    EducationResponse mapped = new EducationResponse();
    mapped.setId(11L);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(educationRepository.findById(11L)).thenReturn(Optional.of(education));
    when(educationRepository.save(education)).thenReturn(education);
    when(modelMapper.map(education, EducationResponse.class)).thenReturn(mapped);

    EducationResponse response = userService.updateEducation("john@example.com", 11L, request);

    assertNotNull(response);
    assertEquals("IIT", education.getSchool());
    assertEquals("M.Tech", education.getDegree());
}

@Test
void updateEducation_Unauthorized_ThrowsException() {
    User currentUser = buildUser(1L, "john@example.com");
    User anotherUser = buildUser(2L, "jane@example.com");
    Education education = buildEducation(11L, anotherUser);

    EducationRequest request = new EducationRequest();

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(currentUser));
    when(educationRepository.findById(11L)).thenReturn(Optional.of(education));

    assertThrows(UnauthorizedException.class,
            () -> userService.updateEducation("john@example.com", 11L, request));
}

@Test
void deleteEducation_Success() {
    User user = buildUser(1L, "john@example.com");
    Education education = buildEducation(11L, user);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(educationRepository.findById(11L)).thenReturn(Optional.of(education));

    userService.deleteEducation("john@example.com", 11L);

    verify(educationRepository).delete(education);
}

@Test
void addExperience_Success() {
    User user = buildUser(1L, "john@example.com");
    Experience experience = buildExperience(21L, user);

    ExperienceRequest request = new ExperienceRequest();
    request.setTitle("Intern");
    request.setCompany("ABC");
    request.setLocation("Bangalore");
    request.setStartDate(LocalDate.of(2024, 1, 1));
    request.setEndDate(LocalDate.of(2024, 6, 1));
    request.setIsCurrent(false);
    request.setDescription("Worked on backend");

    ExperienceResponse mapped = new ExperienceResponse();
    mapped.setId(21L);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(experienceRepository.save(any(Experience.class))).thenReturn(experience);
    when(modelMapper.map(experience, ExperienceResponse.class)).thenReturn(mapped);

    ExperienceResponse response = userService.addExperience("john@example.com", request);

    assertNotNull(response);
    assertEquals(21L, response.getId());
}

@Test
void updateExperience_Success() {
    User user = buildUser(1L, "john@example.com");
    Experience experience = buildExperience(21L, user);

    ExperienceRequest request = new ExperienceRequest();
    request.setTitle("Software Engineer");
    request.setCompany("XYZ");
    request.setLocation("Hyderabad");
    request.setStartDate(LocalDate.of(2025, 1, 1));
    request.setEndDate(LocalDate.of(2025, 12, 31));
    request.setIsCurrent(true);
    request.setDescription("Worked on microservices");

    ExperienceResponse mapped = new ExperienceResponse();
    mapped.setId(21L);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(experienceRepository.findById(21L)).thenReturn(Optional.of(experience));
    when(experienceRepository.save(experience)).thenReturn(experience);
    when(modelMapper.map(experience, ExperienceResponse.class)).thenReturn(mapped);

    ExperienceResponse response = userService.updateExperience("john@example.com", 21L, request);

    assertNotNull(response);
    assertEquals("Software Engineer", experience.getTitle());
    assertEquals("XYZ", experience.getCompany());
}

@Test
void updateExperience_Unauthorized_ThrowsException() {
    User currentUser = buildUser(1L, "john@example.com");
    User anotherUser = buildUser(2L, "jane@example.com");
    Experience experience = buildExperience(21L, anotherUser);

    ExperienceRequest request = new ExperienceRequest();

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(currentUser));
    when(experienceRepository.findById(21L)).thenReturn(Optional.of(experience));

    assertThrows(UnauthorizedException.class,
            () -> userService.updateExperience("john@example.com", 21L, request));
}

@Test
void deleteExperience_Success() {
    User user = buildUser(1L, "john@example.com");
    Experience experience = buildExperience(21L, user);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(experienceRepository.findById(21L)).thenReturn(Optional.of(experience));

    userService.deleteExperience("john@example.com", 21L);

    verify(experienceRepository).delete(experience);
}

@Test
void addSkill_Success() {
    User user = buildUser(1L, "john@example.com");
    Skill skill = buildSkill(31L, user);

    SkillRequest request = new SkillRequest();
    request.setSkillName("Java");

    SkillResponse mapped = new SkillResponse();
    mapped.setId(31L);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(skillRepository.save(any(Skill.class))).thenReturn(skill);
    when(modelMapper.map(skill, SkillResponse.class)).thenReturn(mapped);

    SkillResponse response = userService.addSkill("john@example.com", request);

    assertNotNull(response);
    assertEquals(31L, response.getId());
}

@Test
void deleteSkill_Success() {
    User user = buildUser(1L, "john@example.com");
    Skill skill = buildSkill(31L, user);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(skillRepository.findById(31L)).thenReturn(Optional.of(skill));

    userService.deleteSkill("john@example.com", 31L);

    verify(skillRepository).delete(skill);
}

@Test
void deleteSkill_Unauthorized_ThrowsException() {
    User currentUser = buildUser(1L, "john@example.com");
    User anotherUser = buildUser(2L, "jane@example.com");
    Skill skill = buildSkill(31L, anotherUser);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(currentUser));
    when(skillRepository.findById(31L)).thenReturn(Optional.of(skill));

    assertThrows(UnauthorizedException.class,
            () -> userService.deleteSkill("john@example.com", 31L));
}

}