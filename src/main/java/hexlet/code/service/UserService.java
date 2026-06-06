package hexlet.code.service;

import hexlet.code.dto.UserCreateDto;
import hexlet.code.dto.UserDto;
import hexlet.code.dto.UserUpdateDto;
import hexlet.code.exception.BadRequestException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            TaskRepository taskRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public String getEmailById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"))
                .getEmail();
    }

    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    public UserDto findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        return userMapper.toDto(user);
    }

    public UserDto create(UserCreateDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    public UserDto update(Long id, UserUpdateDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));

        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        userMapper.applyPartialUpdate(dto, user);

        if (dto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User with id " + id + " not found");
        }
        if (taskRepository.existsByAssigneeId(id)) {
            throw new BadRequestException("Cannot delete user with assigned tasks");
        }
        userRepository.deleteById(id);
    }
}
