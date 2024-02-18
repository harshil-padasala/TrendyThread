package com.blog.app.services.impl;

import com.blog.app.entities.User;
import com.blog.app.exceptions.ResourceNotFoundException;
import com.blog.app.exceptions.UserAlreadyExistException;
import com.blog.app.payloads.UserDto;
import com.blog.app.repositories.UserRepository;
import com.blog.app.services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public UserDto createUser(UserDto userDto) {
        if ((userRepository.findByEmail(userDto.getEmail())).isEmpty()) {

            User user = this.dtoToUser(userDto);
            User savedUser = this.userRepository.save(user);
            return this.userToDto(savedUser);
        }
        else {
            throw new UserAlreadyExistException("User", "email", userDto.getEmail());
        }
    }

    @Override
    public UserDto updateUser(UserDto userDto, Integer id) {
        User updatedUser = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        updatedUser.setName(userDto.getName());
        updatedUser.setEmail(userDto.getEmail());
        updatedUser.setPassword(userDto.getPassword());
        updatedUser.setAbout(userDto.getAbout());

        this.userRepository.save(updatedUser);

        return this.userToDto(updatedUser);
    }

    @Override
    public UserDto getUserById(Integer userId) {
        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return this.userToDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> listOfUsers = this.userRepository.findAll();
//        List<UserDto> userDtoList = listOfUsers.stream().map(user ->
//                this.userToDto(user)
//        ).collect(Collectors.toList());
//        return userDtoList;
        return listOfUsers.stream().map(this::userToDto).toList();
    }

    @Override
    public void deleteUser(Integer id) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Id", id));
        this.userRepository.delete(user);
    }

    private User dtoToUser(UserDto userDto) {
//        user.setUserId(userDto.getId());
//        user.setName(userDto.getName());
//        user.setEmail(userDto.getEmail());
//        user.setPassword(userDto.getPassword());
//        user.setAbout(userDto.getAbout());

        return this.modelMapper.map(userDto, User.class);
    }

    private UserDto userToDto(User user) {
//        UserDto userDto = new UserDto();
//        userDto.setId(user.getUserId());
//        userDto.setName(user.getName());
//        userDto.setEmail(user.getEmail());
//        userDto.setPassword(user.getPassword());
//        userDto.setAbout(user.getAbout());
        return this.modelMapper.map(user, UserDto.class);
    }

}
