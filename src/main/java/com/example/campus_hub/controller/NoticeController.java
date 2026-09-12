package com.example.campus_hub.controller;

import com.example.campus_hub.dto.ApiResponse;
import com.example.campus_hub.dto.CreateNoticeRequest;
import com.example.campus_hub.entity.Notice;
import com.example.campus_hub.service.NoticeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notices")
@CrossOrigin(
        origins = {"${app.frontend.url}", "https://campushub-n6bn.onrender.com"},
        allowCredentials = "true"
)
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    // GET /api/notices
    // Each role sees notices relevant to them
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<Notice>>> getAll(
            Authentication auth,
            @RequestParam(defaultValue = "")  String search,
            @RequestParam(defaultValue = "1") int    page,
            @RequestParam(defaultValue = "10")int    size
    ) {
        // Get role from JWT — stored as ROLE_ADMIN etc
        // Strip the ROLE_ prefix to get clean role name
        String role = auth.getAuthorities()
                .iterator().next()
                .getAuthority()
                .replace("ROLE_", "");

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Notices fetched",
                        noticeService.getAll(
                                auth.getName(), role, search, page, size
                        )
                )
        );
    }

    // GET /api/notices/:id
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Notice>> getById(
            @PathVariable String id
    ) {
        try {
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Notice fetched",
                            noticeService.getById(id)
                    )
            );
        } catch (RuntimeException e) {
            if ("NOTICE_NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Notice not found"));
            }
            throw e;
        }
    }

    // POST /api/notices
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Notice>> create(
            @Valid @RequestBody CreateNoticeRequest request,
            Authentication auth
    ) {
        try {
            Notice notice = noticeService.create(
                    request, auth.getName()
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            "Notice created", notice
                    ));
        } catch (RuntimeException e) {
            if ("INVALID_ROLE".equals(e.getMessage())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(
                                "forRole must be ALL, STUDENT, TEACHER, or PARENT"
                        ));
            }
            throw e;
        }
    }

    // DELETE /api/notices/:id
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Object>> delete(
            @PathVariable String id,
            Authentication auth
    ) {
        try {
            noticeService.delete(id, auth.getName());
            return ResponseEntity.ok(
                    ApiResponse.success("Notice deleted", null)
            );
        } catch (RuntimeException e) {
            return switch (e.getMessage()) {
                case "NOTICE_NOT_FOUND" ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error("Notice not found"));
                case "NOT_AUTHORIZED" ->
                        ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ApiResponse.error(
                                        "You can only delete your own notices"
                                ));
                default -> throw e;
            };
        }
    }
}