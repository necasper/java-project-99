package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import hexlet.code.AppApplication;
import hexlet.code.dto.AuthRequest;
import hexlet.code.dto.TaskStatusCreateDto;
import hexlet.code.dto.TaskStatusDto;
import hexlet.code.mapper.TaskStatusMapper;
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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AppApplication.class)
class TaskStatusControllerTest extends BaseSpringBootTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TaskStatusMapper taskStatusMapper;

    private TaskStatus testStatus;

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

        User testUser = new User();
        testUser.setEmail("john@google.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        userRepository.save(testUser);

        testStatus = new TaskStatus();
        testStatus.setName("Draft");
        testStatus.setSlug("draft");
        testStatus = taskStatusRepository.save(testStatus);

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
        mockMvc.perform(get("/api/task_statuses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetAllTaskStatuses() throws Exception {
        var response = mockMvc.perform(get("/api/task_statuses")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();

        List<TaskStatusDto> taskStatusDtos = objectMapper.readValue(body, new TypeReference<>() { });
        var actual = taskStatusDtos.stream().map(taskStatusMapper::map).toList();
        var expected = taskStatusRepository.findAll();

        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void testGetTaskStatusById() throws Exception {
        mockMvc.perform(get("/api/task_statuses/" + testStatus.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testStatus.getId()))
                .andExpect(jsonPath("$.name").value("Draft"))
                .andExpect(jsonPath("$.slug").value("draft"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void testGetTaskStatusNotFound() throws Exception {
        mockMvc.perform(get("/api/task_statuses/999")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateTaskStatus() throws Exception {
        TaskStatusCreateDto dto = new TaskStatusCreateDto();
        dto.setName("ToReview");
        dto.setSlug("to_review");

        mockMvc.perform(post("/api/task_statuses")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("ToReview"))
                .andExpect(jsonPath("$.slug").value("to_review"))
                .andExpect(jsonPath("$.createdAt").exists());

        TaskStatus saved = taskStatusRepository.findBySlug("to_review").orElseThrow();
        assertThat(saved.getName()).isEqualTo("ToReview");
    }

    @Test
    void testCreateTaskStatusInvalid() throws Exception {
        TaskStatusCreateDto dto = new TaskStatusCreateDto();
        dto.setName("");
        dto.setSlug("");

        mockMvc.perform(post("/api/task_statuses")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTaskStatusDuplicateSlug() throws Exception {
        TaskStatusCreateDto dto = new TaskStatusCreateDto();
        dto.setName("Another Draft");
        dto.setSlug("draft");

        mockMvc.perform(post("/api/task_statuses")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateTaskStatusPartial() throws Exception {
        Map<String, String> updates = new HashMap<>();
        updates.put("name", "newStatus");

        mockMvc.perform(put("/api/task_statuses/" + testStatus.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("newStatus"))
                .andExpect(jsonPath("$.slug").value("draft"));

        TaskStatus updated = taskStatusRepository.findById(testStatus.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("newStatus");
    }

    @Test
    void testDeleteTaskStatus() throws Exception {
        mockMvc.perform(delete("/api/task_statuses/" + testStatus.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        assertThat(taskStatusRepository.existsById(testStatus.getId())).isFalse();
    }

    @Test
    void testDeleteTaskStatusWithAssignedTasks() throws Exception {
        User assignee = new User();
        assignee.setEmail("assignee@google.com");
        assignee.setPassword(passwordEncoder.encode("password"));
        assignee = userRepository.save(assignee);

        Task task = new Task();
        task.setName("Task");
        task.setTaskStatus(testStatus);
        task.setAssignee(assignee);
        taskRepository.save(task);

        mockMvc.perform(delete("/api/task_statuses/" + testStatus.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUnauthorizedCreateWithoutToken() throws Exception {
        TaskStatusCreateDto dto = new TaskStatusCreateDto();
        dto.setName("New");
        dto.setSlug("new");

        mockMvc.perform(post("/api/task_statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
