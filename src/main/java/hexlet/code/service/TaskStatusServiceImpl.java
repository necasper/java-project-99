package hexlet.code.service;

import hexlet.code.dto.TaskStatusCreateDto;
import hexlet.code.dto.TaskStatusDto;
import hexlet.code.dto.TaskStatusUpdateDto;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TaskStatusServiceImpl implements TaskStatusService {

    private final TaskStatusRepository taskStatusRepository;
    private final TaskStatusMapper taskStatusMapper;

    public TaskStatusServiceImpl(TaskStatusRepository taskStatusRepository, TaskStatusMapper taskStatusMapper) {
        this.taskStatusRepository = taskStatusRepository;
        this.taskStatusMapper = taskStatusMapper;
    }

    @Override
    public List<TaskStatusDto> findAll() {
        return taskStatusRepository.findAll().stream()
                .map(taskStatusMapper::toDto)
                .toList();
    }

    @Override
    public TaskStatusDto findById(Long id) {
        TaskStatus taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with id " + id + " not found"));
        return taskStatusMapper.toDto(taskStatus);
    }

    @Override
    public TaskStatusDto create(TaskStatusCreateDto dto) {
        TaskStatus taskStatus = taskStatusMapper.toEntity(dto);
        TaskStatus saved = taskStatusRepository.save(taskStatus);
        return taskStatusMapper.toDto(saved);
    }

    @Override
    public TaskStatusDto update(Long id, TaskStatusUpdateDto dto) {
        TaskStatus taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with id " + id + " not found"));

        taskStatusMapper.applyPartialUpdate(dto, taskStatus);
        TaskStatus saved = taskStatusRepository.save(taskStatus);
        return taskStatusMapper.toDto(saved);
    }

    @Override
    public void delete(Long id) {
        taskStatusRepository.deleteById(id);
    }
}
