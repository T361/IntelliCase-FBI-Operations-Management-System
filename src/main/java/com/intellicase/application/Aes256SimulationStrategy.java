package com.intellicase.application;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Strategy simulating AES-256 encryption using Base64 encoding.
 * GoF Strategy: pluggable encryption behavior.
 */
public class Aes256SimulationStrategy implements EncryptionStrategy {
    @Override
    public String encrypt(String rawData) {
        if (rawData == null) {
            return "";
        }
        return Base64.getEncoder().encodeToString(rawData.getBytes(StandardCharsets.UTF_8));
    }
}
