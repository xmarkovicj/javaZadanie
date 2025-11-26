package com.markovic.javazadanie.fx;

public class AlreadySubmittedException extends RuntimeException {
    public AlreadySubmittedException() {
        super("ALREADY_SUBMITTED");
    }
}
