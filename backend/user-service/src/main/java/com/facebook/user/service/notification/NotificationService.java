package com.facebook.user.service.notification;

import com.facebook.user.model.User;

public interface NotificationService {
    void sendBanNotificationEmail(User user);
    void sendBanNotificationSms(User user);
    void sendBanNotificationWhatsApp(User user);
} 