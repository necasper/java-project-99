package hexlet.code.service;

import hexlet.code.dto.UserCreateDto;
import hexlet.code.dto.UserDto;
import hexlet.code.dto.UserUpdateDto;

import java.util.List;

public interface UserService {

    List<UserDto> findAll();

    UserDto findById(Long id);

    UserDto create(UserCreateDto dto);

    UserDto update(Long id, UserUpdateDto dto);

    void delete(Long id);

    String getEmailById(Long id);
}
