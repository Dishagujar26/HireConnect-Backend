package com.hireconnect.profileservice.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireconnect.profileservice.dto.ParsedResumeDto;
import com.hireconnect.profileservice.dto.request.ProfileRequestDto;
import com.hireconnect.profileservice.dto.response.CandidateFullProfileDto;
import com.hireconnect.profileservice.dto.response.CandidateProfilePreviewDto;
import com.hireconnect.profileservice.dto.response.ProfileResponseDto;
import com.hireconnect.profileservice.entity.Resume;
import com.hireconnect.profileservice.entity.Role;
import com.hireconnect.profileservice.security.AuthenticatedUser;
import com.hireconnect.profileservice.service.ProfileService;
import com.hireconnect.profileservice.service.ResumeParserService;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProfileControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProfileService profileService;

    @Mock
    private ResumeParserService resumeParserService;

    @InjectMocks
    private ProfileController profileController;

    private ObjectMapper objectMapper;
    private AuthenticatedUser candidateUser;
    private AuthenticatedUser recruiterUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        candidateUser = new AuthenticatedUser(1L, Role.CANDIDATE);
        recruiterUser = new AuthenticatedUser(2L, Role.RECRUITER);

        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders
                .standaloneSetup(profileController)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter,
                                                  ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest,
                                                  WebDataBinderFactory binderFactory) {
                        String role = webRequest.getHeader("role");
                        return "RECRUITER".equals(role) ? recruiterUser : candidateUser;
                    }
                })
                .build();
    }

    @Test
    void createProfile_ShouldReturnCreated() throws Exception {
        ProfileRequestDto request = new ProfileRequestDto();
        request.setFirstName("John");
        request.setLastName("Doe");

        ProfileResponseDto response = ProfileResponseDto.builder()
                .id(1L)
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .build();

        when(profileService.createProfile(anyLong(), any(Role.class), any(ProfileRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void getMyProfile_ShouldReturnOk() throws Exception {
        ProfileResponseDto response = ProfileResponseDto.builder()
                .id(1L)
                .userId(1L)
                .firstName("John")
                .build();

        when(profileService.getProfileByUserId(any(AuthenticatedUser.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/profiles/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void updateMyProfile_ShouldReturnOk() throws Exception {
        ProfileRequestDto request = new ProfileRequestDto();
        request.setFirstName("Updated");

        ProfileResponseDto response = ProfileResponseDto.builder()
                .id(1L)
                .userId(1L)
                .firstName("Updated")
                .build();

        when(profileService.updateProfile(any(AuthenticatedUser.class), any(ProfileRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/profiles/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));
    }

    @Test
    void getCandidatePreview_ShouldReturnOk() throws Exception {
        CandidateProfilePreviewDto response = CandidateProfilePreviewDto.builder()
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .headline("Java Developer")
                .build();

        when(profileService.getCandidateProfilePreviewByUserId(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/profiles/internal/candidates/1/preview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void uploadResume_ShouldReturnOk() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "dummy".getBytes()
        );

        when(profileService.uploadResume(any(AuthenticatedUser.class), any()))
                .thenReturn("Resume uploaded successfully");

        mockMvc.perform(multipart("/api/profiles/resume/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Resume uploaded successfully"));
    }

    @Test
    void downloadMyResume_ShouldReturnOk() throws Exception {
        Resume resume = new Resume();
        resume.setFileName("resume.pdf");
        resume.setContentType("application/pdf");
        resume.setFileData("pdf-data".getBytes());

        when(profileService.getMyResume(any(AuthenticatedUser.class)))
                .thenReturn(resume);

        mockMvc.perform(get("/api/profiles/resume/my"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"resume.pdf\""));
    }

    @Test
    void downloadResumeForRecruiter_ShouldReturnOk() throws Exception {
        Resume resume = new Resume();
        resume.setFileName("candidate.pdf");
        resume.setContentType("application/pdf");
        resume.setFileData("pdf-data".getBytes());

        when(profileService.getResumeForRecruiter(any(AuthenticatedUser.class), eq(1L), eq(10L)))
                .thenReturn(resume);

        mockMvc.perform(get("/api/profiles/resume/recruiter/1/10")
                        .header("role", "RECRUITER"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"candidate.pdf\""));
    }

    @Test
    void getCandidateFullProfile_ShouldReturnOk() throws Exception {
        CandidateFullProfileDto response = new CandidateFullProfileDto();

        when(profileService.getCandidateFullProfile(any(AuthenticatedUser.class), eq(1L), eq(10L)))
                .thenReturn(response);

        mockMvc.perform(get("/api/profiles/recruiter/candidates/1/full")
                        .param("jobId", "10")
                        .header("role", "RECRUITER"))
                .andExpect(status().isOk());
    }

    @Test
    void parseMyResume_ShouldReturnOk() throws Exception {
        Resume resume = new Resume();
        resume.setFileData("pdf-data".getBytes());

        ParsedResumeDto parsed = ParsedResumeDto.builder()
                .extractedSkills(java.util.List.of("Java", "Spring Boot"))
                .build();

        when(profileService.getMyResume(any(AuthenticatedUser.class)))
                .thenReturn(resume);

        when(resumeParserService.parsePdfResume(any()))
                .thenReturn(parsed);

        mockMvc.perform(post("/api/profiles/resume/parse"))
                .andExpect(status().isOk());
    }

    @Test
    void parseMyResume_NoResume_ShouldThrowException() {
        when(profileService.getMyResume(any(AuthenticatedUser.class)))
                .thenReturn(null);

        assertThrows(Exception.class, () -> {
                mockMvc.perform(post("/api/profiles/resume/parse"))
                        .andReturn();
        });
   }

   @Test
void parseMyResume_ResumeFileDataNull_ShouldThrowException() {
    Resume resume = new Resume();
    resume.setFileData(null);

    when(profileService.getMyResume(any(AuthenticatedUser.class)))
            .thenReturn(resume);

    assertThrows(Exception.class, () -> {
        mockMvc.perform(post("/api/profiles/resume/parse"))
                .andReturn();
    });
}

@Test
void createProfile_ShouldCallServiceWithAuthenticatedUser() throws Exception {
    ProfileRequestDto request = new ProfileRequestDto();
    request.setFirstName("Disha");

    ProfileResponseDto response = ProfileResponseDto.builder()
            .id(10L)
            .userId(1L)
            .firstName("Disha")
            .build();

    when(profileService.createProfile(eq(1L), eq(Role.CANDIDATE), any(ProfileRequestDto.class)))
            .thenReturn(response);

    mockMvc.perform(post("/api/profiles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").value(1L));

    verify(profileService).createProfile(eq(1L), eq(Role.CANDIDATE), any(ProfileRequestDto.class));
}

@Test
void updateMyProfile_ShouldCallService() throws Exception {
    ProfileRequestDto request = new ProfileRequestDto();
    request.setFirstName("Updated");

    ProfileResponseDto response = ProfileResponseDto.builder()
            .id(1L)
            .firstName("Updated")
            .build();

    when(profileService.updateProfile(any(AuthenticatedUser.class), any(ProfileRequestDto.class)))
            .thenReturn(response);

    mockMvc.perform(put("/api/profiles/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Updated"));

    verify(profileService).updateProfile(any(AuthenticatedUser.class), any(ProfileRequestDto.class));
}

@Test
void downloadMyResume_ShouldReturnPdfContent() throws Exception {
    byte[] fileData = "resume-content".getBytes();

    Resume resume = new Resume();
    resume.setFileName("my-resume.pdf");
    resume.setContentType("application/pdf");
    resume.setFileData(fileData);

    when(profileService.getMyResume(any(AuthenticatedUser.class)))
            .thenReturn(resume);

    mockMvc.perform(get("/api/profiles/resume/my"))
            .andExpect(status().isOk())
            .andExpect(content().bytes(fileData))
            .andExpect(content().contentType("application/pdf"));
}

@Test
void downloadResumeForRecruiter_ShouldReturnPdfContent() throws Exception {
    byte[] fileData = "candidate-resume-content".getBytes();

    Resume resume = new Resume();
    resume.setFileName("candidate-resume.pdf");
    resume.setContentType("application/pdf");
    resume.setFileData(fileData);

    when(profileService.getResumeForRecruiter(any(AuthenticatedUser.class), eq(1L), eq(10L)))
            .thenReturn(resume);

    mockMvc.perform(get("/api/profiles/resume/recruiter/1/10")
                    .header("role", "RECRUITER"))
            .andExpect(status().isOk())
            .andExpect(content().bytes(fileData))
            .andExpect(content().contentType("application/pdf"));
}
}