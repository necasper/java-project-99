package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.AppApplication;
import hexlet.code.dto.AuthRequest;
import hexlet.code.dto.UserCreateDto;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.support.BaseSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest(classes = AppApplication.class)
class UserControllerTest extends BaseSpringBootTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        taskRepository.deleteAll();
        labelRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();
        testUser = new User();
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john@google.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser = userRepository.save(testUser);
        authToken = login("john@google.com", "password");
    }

    private String login(String email, String password) throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(email);
        authRequest.setPassword(password);

        return mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .trim();
    }

    @Test
    void testUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email").value("john@google.com"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void testGetUserById() throws Exception {
        mockMvc.perform(get("/api/users/" + testUser.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email").value("john@google.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void testGetUserNotFound() throws Exception {
        mockMvc.perform(get("/api/users/999")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateUser() throws Exception {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("jack@google.com");
        dto.setFirstName("Jack");
        dto.setLastName("Jons");
        dto.setPassword("some-password");

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("jack@google.com"))
                .andExpect(jsonPath("$.firstName").value("Jack"))
                .andExpect(jsonPath("$.lastName").value("Jons"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.password").doesNotExist());

        User saved = userRepository.findByEmail("jack@google.com").orElseThrow();
        assertThat(passwordEncoder.matches("some-password", saved.getPassword())).isTrue();
    }

    @Test
    void testCreateUserInvalid() throws Exception {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("invalid-email");
        dto.setPassword("ab");

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateUserPartial() throws Exception {
        Map<String, String> updates = new HashMap<>();
        updates.put("email", "jack@yahoo.com");
        updates.put("password", "new-password");

        mockMvc.perform(put("/api/users/" + testUser.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jack@yahoo.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.password").doesNotExist());

        User updated = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updated.getEmail()).isEqualTo("jack@yahoo.com");
        assertThat(passwordEncoder.matches("new-password", updated.getPassword())).isTrue();
    }

    @Test
    void testUpdateOtherUserForbidden() throws Exception {
        User other = new User();
        other.setEmail("other@google.com");
        other.setPassword(passwordEncoder.encode("password"));
        other = userRepository.save(other);

        Map<String, String> updates = new HashMap<>();
        updates.put("firstName", "Hacker");

        mockMvc.perform(put("/api/users/" + other.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/" + testUser.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(testUser.getId())).isFalse();
    }

    @Test
    void testDeleteOtherUserForbidden() throws Exception {
        User other = new User();
        other.setEmail("other@google.com");
        other.setPassword(passwordEncoder.encode("password"));
        other = userRepository.save(other);

        mockMvc.perform(delete("/api/users/" + other.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteUserWithAssignedTasks() throws Exception {
        TaskStatus status = new TaskStatus();
        status.setName("Draft");
        status.setSlug("draft");
        status = taskStatusRepository.save(status);

        Task task = new Task();
        task.setName("Task");
        task.setTaskStatus(status);
        task.setAssignee(testUser);
        taskRepository.save(task);

        mockMvc.perform(delete("/api/users/" + testUser.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testResponseDoesNotContainPassword() throws Exception {
        String response = mockMvc.perform(get("/api/users/" + testUser.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).doesNotContain("password");
    }
}
