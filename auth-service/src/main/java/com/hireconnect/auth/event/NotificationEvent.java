package com.hireconnect.auth.event;

import com.hireconnect.auth.entity.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Domain entity or core component representing NotificationEvent.
 *
 * @author Disha Gujar
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {

    private Long recipientUserId;
    private String recipientEmail;
    private String title;
    private String message;
    private NotificationType type;
    private Boolean sendEmail;
}
