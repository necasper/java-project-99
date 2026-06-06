package hexlet.code.service;

import hexlet.code.dto.LabelCreateDto;
import hexlet.code.dto.LabelDto;
import hexlet.code.dto.LabelUpdateDto;
import hexlet.code.exception.BadRequestException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LabelService {

    private final LabelRepository labelRepository;
    private final TaskRepository taskRepository;
    private final LabelMapper labelMapper;

    public LabelService(LabelRepository labelRepository, TaskRepository taskRepository, LabelMapper labelMapper) {
        this.labelRepository = labelRepository;
        this.taskRepository = taskRepository;
        this.labelMapper = labelMapper;
    }

    public List<LabelDto> findAll() {
        return labelRepository.findAll().stream()
                .map(labelMapper::toDto)
                .toList();
    }

    public LabelDto findById(Long id) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label with id " + id + " not found"));
        return labelMapper.toDto(label);
    }

    public LabelDto create(LabelCreateDto dto) {
        if (labelRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Name already exists");
        }
        Label label = labelMapper.toEntity(dto);
        Label saved = labelRepository.save(label);
        return labelMapper.toDto(saved);
    }

    public LabelDto update(Long id, LabelUpdateDto dto) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label with id " + id + " not found"));

        if (dto.getName() != null && !dto.getName().equals(label.getName())
                && labelRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Name already exists");
        }

        labelMapper.applyPartialUpdate(dto, label);
        Label saved = labelRepository.save(label);
        return labelMapper.toDto(saved);
    }

    public void delete(Long id) {
        if (!labelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Label with id " + id + " not found");
        }
        if (taskRepository.existsByLabelsId(id)) {
            throw new BadRequestException("Cannot delete label with assigned tasks");
        }
        labelRepository.deleteById(id);
    }
}
