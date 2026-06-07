package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.AppApplication;
import hexlet.code.dto.AuthRequest;
import hexlet.code.dto.TaskCreateDto;
import hexlet.code.model.Label;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
    private LabelRepository labelRepository;

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
        labelRepository.deleteAll();
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
                .andExpect(header().string("X-Total-Count", "1"))
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
    void testCreateTaskWithLabels() throws Exception {
        Label bugLabel = new Label();
        bugLabel.setName("bug");
        bugLabel = labelRepository.save(bugLabel);

        Label featureLabel = new Label();
        featureLabel.setName("feature");
        featureLabel = labelRepository.save(featureLabel);

        TaskCreateDto dto = new TaskCreateDto();
        dto.setTitle("Task with labels");
        dto.setStatus("draft");
        dto.setTaskLabelIds(List.of(bugLabel.getId(), featureLabel.getId()));

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskLabelIds", hasSize(2)))
                .andExpect(jsonPath("$.taskLabelIds[0]").value(bugLabel.getId().intValue()));
    }

    @Test
    void testUpdateTaskLabels() throws Exception {
        Label bugLabel = new Label();
        bugLabel.setName("bug");
        bugLabel = labelRepository.save(bugLabel);

        Map<String, Object> updates = new HashMap<>();
        updates.put("taskLabelIds", List.of(bugLabel.getId()));

        mockMvc.perform(put("/api/tasks/" + testTask.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskLabelIds", hasSize(1)))
                .andExpect(jsonPath("$.taskLabelIds[0]").value(bugLabel.getId().intValue()));
    }

    @Test
    void testFilterByTitleCont() throws Exception {
        Task matchingTask = new Task();
        matchingTask.setName("Create new version");
        matchingTask.setDescription("Description");
        matchingTask.setTaskStatus(draftStatus);
        matchingTask.setAssignee(testUser);
        taskRepository.save(matchingTask);

        mockMvc.perform(get("/api/tasks")
                        .param("titleCont", "create")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Create new version"));
    }

    @Test
    void testFilterByAssigneeId() throws Exception {
        User otherUser = new User();
        otherUser.setEmail("other@google.com");
        otherUser.setPassword(passwordEncoder.encode("password"));
        otherUser = userRepository.save(otherUser);

        Task otherTask = new Task();
        otherTask.setName("Other task");
        otherTask.setTaskStatus(draftStatus);
        otherTask.setAssignee(otherUser);
        taskRepository.save(otherTask);

        mockMvc.perform(get("/api/tasks")
                        .param("assigneeId", testUser.getId().toString())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Task 1"));
    }

    @Test
    void testFilterByStatus() throws Exception {
        TaskStatus reviewStatus = new TaskStatus();
        reviewStatus.setName("To Review");
        reviewStatus.setSlug("to_review");
        reviewStatus = taskStatusRepository.save(reviewStatus);

        Task reviewTask = new Task();
        reviewTask.setName("Review task");
        reviewTask.setTaskStatus(reviewStatus);
        reviewTask.setAssignee(testUser);
        taskRepository.save(reviewTask);

        mockMvc.perform(get("/api/tasks")
                        .param("status", "to_review")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Review task"))
                .andExpect(jsonPath("$[0].status").value("to_review"));
    }

    @Test
    void testFilterByLabelId() throws Exception {
        Label bugLabel = new Label();
        bugLabel.setName("bug");
        bugLabel = labelRepository.save(bugLabel);

        Task labeledTask = new Task();
        labeledTask.setName("Bug task");
        labeledTask.setTaskStatus(draftStatus);
        labeledTask.setAssignee(testUser);
        labeledTask.setLabels(Set.of(bugLabel));
        taskRepository.save(labeledTask);

        mockMvc.perform(get("/api/tasks")
                        .param("labelId", bugLabel.getId().toString())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Bug task"));
    }

    @Test
    void testFilterCombined() throws Exception {
        TaskStatus fixedStatus = new TaskStatus();
        fixedStatus.setName("To Be Fixed");
        fixedStatus.setSlug("to_be_fixed");
        fixedStatus = taskStatusRepository.save(fixedStatus);

        Label bugLabel = new Label();
        bugLabel.setName("bug");
        bugLabel = labelRepository.save(bugLabel);

        Task matchingTask = new Task();
        matchingTask.setName("Create new version");
        matchingTask.setDescription("Description of task");
        matchingTask.setIndex(3245);
        matchingTask.setTaskStatus(fixedStatus);
        matchingTask.setAssignee(testUser);
        matchingTask.setLabels(Set.of(bugLabel));
        taskRepository.save(matchingTask);

        mockMvc.perform(get("/api/tasks")
                        .param("titleCont", "create")
                        .param("assigneeId", testUser.getId().toString())
                        .param("status", "to_be_fixed")
                        .param("labelId", bugLabel.getId().toString())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Create new version"))
                .andExpect(jsonPath("$[0].status").value("to_be_fixed"))
                .andExpect(jsonPath("$[0].assignee_id").value(testUser.getId().intValue()))
                .andExpect(jsonPath("$[0].index").value(3245));
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
