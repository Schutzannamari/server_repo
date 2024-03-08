package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserProfileUpdateDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
public class UserController {

  private final UserService userService;

  UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/users")
  @ResponseStatus(HttpStatus.OK)
  public List<UserGetDTO> getAllUsers() {
    List<User> users = userService.getUsers();
    List<UserGetDTO> userGetDTOs = new ArrayList<>();

    for (User user : users) {
      userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
    }
    return userGetDTOs;
  }

  @PostMapping("/users")
  @ResponseStatus(HttpStatus.CREATED)
  public UserGetDTO registerUser(@RequestBody UserPostDTO userPostDTO) {
      if (userPostDTO.getUsername() == null || userPostDTO.getPassword() == null
              || userPostDTO.getUsername().isEmpty() || userPostDTO.getPassword().isEmpty()) {
          throw new IllegalArgumentException("Username and password cannot be empty");
      }
  
      if (userService.getUserByUsername(userPostDTO.getUsername()) != null) {
          throw new IllegalArgumentException("Username is already taken");
      }
  
      if (userService.getUserByName(userPostDTO.getName()) != null) {
          throw new IllegalArgumentException("Name is already taken");
      }
  
      User newUser = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
      newUser.setCreationDate(LocalDateTime.now());
      newUser.setName(userPostDTO.getName()); // Set the name
  
      User createdUser = userService.createUser(newUser);
  
      return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
  }

  @GetMapping("/users/{userid}")
  public ResponseEntity<UserGetDTO> getUserProfile(@PathVariable Long userid) { 
      User user = userService.getUserById(userid); 
  
      if (user == null) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      }
  
      // Determine if the fetched user is the currently authenticated user
      String authenticatedUsername = getAuthenticatedUsername();
      boolean loggedInUser = authenticatedUsername != null && authenticatedUsername.equals(user.getUsername());
  
      // Convert user entity to DTO and include loggedInUser field
      UserGetDTO userDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
      userDTO.setLoggedInUser(loggedInUser);
  
      return ResponseEntity.ok(userDTO);
  }
  

  @PutMapping("/users/{userid}/edit")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<UserGetDTO> updateProfile(@PathVariable Long id, @RequestBody UserProfileUpdateDTO updateDTO) { 
    // Fetch the user from the database
    User user = userService.getUserById(id); 
    if (user == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // User not found
    }

    // Verify that the authenticated user is the same as the user being updated
    String authenticatedUsername = getAuthenticatedUsername(); 
    if (!authenticatedUsername.equals(user.getUsername())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // User is not allowed to update other users' profiles
    }

    // Update the user data
    try {
        userService.updateUser(id, updateDTO.getUsername(), updateDTO.getBirthDate()); 
    } catch (IllegalArgumentException e) {
        // Handle any errors (e.g., username already taken)
        UserGetDTO errorDTO = new UserGetDTO();
        errorDTO.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDTO);
    }

    // Fetch the updated user from the database
    User updatedUser = userService.getUserById(id);

    // Convert the updated user entity to DTO and return it in the response
    UserGetDTO userDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(updatedUser);
    return ResponseEntity.ok(userDTO);
  }


  @PostMapping("/login")
  @ResponseStatus(HttpStatus.OK)
  public UserGetDTO loginUser(@RequestBody UserPostDTO userPostDTO) {
    if (userPostDTO.getUsername() == null || userPostDTO.getPassword() == null
            || userPostDTO.getUsername().isEmpty() || userPostDTO.getPassword().isEmpty()) {
        throw new IllegalArgumentException("Username and password cannot be empty");
    }

    User user = userService.getUserByUsername(userPostDTO.getUsername());

    if (user == null || !user.getPassword().equals(userPostDTO.getPassword())) {
        throw new IllegalArgumentException("Invalid username or password");
    }

    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.OK)
  public void logoutUser(HttpServletRequest request) {
    // Invalidate the user's session or token
    HttpSession session = request.getSession(false);
    if (session != null) {
        session.invalidate();
    }
  }

  private String getAuthenticatedUsername() {
    // Get the authentication object from the SecurityContextHolder
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // Check if the authentication object is not null and contains details
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
        // Retrieve the UserDetails object
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // Return the username of the authenticated user
        return userDetails.getUsername();
    } else {
        // If the authentication object is null or doesn't contain details, return null
        return null;
    }
  }
}

