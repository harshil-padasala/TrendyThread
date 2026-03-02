package com.trendythread.app.services.impl;

import com.trendythread.app.entities.User;
import com.trendythread.app.exceptions.ResourceNotFoundException;
import com.trendythread.app.dto.UserDto;
import com.trendythread.app.repositories.UserRepository;
import com.trendythread.app.services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public UserDto createUser(UserDto userDto) {
        log.info("createUser - request received: userDto={}", userDto);
        User user = this.dtoToUser(userDto);
        User savedUser = this.userRepository.save(user);
        UserDto result = this.userToDto(savedUser);
        log.info("createUser - user created: id={}", result.getId());
        return result;
    }

    @Override
    public UserDto updateByUserId(UserDto userDto, Integer id) {
        log.info("updateByUserId - request received: id={}, userDto={}", id, userDto);
        User updatedUser = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        updatedUser.setName(userDto.getName());
        updatedUser.setEmail(userDto.getEmail());
        updatedUser.setPassword(userDto.getPassword());
        updatedUser.setAbout(userDto.getAbout());

        this.userRepository.save(updatedUser);

        UserDto result = this.userToDto(updatedUser);
        log.info("updateByUserId - update successful: id={}", id);
        return result;
    }

    @Override
    public UserDto findByUserId(Integer userId) {
        log.info("findByUserId - request received: id={}", userId);
        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserDto result = this.userToDto(user);
        log.debug("findByUserId - fetched user: {}", result);
        return result;
    }

    @Override
    public List<UserDto> fetchAllUsers() {
        log.info("fetchAllUsers - request received");
        List<User> listOfUsers = this.userRepository.findAll();
        List<UserDto> dtoList = listOfUsers.stream().map(this::userToDto).toList();
        log.debug("fetchAllUsers - fetched {} users", dtoList.size());
        return dtoList;
    }

    @Override
    public void fetchByUserId(Integer id) {
        log.info("fetchByUserId (delete) - request received: id={}", id);
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Id", id));
        this.userRepository.delete(user);
        log.info("fetchByUserId (delete) - deleted user id={}", id);
    }

    private User dtoToUser(UserDto userDto) {
        return this.modelMapper.map(userDto, User.class);
    }

    private UserDto userToDto(User user) {
        return this.modelMapper.map(user, UserDto.class);
    }

}
