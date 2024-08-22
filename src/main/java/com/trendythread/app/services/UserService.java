package com.trendythread.app.services;

import com.trendythread.app.dto.UserDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    UserDto createUser(UserDto userDto);

    UserDto updateByUserId(UserDto userDto, Integer id);

    UserDto findByUserId(Integer userId);

    List<UserDto> fetchAllUsers();

    void fetchByUserId(Integer id);
}
