package com.linkedin.serviceImpl;

import com.linkedin.dto.request.*;
import com.linkedin.dto.response.*;
import com.linkedin.entity.*;
import com.linkedin.exception.ResourceNotFoundException;
import com.linkedin.exception.UnauthorizedException;
import com.linkedin.repository.*;
import com.linkedin.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EducationRepository educationRepository;
    private final ExperienceRepository experienceRepository;
    private final SkillRepository skillRepository;
    private final ModelMapper modelMapper;

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public UserResponse getCurrentUser(String email) {
        User user = getUserByEmail(email);
        return mapToUserResponse(user);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(String email, ProfileUpdateRequest request) {
        User user = getUserByEmail(email);
        if (request.getHeadline() != null) user.setHeadline(request.getHeadline());
        if (request.getLocation() != null) user.setLocation(request.getLocation());
        if (request.getSummary() != null) user.setSummary(request.getSummary());
        if (request.getProfilePhotoUrl() != null) user.setProfilePhotoUrl(request.getProfilePhotoUrl());
        return mapToUserResponse(userRepository.save(user));
    }

    @Override
    public List<UserResponse> searchUsers(String keyword) {
        // Bug fix 3: use DB-level search instead of loading all users into memory
        String trimmed = (keyword == null) ? "" : keyword.trim();
        return userRepository.searchUsers(trimmed).stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EducationResponse addEducation(String email, EducationRequest request) {
        User user = getUserByEmail(email);
        Education education = Education.builder()
                .user(user)
                .school(request.getSchool())
                .degree(request.getDegree())
                .fieldOfStudy(request.getFieldOfStudy())
                .startYear(request.getStartYear())
                .endYear(request.getEndYear())
                .description(request.getDescription())
                .build();
        education = educationRepository.save(education);
        return mapToEducationResponse(education);
    }

    @Override
    @Transactional
    public EducationResponse updateEducation(String email, Long educationId, EducationRequest request) {
        User user = getUserByEmail(email);
        Education education = educationRepository.findById(educationId)
                .orElseThrow(() -> new ResourceNotFoundException("Education not found"));
        if (!education.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to update this education");
        }
        education.setSchool(request.getSchool());
        education.setDegree(request.getDegree());
        education.setFieldOfStudy(request.getFieldOfStudy());
        education.setStartYear(request.getStartYear());
        education.setEndYear(request.getEndYear());
        education.setDescription(request.getDescription());
        return mapToEducationResponse(educationRepository.save(education));
    }

    @Override
    @Transactional
    public void deleteEducation(String email, Long educationId) {
        User user = getUserByEmail(email);
        Education education = educationRepository.findById(educationId)
                .orElseThrow(() -> new ResourceNotFoundException("Education not found"));
        if (!education.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this education");
        }
        educationRepository.delete(education);
    }

    @Override
    @Transactional
    public ExperienceResponse addExperience(String email, ExperienceRequest request) {
        User user = getUserByEmail(email);
        Experience experience = Experience.builder()
                .user(user)
                .title(request.getTitle())
                .company(request.getCompany())
                .location(request.getLocation())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isCurrent(request.getIsCurrent())
                .description(request.getDescription())
                .build();
        return mapToExperienceResponse(experienceRepository.save(experience));
    }

    @Override
    @Transactional
    public ExperienceResponse updateExperience(String email, Long experienceId, ExperienceRequest request) {
        User user = getUserByEmail(email);
        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience not found"));
        if (!experience.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to update this experience");
        }
        experience.setTitle(request.getTitle());
        experience.setCompany(request.getCompany());
        experience.setLocation(request.getLocation());
        experience.setStartDate(request.getStartDate());
        experience.setEndDate(request.getEndDate());
        experience.setIsCurrent(request.getIsCurrent());
        experience.setDescription(request.getDescription());
        return mapToExperienceResponse(experienceRepository.save(experience));
    }

    @Override
    @Transactional
    public void deleteExperience(String email, Long experienceId) {
        User user = getUserByEmail(email);
        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience not found"));
        if (!experience.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this experience");
        }
        experienceRepository.delete(experience);
    }

    @Override
    @Transactional
    public SkillResponse addSkill(String email, SkillRequest request) {
        User user = getUserByEmail(email);
        Skill skill = Skill.builder()
                .user(user)
                .skillName(request.getSkillName())
                .build();
        return mapToSkillResponse(skillRepository.save(skill));
    }

    @Override
    @Transactional
    public void deleteSkill(String email, Long skillId) {
        User user = getUserByEmail(email);
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));
        if (!skill.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this skill");
        }
        skillRepository.delete(skill);
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = modelMapper.map(user, UserResponse.class);
        response.setEducations(educationRepository.findByUserIdOrderByEndYearDesc(user.getId())
                .stream().map(this::mapToEducationResponse).collect(Collectors.toList()));
        response.setExperiences(experienceRepository.findByUserIdOrderByStartDateDesc(user.getId())
                .stream().map(this::mapToExperienceResponse).collect(Collectors.toList()));
        response.setSkills(skillRepository.findByUserId(user.getId())
                .stream().map(this::mapToSkillResponse).collect(Collectors.toList()));
        return response;
    }

    private EducationResponse mapToEducationResponse(Education e) {
        EducationResponse r = modelMapper.map(e, EducationResponse.class);
        r.setUserId(e.getUser().getId());
        return r;
    }

    private ExperienceResponse mapToExperienceResponse(Experience e) {
        ExperienceResponse r = modelMapper.map(e, ExperienceResponse.class);
        r.setUserId(e.getUser().getId());
        return r;
    }

    private SkillResponse mapToSkillResponse(Skill s) {
        SkillResponse r = modelMapper.map(s, SkillResponse.class);
        r.setUserId(s.getUser().getId());
        return r;
    }
}
