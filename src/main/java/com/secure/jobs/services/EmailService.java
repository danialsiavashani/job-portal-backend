package com.secure.jobs.services;

public interface EmailService {

    void sendPasswordResetEmail(String toEmail, String resetUrl);
}
