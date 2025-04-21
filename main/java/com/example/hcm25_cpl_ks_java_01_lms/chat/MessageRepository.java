package com.example.hcm25_cpl_ks_java_01_lms.chat;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    //List<Message> findMessageByConversation_Id(Long conversationId);

    @Query("SELECT m FROM Message m " +
            "WHERE m.conversation.id = :conversationId " +
            "AND NOT EXISTS (SELECT d FROM DeletedMessage d WHERE d.messageId = m.id AND d.userId = :userId)")
    List<Message> findMessageByConversationOfUserId(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    // Thêm phương thức mới để lấy tất cả tin nhắn trong cuộc trò chuyện
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId")
    List<Message> findAllMessagesByConversationId(@Param("conversationId") Long conversationId);

    // Long conversation_id(Long conversationId);

    @Query("SELECT m FROM Message m " +
            "WHERE m.conversation.id = :conversationId " +
            "AND NOT EXISTS (SELECT d FROM DeletedMessage d WHERE d.messageId = m.id AND d.userId = :userId) " +
            "ORDER BY m.timestamp DESC")
    List<Message> findLastMessageByConversationId(@Param("conversationId") Long conversationId,
                                                  @Param("userId") Long userId,
                                                  Pageable pageable);

}