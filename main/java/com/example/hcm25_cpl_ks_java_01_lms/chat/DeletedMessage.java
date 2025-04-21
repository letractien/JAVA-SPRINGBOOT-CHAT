package com.example.hcm25_cpl_ks_java_01_lms.chat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class DeletedMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}