package com.example.hcm25_cpl_ks_java_01_lms.chat;

import java.time.LocalDateTime;

public record ConversationDTO (
        Long id,
        String name,
        String timestamp,
        String opponentName,
        String lastMessage,
        String lastMessageTime
) {}