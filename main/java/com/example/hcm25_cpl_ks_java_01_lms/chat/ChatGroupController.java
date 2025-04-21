package com.example.hcm25_cpl_ks_java_01_lms.chat;

import com.example.hcm25_cpl_ks_java_01_lms.common.Constants;
import com.example.hcm25_cpl_ks_java_01_lms.user.User;
import com.example.hcm25_cpl_ks_java_01_lms.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
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
@RequestMapping("/chat-group")
@PreAuthorize("permitAll()")
//@PreAuthorize("@customSecurityService.hasRoleForModule(authentication, 'Chat')")
public class ChatGroupController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationService conversationService;
    private final MessageService messageService;
    private final UserService userService;

    public ChatGroupController(SimpMessagingTemplate messagingTemplate, ConversationService conversationService, MessageService messageService, UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.conversationService = conversationService;
        this.messageService = messageService;
        this.userService = userService;
    }

    @GetMapping
    public String getChatGroup(Model model, Authentication authentication) {
        User currentAuthentication = (User) authentication.getPrincipal();
        User currentUser = userService.getUserById(currentAuthentication.getId());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("content", "chats/intro_group");
        return Constants.LAYOUT;
    }

    @GetMapping("/conversations")
    @ResponseBody
    public List<ConversationDTO> getGroupConversations(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "5") int limit,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        List<Conversation> conversations = conversationService.findGroupConversationsOfUser(currentUser);
        List<ConversationDTO> allConversationDTO = conversations.stream()
                .map(conversation -> {
                    Optional<Message> lastMessageOpt = messageService.findLastMessageByConversationId(conversation.getId(), currentUser.getId());
                    String lastMessage = lastMessageOpt.isPresent() ? lastMessageOpt.get().getText() : "No messages yet";
                    String lastMessageTime = lastMessageOpt.map(message -> message.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).orElse("11/11/1111 11:11");

                    return new ConversationDTO(
                            conversation.getId(),
                            conversation.getName(),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                            "",  // Không cần opponentName trong group chat, để rỗng
                            lastMessage,
                            lastMessageTime
                    );
                })
                .sorted(Comparator.comparing(ConversationDTO::lastMessageTime).reversed())
                .collect(Collectors.toList());

        int start = Math.min(offset, allConversationDTO.size());
        int end = Math.min(offset + limit, allConversationDTO.size());
        return allConversationDTO.subList(start, end);
    }

    @GetMapping("/{conversationId}")
    public String getChatConversation(@PathVariable Long conversationId, Model model, Authentication authentication) {
        User currentAuthentication = (User) authentication.getPrincipal();
        User currentUser = userService.getUserById(currentAuthentication.getId());

        Optional<Conversation> currentConversationOpt = conversationService.findConversationById(conversationId);
        Conversation conversation = currentConversationOpt.orElse(null);
        List<Message> messages = messageService.findMessageByConversationOfUserId(conversationId,currentUser.getId());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("currentConversation", conversation);
        model.addAttribute("messages", messages);
        model.addAttribute("content", "chats/group");

        return Constants.LAYOUT;
    }

    @MessageMapping("/chat-group.sendMessage")
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
        newMessage2.setTimestamp(message.getTimestamp());
        newMessage2.setId(newMessage.getId());

        messagingTemplate.convertAndSend("/chat-group/" + conversationId, newMessage2);
    }

    @GetMapping("/create")
    public String showCreateConversationForm(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "chats/create_conversation_group";
    }

    @PostMapping("/create")
    public String createConversation(@RequestParam String name, @RequestParam List<Long> userIds) {
        List<User> participantList = userService.getUsersByIds(userIds);
        conversationService.createConversationGroup(name, participantList);
        return "redirect:/chat-group";
    }

    @PostMapping("/delete/{conversationId}")
    @ResponseBody
    public ResponseEntity<?> deleteConversation(@PathVariable Long conversationId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        conversationService.deleteConversationForUser(conversationId, currentUser.getId());
        return ResponseEntity.ok().build();
    }
}
