package com.example.hcm25_cpl_ks_java_01_lms.chat;

import com.example.hcm25_cpl_ks_java_01_lms.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c LEFT JOIN FETCH c.users u LEFT JOIN FETCH c.messages m LEFT JOIN FETCH m.user WHERE c.id = :id")
    Conversation findByIdWithAllDetails(@Param("id") Long id);

    @Query("SELECT c FROM Conversation c WHERE SIZE(c.users) = 2 AND :user1 MEMBER OF c.users AND :user2 MEMBER OF c.users")
    Optional<Conversation> findPersonalConversationBetween(@Param("user1") User user1, @Param("user2") User user2);

    @Query("SELECT c FROM Conversation c WHERE SIZE(c.users) = 2 AND :user MEMBER OF c.users")
    List<Conversation> findPersonalConversationOfUser(@Param("user") User user);

    @Query("SELECT c FROM Conversation c WHERE SIZE(c.users) > 2 AND :user MEMBER OF c.users ")
    List<Conversation> findGroupConversationOfUser(@Param("user") User user);

    @Query("SELECT DISTINCT c FROM Conversation c " +
            "JOIN c.users u1 " +
            "JOIN c.users u2 " +
            "WHERE SIZE(c.users) = 2 " +
            "AND u1.id = :currentUserId " +
            "AND u2.id != :currentUserId " +
            "AND (LOWER(u2.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u2.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) ")
    List<Conversation> searchConversationsByOpponentName(@Param("currentUserId") Long currentUserId,
                                                         @Param("searchTerm") String searchTerm,
                                                         Pageable pageable);

    @Query(value = "SELECT new com.example.hcm25_cpl_ks_java_01_lms.chat.ConversationDTO(" +
            "c.id, c.name, :currentTime, " +
            "CONCAT(u2.lastName, ' ', u2.firstName), " +
            "COALESCE(m.text, 'No messages yet'), " +
            "COALESCE(CAST(m.timestamp AS string), '11/11/1111 11:11')) " +
            "FROM Conversation c " +
            "JOIN c.users u1 " +
            "JOIN c.users u2 " +
            "LEFT JOIN Message m ON m.conversation = c AND m.timestamp = (" +
            "    SELECT MAX(m2.timestamp) FROM Message m2 WHERE m2.conversation = c " +
            "    AND NOT EXISTS (SELECT dm FROM DeletedMessage dm WHERE dm.messageId = m2.id AND dm.userId = :userId)" +
            ") " +
            "WHERE u1.id = :userId AND u2.id != :userId AND SIZE(c.users) = 2 " +
            "AND EXISTS (SELECT m3 FROM Message m3 WHERE m3.conversation = c " +
            "    AND NOT EXISTS (SELECT dm FROM DeletedMessage dm WHERE dm.messageId = m3.id AND dm.userId = :userId)) " +
            "ORDER BY COALESCE((SELECT MAX(m4.timestamp) FROM Message m4 WHERE m4.conversation = c " +
            "    AND NOT EXISTS (SELECT dm FROM DeletedMessage dm WHERE dm.messageId = m4.id AND dm.userId = :userId)), c.timestamp) DESC",
            countQuery = "SELECT count(c) FROM Conversation c " +
                    "JOIN c.users u1 " +
                    "JOIN c.users u2 " +
                    "WHERE u1.id = :userId AND u2.id != :userId AND SIZE(c.users) = 2 " +
                    "AND EXISTS (SELECT m3 FROM Message m3 WHERE m3.conversation = c " +
                    "    AND NOT EXISTS (SELECT dm FROM DeletedMessage dm WHERE dm.messageId = m3.id AND dm.userId = :userId))")
    Page<ConversationDTO> findPersonalConversationsWithMessages(
            @Param("userId") Long userId,
            @Param("currentTime") String currentTime,
            Pageable pageable);
}