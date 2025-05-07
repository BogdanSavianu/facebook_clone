package com.facebook.user.service.notification;

import com.facebook.user.model.User;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;

import java.io.IOException;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${app.sendgrid.from.email}")
    private String fromEmail;

    @Value("${twilio.account.sid}")
    private String twilioAccountSid;

    @Value("${twilio.auth.token}")
    private String twilioAuthToken;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    @PostConstruct
    public void initTwilio() {
        try {
            if (twilioAccountSid != null && !twilioAccountSid.startsWith("ACxxxxx") && 
                twilioAuthToken != null && !twilioAuthToken.equals("your_auth_token")) {
                Twilio.init(twilioAccountSid, twilioAuthToken);
                logger.info("Twilio client initialized successfully.");
            } else {
                logger.warn("Twilio client NOT initialized. Please provide valid twilio.account.sid and twilio.auth.token in properties.");
            }
        } catch (Exception e) {
            logger.error("Failed to initialize Twilio client: {}", e.getMessage());
        }
    }

    @Override
    public void sendBanNotificationEmail(User user) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            logger.warn("Cannot send ban notification email: User {} has no email address.", user.getUsername());
            return;
        }

        Email from = new Email(fromEmail);
        String subject = "Account Banned Notification";
        Email to = new Email(user.getEmail());
        Content content = new Content("text/plain", 
                "Hello " + user.getUsername() + ",\n\nYour account has been banned due to a violation of our terms of service.\n\nSincerely,\nThe Facebook Clone Team");
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            logger.info("SendGrid API Response - Status: {}, Body: {}, Headers: {}", 
                        response.getStatusCode(), response.getBody(), response.getHeaders());
                        
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                 logger.info("Successfully sent ban notification email request to SendGrid for {}.", user.getEmail());
            } else {
                logger.error("Failed to send email via SendGrid for {}. Status: {}, Body: {}", 
                             user.getEmail(), response.getStatusCode(), response.getBody());
            }
        } catch (IOException ex) {
            logger.error("Error sending ban notification email to {}: {}", user.getEmail(), ex.getMessage());
        }
    }

    @Override
    public void sendBanNotificationSms(User user) {
        if (user.getPhoneNumber() == null || user.getPhoneNumber().trim().isEmpty()) {
            logger.warn("Cannot send SMS ban notification: User {} has no phone number recorded.", user.getUsername());
            return;
        }
        
        // Basic validation/formatting (ensure E.164 format like +12223334444)
        String recipientPhoneNumber = user.getPhoneNumber().trim();
        if (!recipientPhoneNumber.startsWith("+")) {
             logger.warn("Recipient phone number {} does not start with '+'. Attempting to send anyway, but E.164 format is recommended.", recipientPhoneNumber);
             // Potentially add logic here to prefix country code if needed/possible
        }
        
        // Check if Twilio was initialized
        if (twilioAccountSid == null || twilioAccountSid.startsWith("ACxxxxx") || 
            twilioAuthToken == null || twilioAuthToken.equals("your_auth_token")) {
            logger.error("Cannot send SMS: Twilio client not initialized due to missing/placeholder credentials.");
            return;
        }

        try {
            String messageBody = String.format("Hello %s, Your Facebook Clone account has been banned due to a violation of our terms.", user.getUsername());

            Message message = Message.creator(
                    new PhoneNumber(recipientPhoneNumber), // To number
                    new PhoneNumber(twilioPhoneNumber),    // From Twilio number
                    messageBody)
                    .create();

            logger.info("Sent ban notification SMS to {} (SID: {}). Status: {}", recipientPhoneNumber, message.getSid(), message.getStatus().toString());

        } catch (Exception e) {
            // Catch specific TwilioException if preferred
            logger.error("Error sending ban notification SMS to {}: {}", recipientPhoneNumber, e.getMessage());
        }
    }

    @Override
    public void sendBanNotificationWhatsApp(User user) {
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            logger.info("SIMULATING: WhatsApp ban notification sent to {} for user {}. Message: Hello {}, your account has been banned.", 
                user.getPhoneNumber(), user.getUsername(), user.getUsername());
        } else {
            logger.warn("Cannot send WhatsApp ban notification: User {} has no phone number recorded.", user.getUsername());
        }
    }
} 