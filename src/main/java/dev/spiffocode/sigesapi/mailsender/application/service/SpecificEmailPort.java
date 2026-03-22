package dev.spiffocode.sigesapi.mailsender.application.service;

import org.thymeleaf.context.Context;

public interface SpecificEmailPort  {
    void sendHtml(String to, String subject, String template, Context ctx);
}
