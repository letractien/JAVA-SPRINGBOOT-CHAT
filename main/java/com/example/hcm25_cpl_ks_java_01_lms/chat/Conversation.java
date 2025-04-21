package com.example.hcm25_cpl_ks_java_01_lms.chat;

import com.example.hcm25_cpl_ks_java_01_lms.classes.Classes;
import com.example.hcm25_cpl_ks_java_01_lms.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String name;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @OneToMany(mappedBy = "conversation", fetch = FetchType.LAZY)
    private Set<Message> messages = new HashSet<>();

    @ManyToMany(mappedBy = "conversations", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();
}