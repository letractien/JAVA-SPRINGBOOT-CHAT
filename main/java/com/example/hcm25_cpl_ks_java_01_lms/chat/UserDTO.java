package com.example.hcm25_cpl_ks_java_01_lms.chat;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;

    public UserDTO(Long id, String username, String firstName, String lastName) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}