package com.example.hcm25_cpl_ks_java_01_lms.chat;

import com.example.hcm25_cpl_ks_java_01_lms.classes.Classes;
import com.example.hcm25_cpl_ks_java_01_lms.classes.ClassesRepository;
import com.example.hcm25_cpl_ks_java_01_lms.user.User;
import com.example.hcm25_cpl_ks_java_01_lms.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConversationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;
    @Autowired
    private ClassesRepository classesRepository;
    @Autowired
    private DeletedMessageRepository deletedMessageRepository;
    @Autowired
    private MessageRepository messageRepository;


    public List<Conversation> findPersonalConversationsOfUser(User user) {
        return conversationRepository.findPersonalConversationOfUser(user);
    }

    public List<Conversation> findGroupConversationsOfUser(User user) {
        return conversationRepository.findGroupConversationOfUser(user);
    }

    public Optional<Conversation> findConversationById(Long id) {
        return conversationRepository.findById(id);
    }

    @Transactional
    public Conversation createConversationGroup(String name, List<User> users) {
        if (users.isEmpty()) {
            throw new IllegalArgumentException("Users cannot be null");
        }

        // Tạo Conversation mới
        Conversation newConversation = new Conversation();
        newConversation.setName(name);
        newConversation.setTimestamp(LocalDateTime.now());

        // Lưu Conversation trước để nó trở thành persistent
        newConversation = conversationRepository.save(newConversation);

        // Thêm Conversation vào User và lưu User
        for (User user : users) {
            user.getConversations().add(newConversation);
            userRepository.save(user);
        }

        return newConversation;
    }
    public void createConversationGroup(Classes classes) {
        Conversation newConversation = new Conversation();
        newConversation.setName(classes.getName().toString());
        newConversation.setTimestamp(LocalDateTime.now());
        newConversation = conversationRepository.save(newConversation);

        // Cập nhật trường conversation trong lớp với id của Conversation mới
        classes.setConversation(newConversation);
        // Lưu lại lớp với conversation đã được gán
        classesRepository.save(classes);
    }


        @Transactional
    public void addUserToConversation(Long classId, Long userId) {
        // Tìm lớp học theo ID
        Classes classes = classesRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Class not found with id: " + classId));

        // Kiểm tra xem lớp học có Conversation chưa
        Conversation conversation = classes.getConversation();
        if (conversation == null) {
            throw new IllegalArgumentException("No conversation found for the class with id: " + classId);
        }

        // Tìm User theo ID
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
        User user = optionalUser.get();

        // Kiểm tra xem User đã có trong Conversation chưa
        if (!conversation.getUsers().contains(user)) {
            // Nếu chưa có, thêm User vào Conversation
            conversation.getUsers().add(user);
            user.getConversations().add(conversation); // Nếu cần thiết, thêm Conversation vào User

            // Lưu lại các thay đổi
            conversationRepository.save(conversation);
            userRepository.save(user);
        } else {
            // Nếu User đã có trong Conversation, bỏ qua
            System.out.println("User already exists in the conversation.");
        }
    }

    public void addUsersToConversation(Long classId, List<Long> userIds) {
        // Tìm lớp học theo ID
        Classes classes = classesRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Class not found with id: " + classId));

        // Kiểm tra xem lớp học có Conversation chưa
        Conversation conversation = classes.getConversation();
        if (conversation == null) {
            throw new IllegalArgumentException("No conversation found for the class with id: " + classId);
        }

        // Tìm User theo ID
        List<User> users = userRepository.findAllById(userIds);

        for (User user : users) {
            // Kiểm tra xem User đã có trong Conversation chưa
            if (!conversation.getUsers().contains(user)) {
                // Nếu chưa có, thêm User vào Conversation
                conversation.getUsers().add(user);
                user.getConversations().add(conversation); // Nếu cần thiết, thêm Conversation vào User

                // Lưu lại các thay đổi
                conversationRepository.save(conversation);
                userRepository.save(user);
            } else {
                // Nếu User đã có trong Conversation, bỏ qua
                System.out.println("User already exists in the conversation.");
            }
        }
    }



    @Transactional
    public Conversation getOrCreateConversationPersonal(User user1, User user2) {
        if (user1 == null || user2 == null) {
            throw new IllegalArgumentException("Users cannot be null");
        }

        Optional<Conversation> existingConversations = conversationRepository.findPersonalConversationBetween(user1, user2);
        if (existingConversations.isPresent()) {
            return existingConversations.get();
        }

        // Tạo Conversation mới
        Conversation newConversation = new Conversation();
        newConversation.setTimestamp(LocalDateTime.now());

        // Lưu Conversation trước để nó trở thành persistent
        newConversation = conversationRepository.save(newConversation);

        // Thêm Conversation vào User và lưu User
        user1.getConversations().add(newConversation);
        user2.getConversations().add(newConversation);
        userRepository.save(user1);
        userRepository.save(user2);

        return newConversation;
    }

    public Conversation getConversationById(Long id) {
        return conversationRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deleteConversationForUser(Long conversationId, Long userId) {
        // Lấy tất cả tin nhắn trong cuộc trò chuyện
        List<Message> messages = messageRepository.findAllMessagesByConversationId(conversationId);
        for (Message message : messages) {
            DeletedMessage deletedMessage = new DeletedMessage();
            deletedMessage.setMessageId(message.getId());
            deletedMessage.setUserId(userId);
            deletedMessage.setDeletedAt(LocalDateTime.now());
            deletedMessageRepository.save(deletedMessage);
        }
    }

    @Transactional
    public List<ConversationDTO> searchConversationsByName(User currentUser, String searchTerm, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);  // Phân trang
        List<Conversation> conversations = conversationRepository.searchConversationsByOpponentName(
                currentUser.getId(), searchTerm.toLowerCase(), pageable);

        return conversations.stream()
                .map(conversation -> {
                    User opponent = conversation.getUsers().stream()
                            .filter(user -> !user.getId().equals(currentUser.getId()))
                            .findFirst().orElse(null);
                    String opponentName = (opponent != null) ? opponent.getFirstName() + " " + opponent.getLastName() : "Unknown";
                    Optional<Message> lastMessageOpt = conversation.getMessages().stream().reduce((first, second) -> second);
                    String lastMessage = lastMessageOpt.map(Message::getText).orElse("No messages yet");
                    String lastMessageTime = lastMessageOpt.map(message -> message.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).orElse("11/11/1111 11:11");

                    return new ConversationDTO(
                            conversation.getId(),
                            conversation.getName(),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                            opponentName,
                            lastMessage,
                            lastMessageTime
                    );
                })
                .sorted(Comparator.comparing(ConversationDTO::opponentName)) // Sắp xếp theo tên đối thủ tăng dần
                .collect(Collectors.toList());
    }

    public Page<ConversationDTO> getPersonalConversations(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        return conversationRepository.findPersonalConversationsWithMessages(userId, currentTime, pageable);
    }
}
