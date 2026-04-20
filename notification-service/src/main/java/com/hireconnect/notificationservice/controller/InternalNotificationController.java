package com.hireconnect.notificationservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hireconnect.notificationservice.dto.request.NotificationCreateRequestDto;
import com.hireconnect.notificationservice.dto.response.NotificationResponseDto;
import com.hireconnect.notificationservice.service.NotificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/notifications")
@RequiredArgsConstructor
public class InternalNotificationController {

    private static final Logger logger = LoggerFactory.getLogger(InternalNotificationController.class);

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponseDto> createNotification(
            @Valid @RequestBody NotificationCreateRequestDto requestDto
    ) {
        logger.info(
                "Internal create notification request received for recipientUserId={}, type={}, sendEmail={}",
                requestDto.getRecipientUserId(),
                requestDto.getType(),
                requestDto.getSendEmail()
        );

        NotificationResponseDto response = notificationService.createNotification(requestDto);

        logger.info("Notification created successfully with id={} for userId={}", response.getId(), response.getUserId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
