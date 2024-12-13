package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserRepository userRepository;

	@Autowired
	private ObjectMapper objectMapper; // For converting objects to JSON

	private User user;

	@BeforeEach
	void setUp() {
		user = new User("John Doe", "john.doe@example.com");
		user.setId(1L);
	}

	@Test
	void getAllUsers_shouldReturnListOfUsers() throws Exception {
		Mockito.when(userRepository.findAll()).thenReturn(Arrays.asList(user));

		mockMvc.perform(get("/api/users")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name", is(user.getName())))
				.andExpect(jsonPath("$[0].email", is(user.getEmail())));
	}

	@Test
	void getUserById_shouldReturnUser() throws Exception {
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		mockMvc.perform(get("/api/users/1")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is(user.getName())))
				.andExpect(jsonPath("$.email", is(user.getEmail())));
	}

	@Test
	void getUserById_shouldReturnNotFound() throws Exception {
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/users/1")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	void createUser_shouldReturnCreatedUser() throws Exception {
		Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(user);

		mockMvc.perform(post("/api/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(user)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name", is(user.getName())))
				.andExpect(jsonPath("$.email", is(user.getEmail())));
	}

	@Test
	void createUser_shouldReturnConflictIfEmailExists() throws Exception {
		Mockito.when(userRepository.findByEmail(user.getEmail())).thenReturn(user);

		mockMvc.perform(post("/api/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(user)))
				.andExpect(status().isConflict());
	}

	@Test
	void updateUser_shouldUpdateAndReturnUser() throws Exception {
		User updatedUser = new User("Jane Doe", "jane.doe@example.com");
		updatedUser.setId(1L);

		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(updatedUser);

		mockMvc.perform(put("/api/users/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updatedUser)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is(updatedUser.getName())))
				.andExpect(jsonPath("$.email", is(updatedUser.getEmail())));
	}

	@Test
	void updateUser_shouldReturnNotFound() throws Exception {
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());

		User updatedUser = new User("Jane Doe", "jane.doe@example.com");

		mockMvc.perform(put("/api/users/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updatedUser)))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteUser_shouldReturnNoContent() throws Exception {
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		Mockito.doNothing().when(userRepository).delete(user);

		mockMvc.perform(delete("/api/users/1")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
	}

	@Test
	void deleteUser_shouldReturnNotFound() throws Exception {
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());

		mockMvc.perform(delete("/api/users/1")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}
}
