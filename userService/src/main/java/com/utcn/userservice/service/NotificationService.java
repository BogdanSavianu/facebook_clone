package com.utcn.userservice.service;

import com.utcn.userservice.entity.User;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class NotificationService {

    @Value("${sendgrid.api.key}")
    private String sendgridApiKey;
    
    @Value("${sendgrid.from.email:noreply@facebook-clone.com}")
    private String fromEmail;
    
    @Autowired
    private SMSService smsService;

    public void sendBanNotification(User user, String reason) {
        sendBanEmail(user, reason);
        sendBanSMS(user, reason);
    }

    private void sendBanEmail(User user, String reason) {
        Email from = new Email(fromEmail);
        Email to = new Email(user.getEmail());
        String subject = "Your Facebook account has been banned";
        String emailContent = "Dear " + user.getFirstName() + " " + user.getLastName() + ",\n\n" +
                "Your Facebook account has been banned due to a violation of our terms of service.\n\n" +
                "Reason: " + reason + "\n\n" +
                "If you believe this decision is in error, please contact our support team.\n\n" +
                "Facebook Support Team";
        
        Content content = new Content("text/plain", emailContent);
        Mail mail = new Mail(from, subject, to, content);
        
        SendGrid sg = new SendGrid(sendgridApiKey);
        Request request = new Request();
        
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            System.out.println("SendGrid Status Code: " + response.getStatusCode());
            System.out.println("SendGrid Response Body: " + response.getBody());
            System.out.println("SendGrid Response Headers: " + response.getHeaders());
        } catch (IOException ex) {
            System.err.println("Error sending email through SendGrid: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void sendBanSMS(User user, String reason) {
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            String message = "Facebook: Your account has been banned. Reason: " + reason;
            smsService.sendSMS(user.getPhoneNumber(), message);
        }
    }
} 