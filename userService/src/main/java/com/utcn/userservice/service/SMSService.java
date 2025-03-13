package com.utcn.userservice.service;

import org.springframework.stereotype.Service;

@Service
public class SMSService {

    public void sendSMS(String phoneNumber, String message) {
        // In a real application, this would integrate with an SMS provider like Twilio
        System.out.println("Sending SMS to " + phoneNumber + ": " + message);
    }
} 