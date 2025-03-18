package com.utcn.userservice.service;

import org.springframework.stereotype.Service;

@Service
public class SMSService {

    public void sendSMS(String phoneNumber, String message) {
        System.out.println("Sending SMS to " + phoneNumber + ": " + message);
    }
} 