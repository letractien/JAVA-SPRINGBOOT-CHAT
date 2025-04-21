package com.example.hcm25_cpl_ks_java_01_lms.chat;

import com.example.hcm25_cpl_ks_java_01_lms.common.Constants;
import com.example.hcm25_cpl_ks_java_01_lms.user.User;
import com.example.hcm25_cpl_ks_java_01_lms.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/chat-personal")
@PreAuthorize("permitAll()")
//@PreAuthorize("@customSecurityService.hasRoleForModule(authentication, 'Chat')")
public class ChatPersonalController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationService conversationService;
    private final MessageService messageService;
    private final UserService userService;
    @Autowired
    private DeletedMessageRepository deletedMessageRepository;

    public ChatPersonalController(SimpMessagingTemplate messagingTemplate, ConversationService conversationService, MessageService messageService, UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.conversationService = conversationService;
        this.messageService = messageService;
        this.userService = userService;
    }

    @GetMapping
    public String getChatPersonal(Model model, Authentication authentication) {
        User currentAuthentication = (User) authentication.getPrincipal();
        User currentUser = userService.getUserById(currentAuthentication.getId());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("content", "chats/intro_personal");
        return Constants.LAYOUT;
    }

    @PostMapping("/start-conversation")
    @ResponseBody
    public ResponseEntity<Long> startConversation(
            @RequestParam Long opponentId,
            @RequestParam Long currentUserId) { // Thêm tham số currentUserId
        User currentUser = userService.getUserById(currentUserId);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User opponent = userService.getUserById(opponentId);
        if (opponent == null) {
            return ResponseEntity.badRequest().build();
        }

        Conversation conversation = conversationService.getOrCreateConversationPersonal(currentUser, opponent);
        return ResponseEntity.ok(conversation.getId());
    }

    @GetMapping("/conversations")
    @ResponseBody
    public Page<ConversationDTO> getConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return conversationService.getPersonalConversations(currentUser.getId(), page, size);
    }

    @GetMapping("/{conversationId}")
    public String getChatConversation(@PathVariable Long conversationId, Model model, Authentication authentication) {
        User currentAuthentication = (User) authentication.getPrincipal();
        User currentUser = userService.getUserById(currentAuthentication.getId());

        Conversation conversation = conversationService.getConversationById(conversationId);
        if (conversation == null || !conversation.getUsers().contains(currentUser)) {
            return "redirect:/chat-personal";
        }


        //if (currentUser == null || selectedUser == null || currentUser.getId().equals(selectedUser.getId())) {
        //    return "redirect:/chat-personal";
        //}

        User selectedUser = conversation.getUsers().stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .findFirst()
                .orElse(null);

        if (selectedUser == null) {
            return "redirect:/chat-personal";
        }
        String conversationName = selectedUser.getFirstName() + " " + selectedUser.getLastName();
        List<Message> messages = messageService.findMessageByConversationOfUserId(conversation.getId(),currentUser.getId());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("currentConversation", conversation);
        model.addAttribute("conversationName", conversationName);
        model.addAttribute("messages", messages);
        model.addAttribute("content", "chats/personal");

        return Constants.LAYOUT;
    }

    @GetMapping("/{conversationId}/messages")
    public String getConversationMessages(@PathVariable Long conversationId, Model model, Authentication authentication) {
        User currentAuthentication = (User) authentication.getPrincipal();
        User currentUser = userService.getUserById(currentAuthentication.getId());

        Conversation conversation = conversationService.getConversationById(conversationId);
        if (conversation == null || !conversation.getUsers().contains(currentUser)) {
            return "redirect:/chat-personal";
        }

        User selectedUser = conversation.getUsers().stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .findFirst()
                .orElse(null);

        if (selectedUser == null) {
            return "redirect:/chat-personal";
        }

        String conversationName = selectedUser.getFirstName() + " " + selectedUser.getLastName();
        List<Message> messages = messageService.findMessageByConversationOfUserId(conversation.getId(), currentUser.getId());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("currentConversation", conversation);
        model.addAttribute("conversationName", conversationName);
        model.addAttribute("messages", messages);

        return "chats/message_area :: messageAreaFragment";
    }

    @MessageMapping("/chat-personal.sendMessage")
    public void sendMessage(@Payload Message message) {
        Long userId = message.getUser().getId();
        Long conversationId = message.getConversation().getId();
        Message newMessage = messageService.saveMessage(message, userId, conversationId);

        Message newMessage2 = new Message();
        User user = new User();
        user.setId(newMessage.getUser().getId());
        user.setUsername(newMessage.getUser().getUsername());
        user.setFirstName(newMessage.getUser().getFirstName());
        user.setLastName(newMessage.getUser().getLastName());

        Conversation conversation = new Conversation();
        conversation.setId(newMessage.getConversation().getId());
        conversation.setName(newMessage.getConversation().getName());

        newMessage2.setUser(user);
        newMessage2.setConversation(conversation);
        newMessage2.setText(message.getText());
        newMessage2.setMediaUrl(message.getMediaUrl());
        newMessage2.setMediaType(message.getMediaType());
        newMessage2.setTimestamp(message.getTimestamp());
        newMessage2.setId(newMessage.getId());

        messagingTemplate.convertAndSend("/chat-personal/" + conversationId, newMessage2);
    }

    @PostMapping("/delete/{conversationId}")
    @ResponseBody
    public ResponseEntity<?> deleteConversation(@PathVariable Long conversationId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        conversationService.deleteConversationForUser(conversationId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @ResponseBody
    public List<UserDTO> searchUsers(
            @RequestParam(defaultValue = "") String searchTerm,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "8") int limit,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return userService.searchUsersByName(currentUser.getId(), searchTerm, offset, limit);
    }

}
