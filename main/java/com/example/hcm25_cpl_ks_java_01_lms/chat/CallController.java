package com.example.hcm25_cpl_ks_java_01_lms.chat;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class CallController {

    private final SimpMessagingTemplate messagingTemplate;

    public CallController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/call/{conversationId}")
    public void handleCall(@DestinationVariable String conversationId, WebRTCSignal signal) {
        // Gửi tín hiệu tới tất cả người dùng trong conversation
        messagingTemplate.convertAndSend("/topic/call/" + conversationId, signal);

        // Log để debug (tùy chọn)
        System.out.println("Signal sent to /topic/call/" + conversationId + ": " + signal.getType());
    }

    // Gửi tín hiệu đến một user cụ thể (nếu cần)
    public void sendSignal(String userId, WebRTCSignal signal) {
        messagingTemplate.convertAndSendToUser(userId, "/call", signal);
    }
}