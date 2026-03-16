package dev.spiffocode.sigesapi.users.infrastructure.service.impl;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class AvatarGenerator {

    private static final String[] PALETTE = {
        "#5C6BC0", "#7E57C2", "#26A69A", "#EC407A",
        "#AB47BC", "#42A5F5", "#FF7043", "#66BB6A"
    };

    public byte[] generate(String firstName, String lastName){
        String initials = extractInitials(firstName, lastName);
        String color = pickColor(firstName + ' ' + lastName);

        String svg = """
            <svg xmlns="http://www.w3.org/2000/svg" width="256" height="256" viewBox="0 0 256 256">
              <defs>
                <style>
                  @import url('https://fonts.googleapis.com/css2?family=DM+Serif+Display&amp;display=swap');
                </style>
              </defs>
              <circle cx="128" cy="128" r="128" fill="%s"/>
              <text
                x="128" y="128"
                font-family="'DM Serif Display', serif"
                font-size="96"
                fill="white"
                text-anchor="middle"
                dominant-baseline="central"
              >%s</text>
            </svg>
            """.formatted(color, initials);

        return svg.getBytes(StandardCharsets.UTF_8);
    }

    private String extractInitials(String firstName, String lastName) {
        return (firstName.charAt(0) + "" + lastName.charAt(0)).toUpperCase();
    }

    private String pickColor(String fullName) {
        return PALETTE[Math.abs(fullName.hashCode()) % PALETTE.length];
    }
}