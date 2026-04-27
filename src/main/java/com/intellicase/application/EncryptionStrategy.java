package com.intellicase.application;

/**
 * GoF Strategy for shadow profile encryption routines.
 */
public interface EncryptionStrategy {
    String encrypt(String rawData);
}
