/**
 * Base React MetaView component that all specific view implementations extend
 */

import React, { useCallback, useMemo } from 'react';
import { MetaViewProps, ViewMode, ViewParams } from '@/types/metadata';
import { cn } from '@/utils/classnames';

export interface BaseMetaViewProps extends MetaViewProps {
  children?: React.ReactNode;
}

/**
 * Base component providing common functionality for all MetaViews
 */
export const BaseMetaView: React.FC<BaseMetaViewProps> = ({
  field,
  value,
  mode,
  label,
  params = {},
  onChange,
  onBlur,
  errors = [],
  className,
  children,
}) => {
  const displayLabel = label || field.displayName || field.name;
  const hasErrors = errors.length > 0;
  const isReadOnly = mode === ViewMode.READ || params.isReadOnly === true;
  const isHidden = mode === ViewMode.HIDE;

  // Combine CSS classes
  const combinedClassName = useMemo(() => {
    return cn(
      'meta-view',
      `meta-view--${field.type}`,
      `meta-view--${mode.toLowerCase()}`,
      {
        'meta-view--error': hasErrors,
        'meta-view--readonly': isReadOnly,
        'meta-view--hidden': isHidden,
      },
      params.styleClass,
      className
    );
  }, [field.type, mode, hasErrors, isReadOnly, isHidden, params.styleClass, className]);

  // Handle value changes with validation
  const handleChange = useCallback((newValue: unknown) => {
    if (isReadOnly) return;
    
    // Perform client-side validation here if needed
    onChange?.(newValue);
  }, [onChange, isReadOnly]);

  // Handle blur events
  const handleBlur = useCallback(() => {
    if (isReadOnly) return;
    onBlur?.();
  }, [onBlur, isReadOnly]);

  // Don't render anything if hidden
  if (isHidden) {
    return null;
  }

  return (
    <div className={combinedClassName} data-field={field.name}>
      {/* Label */}
      {displayLabel && (
        <label className="meta-view__label" htmlFor={`field-${field.name}`}>
          {displayLabel}
          {field.isRequired && <span className="meta-view__required">*</span>}
        </label>
      )}

      {/* Field Description */}
      {field.description && (
        <div className="meta-view__description">
          {field.description}
        </div>
      )}

      {/* Main Content */}
      <div className="meta-view__content">
        {children}
      </div>

      {/* Error Messages */}
      {hasErrors && (
        <div className="meta-view__errors">
          {errors.map((error, index) => (
            <div key={index} className="meta-view__error">
              {error}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

/**
 * Hook for common MetaView logic
 */
export function useMetaViewLogic(props: MetaViewProps) {
  const { field, value, mode, params = {} } = props;
  
  const isReadOnly = mode === ViewMode.READ || params.isReadOnly === true;
  const isEditable = mode === ViewMode.EDIT && !isReadOnly;
  const isHidden = mode === ViewMode.HIDE;
  
  // Format value for display
  const displayValue = useMemo(() => {
    if (value == null) return '';
    
    // Handle different field types
    switch (field.type) {
      case 'date':
        return value instanceof Date ? value.toLocaleDateString() : String(value);
      case 'boolean':
        return value ? 'Yes' : 'No';
      default:
        return String(value);
    }
  }, [value, field.type]);
  
  // Get field-specific props
  const fieldProps = useMemo(() => {
    const props: Record<string, unknown> = {
      id: `field-${field.name}`,
      name: field.name,
      disabled: isReadOnly,
    };
    
    // Add size/length constraints
    if (field.length && field.type === 'string') {
      props.maxLength = field.length;
    }
    
    // Add CSS styling
    if (params.width) {
      props.style = { ...props.style as object, width: params.width };
    }
    if (params.height) {
      props.style = { ...props.style as object, height: params.height };
    }
    
    return props;
  }, [field, isReadOnly, params]);
  
  return {
    isReadOnly,
    isEditable,
    isHidden,
    displayValue,
    fieldProps,
  };
}