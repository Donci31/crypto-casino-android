package hu.bme.aut.crypto_casino_backend.controller;

import hu.bme.aut.crypto_casino_backend.dto.user.UserDto;
import hu.bme.aut.crypto_casino_backend.security.UserPrincipal;
import hu.bme.aut.crypto_casino_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping
	public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
		String currentUsername = currentUser.getUsername();

		UserDto userDto = userService.getUserByUsername(currentUsername);
		return ResponseEntity.ok(userDto);
	}

	@PutMapping
	public ResponseEntity<UserDto> updateUser(@RequestBody UserDto updateDto) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentUsername = authentication.getName();

		UserDto updatedUser = userService.updateUser(currentUsername, updateDto);
		return ResponseEntity.ok(updatedUser);
	}

}
