package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import hexlet.code.AppApplication;
import hexlet.code.dto.AuthRequest;
import hexlet.code.dto.LabelCreateDto;
import hexlet.code.dto.LabelDto;
import hexlet.code.mapper.LabelMapper;
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
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AppApplication.class)
class LabelControllerTest extends BaseSpringBootTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LabelMapper labelMapper;

    private Label testLabel;

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

        testLabel = new Label();
        testLabel.setName("bug");
        testLabel = labelRepository.save(testLabel);

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
        mockMvc.perform(get("/api/labels"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetAllLabels() throws Exception {
        var response = mockMvc.perform(get("/api/labels")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();

        List<LabelDto> labelDtos = objectMapper.readValue(body, new TypeReference<>() { });
        var actual = labelDtos.stream().map(labelMapper::map).toList();
        var expected = labelRepository.findAll();

        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void testGetLabelById() throws Exception {
        mockMvc.perform(get("/api/labels/" + testLabel.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testLabel.getId()))
                .andExpect(jsonPath("$.name").value("bug"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void testGetLabelNotFound() throws Exception {
        mockMvc.perform(get("/api/labels/999")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateLabel() throws Exception {
        LabelCreateDto dto = new LabelCreateDto();
        dto.setName("feature");

        mockMvc.perform(post("/api/labels")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("feature"))
                .andExpect(jsonPath("$.createdAt").exists());

        assertThat(labelRepository.findByName("feature")).isPresent();
    }

    @Test
    void testCreateLabelInvalid() throws Exception {
        LabelCreateDto dto = new LabelCreateDto();
        dto.setName("ab");

        mockMvc.perform(post("/api/labels")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateLabelDuplicateName() throws Exception {
        LabelCreateDto dto = new LabelCreateDto();
        dto.setName("bug");

        mockMvc.perform(post("/api/labels")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateLabel() throws Exception {
        Map<String, String> updates = new HashMap<>();
        updates.put("name", "Bug");

        mockMvc.perform(put("/api/labels/" + testLabel.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bug"));

        Label updated = labelRepository.findById(testLabel.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Bug");
    }

    @Test
    void testDeleteLabel() throws Exception {
        mockMvc.perform(delete("/api/labels/" + testLabel.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        assertThat(labelRepository.existsById(testLabel.getId())).isFalse();
    }

    @Test
    void testDeleteLabelWithAssignedTasks() throws Exception {
        TaskStatus status = new TaskStatus();
        status.setName("Draft");
        status.setSlug("draft");
        status = taskStatusRepository.save(status);

        Task task = new Task();
        task.setName("Task");
        task.setTaskStatus(status);
        task.setLabels(Set.of(testLabel));
        taskRepository.save(task);

        mockMvc.perform(delete("/api/labels/" + testLabel.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUnauthorizedCreateWithoutToken() throws Exception {
        LabelCreateDto dto = new LabelCreateDto();
        dto.setName("new label");

        mockMvc.perform(post("/api/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
