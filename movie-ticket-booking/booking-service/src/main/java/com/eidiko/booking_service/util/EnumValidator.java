package com.eidiko.booking_service.util;

public class EnumValidator {
    public static <E extends Enum<E>> E validateEnum(Class<E> enumClass, String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + ": " + value);
        }
    }

    private EnumValidator(){}
}

