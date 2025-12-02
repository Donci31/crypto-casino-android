package hu.bme.aut.crypto_casino_backend.service;

import hu.bme.aut.crypto_casino_backend.dto.user.UserDto;
import hu.bme.aut.crypto_casino_backend.exception.ResourceNotFoundException;
import hu.bme.aut.crypto_casino_backend.mapper.UserMapper;
import hu.bme.aut.crypto_casino_backend.model.User;
import hu.bme.aut.crypto_casino_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final UserMapper userMapper;

  public UserDto getUserByUsername(String username) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    return userMapper.toDto(user);
  }

  public UserDto updateUser(String username, UserDto userDto) {
    User existingUser = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + username));

    userMapper.updateFromDto(userDto, existingUser);

    if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
      existingUser.setPasswordHash(passwordEncoder.encode(userDto.getPassword()));
    }

    User updated = userRepository.save(existingUser);
    return userMapper.toDto(updated);
  }

}
