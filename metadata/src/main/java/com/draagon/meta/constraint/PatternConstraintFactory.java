package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * v6.0.0: Factory for creating regex pattern constraints.
 * Validates that string values match a specified regular expression pattern.
 */
public class PatternConstraintFactory implements ConstraintFactory {
    
    @Override
    public Constraint createConstraint(Map<String, Object> parameters) throws ConstraintCreationException {
        validateParameters(parameters);
        
        String regex = (String) parameters.get("pattern");
        String flags = (String) parameters.get("flags");
        
        try {
            Pattern pattern;
            if (flags != null && !flags.isEmpty()) {
                int regexFlags = parseFlags(flags);
                pattern = Pattern.compile(regex, regexFlags);
            } else {
                pattern = Pattern.compile(regex);
            }
            
            return new PatternConstraint(pattern, regex);
            
        } catch (PatternSyntaxException e) {
            throw new ConstraintCreationException("Invalid regex pattern: " + regex, "pattern", e);
        }
    }
    
    @Override
    public String getConstraintType() {
        return "pattern";
    }
    
    @Override
    public void validateParameters(Map<String, Object> parameters) throws ConstraintCreationException {
        if (!parameters.containsKey("pattern")) {
            throw ConstraintCreationException.missingParameter("pattern", "pattern");
        }
        
        Object pattern = parameters.get("pattern");
        if (!(pattern instanceof String)) {
            throw ConstraintCreationException.invalidParameterType("pattern", "pattern", String.class, pattern);
        }
        
        if (((String) pattern).isEmpty()) {
            throw ConstraintCreationException.invalidParameterValue("pattern", "pattern", pattern, "pattern cannot be empty");
        }
        
        // Validate flags if present
        if (parameters.containsKey("flags")) {
            Object flags = parameters.get("flags");
            if (!(flags instanceof String)) {
                throw ConstraintCreationException.invalidParameterType("pattern", "flags", String.class, flags);
            }
        }
    }
    
    private int parseFlags(String flagString) {
        int flags = 0;
        for (char c : flagString.toCharArray()) {
            switch (c) {
                case 'i':
                    flags |= Pattern.CASE_INSENSITIVE;
                    break;
                case 'm':
                    flags |= Pattern.MULTILINE;
                    break;
                case 's':
                    flags |= Pattern.DOTALL;
                    break;
                case 'u':
                    flags |= Pattern.UNICODE_CASE;
                    break;
                case 'x':
                    flags |= Pattern.COMMENTS;
                    break;
                default:
                    // Ignore unknown flags
                    break;
            }
        }
        return flags;
    }
    
    /**
     * Constraint implementation for pattern matching
     */
    private static class PatternConstraint implements Constraint {
        private final Pattern pattern;
        private final String originalRegex;
        
        public PatternConstraint(Pattern pattern, String originalRegex) {
            this.pattern = pattern;
            this.originalRegex = originalRegex;
        }
        
        @Override
        public void validate(MetaData metaData, Object value, ValidationContext context) throws ConstraintViolationException {
            if (value == null) {
                return; // Pattern constraints don't validate null values - use required constraint for that
            }
            
            String stringValue = value.toString();
            if (!pattern.matcher(stringValue).matches()) {
                throw ConstraintViolationException.patternMismatch(originalRegex, value, context);
            }
        }
        
        @Override
        public String getType() {
            return "pattern";
        }
        
        @Override
        public String getDescription() {
            return "Validates that a string value matches the regex pattern: " + originalRegex;
        }
    }
}