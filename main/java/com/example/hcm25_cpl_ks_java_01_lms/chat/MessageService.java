package com.example.hcm25_cpl_ks_java_01_lms.chat;

import com.example.hcm25_cpl_ks_java_01_lms.user.User;
import com.example.hcm25_cpl_ks_java_01_lms.user.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ConversationService conversationService;


    public List<Message> findMessageByConversationOfUserId(Long conversationId, Long userId) {
        return messageRepository.findMessageByConversationOfUserId(conversationId, userId);
    }


    @Transactional
    public Message saveMessage(Message message, Long userId, Long conversationId) {
        User user = userService.getUserById(userId);
        Optional<Conversation> conversation = conversationService.findConversationById(conversationId);

        message.setText(message.getText());
        message.setMediaUrl(message.getMediaUrl());
        message.setMediaType(message.getMediaType());
        message.setTimestamp(LocalDateTime.now());
        message.setConversation(conversation.get());
        message.setUser(user);
        Message newMessage =  messageRepository.save(message);

        user.getMessages().add(newMessage);
        conversation.get().getMessages().add(newMessage);

        return newMessage;
    }

    public Conversation getConversationWithDetails(Long id) {
        return conversationRepository.findByIdWithAllDetails(id);
    }

    public Conversation getConversationById(Long id) {
        return conversationRepository.findById(id).orElse(null);
    }

    public Optional<Message> findLastMessageByConversationId(Long conversationId, Long userId) {
        Pageable pageable = PageRequest.of(0, 1);  // Lấy 1 tin nhắn cuối cùng
        List<Message> messages = messageRepository.findLastMessageByConversationId(conversationId, userId, pageable);
        return messages.isEmpty() ? Optional.empty() : Optional.of(messages.get(0));
    }

}
