package com.blog.app.exceptions;

public class UserAlreadyExistException extends RuntimeException {

    String resourceName;
    String fieldName;
    String fieldValue;

    public UserAlreadyExistException(String resourceName, String fieldName, String fieldValue) {
        super(String.format("%s already exist with provided %s : %s", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

}

