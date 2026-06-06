package hexlet.code.service;

import hexlet.code.dto.TaskStatusCreateDto;
import hexlet.code.dto.TaskStatusDto;
import hexlet.code.dto.TaskStatusUpdateDto;
import hexlet.code.exception.BadRequestException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TaskStatusService {

    private final TaskStatusRepository taskStatusRepository;
    private final TaskStatusMapper taskStatusMapper;

    public TaskStatusService(TaskStatusRepository taskStatusRepository, TaskStatusMapper taskStatusMapper) {
        this.taskStatusRepository = taskStatusRepository;
        this.taskStatusMapper = taskStatusMapper;
    }

    public List<TaskStatusDto> findAll() {
        return taskStatusRepository.findAll().stream()
                .map(taskStatusMapper::toDto)
                .toList();
    }

    public TaskStatusDto findById(Long id) {
        TaskStatus taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with id " + id + " not found"));
        return taskStatusMapper.toDto(taskStatus);
    }

    public TaskStatusDto create(TaskStatusCreateDto dto) {
        if (taskStatusRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Name already exists");
        }
        if (taskStatusRepository.existsBySlug(dto.getSlug())) {
            throw new BadRequestException("Slug already exists");
        }
        TaskStatus taskStatus = taskStatusMapper.toEntity(dto);
        TaskStatus saved = taskStatusRepository.save(taskStatus);
        return taskStatusMapper.toDto(saved);
    }

    public TaskStatusDto update(Long id, TaskStatusUpdateDto dto) {
        TaskStatus taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with id " + id + " not found"));

        if (dto.getName() != null && !dto.getName().equals(taskStatus.getName())
                && taskStatusRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Name already exists");
        }
        if (dto.getSlug() != null && !dto.getSlug().equals(taskStatus.getSlug())
                && taskStatusRepository.existsBySlug(dto.getSlug())) {
            throw new BadRequestException("Slug already exists");
        }

        taskStatusMapper.applyPartialUpdate(dto, taskStatus);
        TaskStatus saved = taskStatusRepository.save(taskStatus);
        return taskStatusMapper.toDto(saved);
    }

    public void delete(Long id) {
        if (!taskStatusRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task status with id " + id + " not found");
        }
        taskStatusRepository.deleteById(id);
    }
}
