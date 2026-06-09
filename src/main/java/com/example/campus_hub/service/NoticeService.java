package com.example.campus_hub.service;

import com.example.campus_hub.dto.CreateNoticeRequest;
import com.example.campus_hub.entity.Notice;
import com.example.campus_hub.entity.Notice.ForRole;
import com.example.campus_hub.entity.User;
import com.example.campus_hub.repository.NoticeRepository;
import com.example.campus_hub.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository   userRepository;

    public NoticeService(
            NoticeRepository noticeRepository,
            UserRepository   userRepository
    ) {
        this.noticeRepository = noticeRepository;
        this.userRepository   = userRepository;
    }

    // ════════════════════════════════════════════════════
    // GET ALL — filtered by role of the requesting user
    // ════════════════════════════════════════════════════
    public Page<Notice> getAll(
            String userEmail,
            String userRole,
            String search,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page - 1, size);

        // Admin sees all with search
        if ("ADMIN".equals(userRole)) {
            return noticeRepository.findAllWithSearch(
                    search, pageable
            );
        }

        // Others see ALL + their role-specific notices
        ForRole role;
        try {
            role = ForRole.valueOf(userRole);
        } catch (IllegalArgumentException e) {
            role = ForRole.ALL;
        }

        return noticeRepository.findByRole(role, pageable);
    }

    // ════════════════════════════════════════════════════
    // GET ONE
    // ════════════════════════════════════════════════════
    public Notice getById(String id) {
        return noticeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("NOTICE_NOT_FOUND")
                );
    }

    // ════════════════════════════════════════════════════
    // CREATE
    // ════════════════════════════════════════════════════
    @Transactional
    public Notice create(
            CreateNoticeRequest request,
            String creatorEmail) {

        User creator = userRepository
                .findByEmail(creatorEmail)
                .orElseThrow(() ->
                        new RuntimeException("USER_NOT_FOUND")
                );

        ForRole forRole;
        try {
            forRole = ForRole.valueOf(
                    request.getForRole().toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("INVALID_ROLE");
        }

        Notice notice = new Notice();
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setCreatedBy(creator);
        notice.setForRole(forRole);
        notice.setUrgent(request.isUrgent());

        return noticeRepository.save(notice);
    }

    // ════════════════════════════════════════════════════
    // DELETE
    // ════════════════════════════════════════════════════
    @Transactional
    public void delete(String id, String userEmail) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("NOTICE_NOT_FOUND")
                );

        // Only the creator or admin can delete
        User user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("USER_NOT_FOUND")
                );

        boolean isAdmin   = user.getRole() == User.Role.ADMIN;
        boolean isCreator = notice.getCreatedBy()
                .getId()
                .equals(user.getId());

        if (!isAdmin && !isCreator) {
            throw new RuntimeException("NOT_AUTHORIZED");
        }

        noticeRepository.delete(notice);
    }
}