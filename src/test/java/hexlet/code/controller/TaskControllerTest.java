package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.AppApplication;
import hexlet.code.dto.AuthRequest;
import hexlet.code.dto.TaskCreateDto;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
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
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AppApplication.class)
class TaskControllerTest extends BaseSpringBootTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    private TaskStatus draftStatus;

    private Task testTask;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("john@google.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser = userRepository.save(testUser);

        draftStatus = new TaskStatus();
        draftStatus.setName("Draft");
        draftStatus.setSlug("draft");
        draftStatus = taskStatusRepository.save(draftStatus);

        testTask = new Task();
        testTask.setName("Task 1");
        testTask.setDescription("Description of task 1");
        testTask.setIndex(3140);
        testTask.setTaskStatus(draftStatus);
        testTask.setAssignee(testUser);
        testTask = taskRepository.save(testTask);

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
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetAllTasks() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[0].content").value("Description of task 1"))
                .andExpect(jsonPath("$[0].status").value("draft"))
                .andExpect(jsonPath("$[0].assignee_id").value(testUser.getId().intValue()))
                .andExpect(jsonPath("$[0].index").value(3140));
    }

    @Test
    void testGetTaskById() throws Exception {
        mockMvc.perform(get("/api/tasks/" + testTask.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTask.getId()))
                .andExpect(jsonPath("$.title").value("Task 1"))
                .andExpect(jsonPath("$.content").value("Description of task 1"))
                .andExpect(jsonPath("$.status").value("draft"))
                .andExpect(jsonPath("$.assignee_id").value(testUser.getId().intValue()))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void testGetTaskNotFound() throws Exception {
        mockMvc.perform(get("/api/tasks/999")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateTask() throws Exception {
        TaskCreateDto dto = new TaskCreateDto();
        dto.setIndex(12);
        dto.setAssigneeId(testUser.getId());
        dto.setTitle("Test title");
        dto.setContent("Test content");
        dto.setStatus("draft");

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Test title"))
                .andExpect(jsonPath("$.content").value("Test content"))
                .andExpect(jsonPath("$.status").value("draft"))
                .andExpect(jsonPath("$.assignee_id").value(testUser.getId().intValue()))
                .andExpect(jsonPath("$.index").value(12))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void testCreateTaskInvalid() throws Exception {
        TaskCreateDto dto = new TaskCreateDto();
        dto.setTitle("");
        dto.setStatus("");

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateTaskPartial() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", "New title");
        updates.put("content", "New content");

        mockMvc.perform(put("/api/tasks/" + testTask.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New title"))
                .andExpect(jsonPath("$.content").value("New content"))
                .andExpect(jsonPath("$.status").value("draft"));

        Task updated = taskRepository.findById(testTask.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("New title");
        assertThat(updated.getDescription()).isEqualTo("New content");
    }

    @Test
    void testDeleteTask() throws Exception {
        mockMvc.perform(delete("/api/tasks/" + testTask.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.existsById(testTask.getId())).isFalse();
    }

    @Test
    void testUnauthorizedCreateWithoutToken() throws Exception {
        TaskCreateDto dto = new TaskCreateDto();
        dto.setTitle("Test title");
        dto.setStatus("draft");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
