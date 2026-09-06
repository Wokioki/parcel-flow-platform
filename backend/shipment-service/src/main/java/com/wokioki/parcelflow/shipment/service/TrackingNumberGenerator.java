package com.wokioki.parcelflow.shipment.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class TrackingNumberGenerator {

    private static final String PREFIX = "PF";
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int RANDOM_LENGTH = 12;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        StringBuilder value = new StringBuilder(PREFIX);

        for (int i = 0; i < RANDOM_LENGTH; i++) {
            int index = secureRandom.nextInt(ALPHABET.length());
            value.append(ALPHABET.charAt(index));
        }

        return value.toString();
    }
}
