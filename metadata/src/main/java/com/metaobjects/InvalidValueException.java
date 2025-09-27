/*
 * Copyright (c) 2003-2012 Doug Mealing LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.metaobjects;

import com.metaobjects.util.ErrorFormatter;

/**
 * Exception thrown when a value is invalid for a field of an object.
 * Enhanced with structured error reporting capabilities.
 * 
 * @since 1.0 (enhanced in 5.2.0)
 */
public class InvalidValueException extends ValueException {

    /**
     * Creates an InvalidValueException with a message and cause.
     * Backward compatible constructor.
     * 
     * @param msg the error message
     * @param t the underlying cause
     */
    public InvalidValueException(String msg, Throwable t) {
        super(msg, t);
    }

    /**
     * Creates an InvalidValueException with a message.
     * Backward compatible constructor.
     * 
     * @param msg the error message
     */
    public InvalidValueException(String msg) {
        super(msg);
    }

    /**
     * Factory method for creating a validation error with enhanced context.
     * 
     * @param source the MetaData object being validated
     * @param validation the validation rule that failed
     * @param value the actual value that failed validation
     * @param expected the expected value or constraint
     * @return a configured InvalidValueException
     */
    public static InvalidValueException forValidation(MetaData source, String validation, Object value, String expected) {
        String message = ErrorFormatter.formatValidationError(source, validation, value, expected);
        return new InvalidValueException(message);
    }

    /**
     * Factory method for creating a type mismatch error.
     * 
     * @param source the MetaData object with the type mismatch
     * @param expected the expected type
     * @param actual the actual type encountered
     * @return a configured InvalidValueException
     */
    public static InvalidValueException forTypeMismatch(MetaData source, Class<?> expected, Class<?> actual) {
        String message = ErrorFormatter.formatTypeError(source, expected, actual);
        return new InvalidValueException(message);
    }

    /**
     * Factory method for creating a type mismatch error with string type names.
     * 
     * @param source the MetaData object with the type mismatch
     * @param expectedType the expected type name
     * @param actualType the actual type name encountered
     * @return a configured InvalidValueException
     */
    public static InvalidValueException forTypeMismatch(MetaData source, String expectedType, String actualType) {
        String message = ErrorFormatter.formatTypeError(source, expectedType, actualType);
        return new InvalidValueException(message);
    }

    /**
     * Factory method for creating a range validation error.
     * 
     * @param source the MetaData object being validated
     * @param value the value that is out of range
     * @param minValue the minimum allowed value
     * @param maxValue the maximum allowed value
     * @return a configured InvalidValueException
     */
    public static InvalidValueException forRange(MetaData source, Object value, Object minValue, Object maxValue) {
        String expected = String.format("value between %s and %s", minValue, maxValue);
        return forValidation(source, "range", value, expected);
    }

    /**
     * Factory method for creating a required field validation error.
     * 
     * @param source the MetaData object that is required but null/empty
     * @return a configured InvalidValueException
     */
    public static InvalidValueException forRequired(MetaData source) {
        return forValidation(source, "required", "<null/empty>", "non-null value");
    }

    /**
     * Factory method for creating a format validation error.
     * 
     * @param source the MetaData object being validated
     * @param value the value with invalid format
     * @param expectedFormat the expected format description
     * @return a configured InvalidValueException
     */
    public static InvalidValueException forFormat(MetaData source, Object value, String expectedFormat) {
        return forValidation(source, "format", value, expectedFormat);
    }
}
