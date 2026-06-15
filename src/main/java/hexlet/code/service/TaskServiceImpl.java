package hexlet.code.service;

import hexlet.code.dto.TaskCreateDto;
import hexlet.code.dto.TaskDto;
import hexlet.code.dto.TaskFilterParams;
import hexlet.code.dto.TaskUpdateDto;
import hexlet.code.exception.BadRequestException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.spec.TaskSpecifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final UserRepository userRepository;
    private final LabelRepository labelRepository;
    private final TaskMapper taskMapper;

    public TaskServiceImpl(
            TaskRepository taskRepository,
            TaskStatusRepository taskStatusRepository,
            UserRepository userRepository,
            LabelRepository labelRepository,
            TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskStatusRepository = taskStatusRepository;
        this.userRepository = userRepository;
        this.labelRepository = labelRepository;
        this.taskMapper = taskMapper;
    }

    @Override
    public List<TaskDto> findAll(TaskFilterParams filter) {
        return taskRepository.findAll(TaskSpecifications.withFilter(filter)).stream()
                .map(taskMapper::toDto)
                .toList();
    }

    @Override
    public TaskDto findById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
        return taskMapper.toDto(task);
    }

    @Override
    public TaskDto create(TaskCreateDto dto) {
        TaskStatus taskStatus = findTaskStatusBySlug(dto.getStatus());
        User assignee = findAssignee(dto.getAssigneeId());
        List<Label> labels = findLabels(dto.getTaskLabelIds());
        Task task = taskMapper.toEntity(dto, taskStatus, assignee, labels);
        Task saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }

    @Override
    public TaskDto update(Long id, TaskUpdateDto dto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));

        TaskStatus taskStatus = dto.getStatus() != null ? findTaskStatusBySlug(dto.getStatus()) : null;
        User assignee = dto.getAssigneeId() != null ? findAssignee(dto.getAssigneeId()) : null;

        taskMapper.applyPartialUpdate(dto, task, taskStatus, assignee);

        if (dto.getTaskLabelIds() != null) {
            task.getLabels().clear();
            task.getLabels().addAll(findLabels(dto.getTaskLabelIds()));
        }

        Task saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }

    @Override
    public void delete(Long id) {
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

    private List<Label> findLabels(List<Long> labelIds) {
        if (labelIds == null || labelIds.isEmpty()) {
            return List.of();
        }
        return labelRepository.findAllById(labelIds);
    }
}
