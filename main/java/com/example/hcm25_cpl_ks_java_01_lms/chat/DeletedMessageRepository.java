package com.example.hcm25_cpl_ks_java_01_lms.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeletedMessageRepository extends JpaRepository<DeletedMessage, Long> {
}