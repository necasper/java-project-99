package hexlet.code.service;

import hexlet.code.dto.TaskCreateDto;
import hexlet.code.dto.TaskDto;
import hexlet.code.dto.TaskUpdateDto;
import hexlet.code.exception.BadRequestException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    public TaskService(
            TaskRepository taskRepository,
            TaskStatusRepository taskStatusRepository,
            UserRepository userRepository,
            TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskStatusRepository = taskStatusRepository;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
    }

    public List<TaskDto> findAll() {
        return taskRepository.findAll().stream()
                .map(taskMapper::toDto)
                .toList();
    }

    public TaskDto findById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
        return taskMapper.toDto(task);
    }

    public TaskDto create(TaskCreateDto dto) {
        TaskStatus taskStatus = findTaskStatusBySlug(dto.getStatus());
        User assignee = findAssignee(dto.getAssigneeId());
        Task task = taskMapper.toEntity(dto, taskStatus, assignee);
        Task saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }

    public TaskDto update(Long id, TaskUpdateDto dto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));

        TaskStatus taskStatus = dto.getStatus() != null ? findTaskStatusBySlug(dto.getStatus()) : null;
        User assignee = dto.getAssigneeId() != null ? findAssignee(dto.getAssigneeId()) : null;

        taskMapper.applyPartialUpdate(dto, task, taskStatus, assignee);
        Task saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }

    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task with id " + id + " not found");
        }
        taskRepository.deleteById(id);
    }

    private TaskStatus findTaskStatusBySlug(String slug) {
        return taskStatusRepository.findBySlug(slug)
                .orElseThrow(() -> new BadRequestException("Task status with slug " + slug + " not found"));
    }

    private User findAssignee(Long assigneeId) {
        if (assigneeId == null) {
            return null;
        }
        return userRepository.findById(assigneeId)
                .orElseThrow(() -> new BadRequestException("User with id " + assigneeId + " not found"));
    }
}
