package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserGetDTO {

    private Long id;
    private String name;
    private String username;
    private UserStatus status;
    private LocalDateTime creationDate;
    private LocalDate birthDate;
    private String message; // New field to hold error messages
    private boolean loggedInUser; // New field to indicate whether the user is logged in

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreationDate() { // Getter for creation date
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) { // Setter for creation date
        this.creationDate = creationDate;
    }

    public LocalDate getBirthDate() { // Getter for birth date
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) { // Setter for birth date
        this.birthDate = birthDate;
    }

    public String getMessage() { // Getter for error message
        return message;
    }

    public void setMessage(String message) { // Setter for error message
        this.message = message;
    }

    public boolean isLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(boolean loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}


