/**
 * React Text MetaView component - equivalent to Java TextView
 */

import React from 'react';
import { BaseMetaView, useMetaViewLogic } from './base/BaseMetaView';
import { MetaViewProps, ViewMode } from '@/types/metadata';

export interface TextViewProps extends MetaViewProps {
  size?: number;
  placeholder?: string;
}

/**
 * Text input MetaView for string fields
 */
export const TextView: React.FC<TextViewProps> = (props) => {
  const {
    field,
    value,
    mode,
    params = {},
    onChange,
    onBlur,
    size,
    placeholder,
    ...baseProps
  } = props;

  const { isReadOnly, isEditable, displayValue, fieldProps } = useMetaViewLogic(props);

  // Get size from props or params, default based on field length
  const inputSize = size || 
    (params.size as number) || 
    Math.min(field.length || 255, 50); // Default max display size of 50

  const maxLength = field.length || 255;

  const handleInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    onChange?.(event.target.value);
  };

  const handleInputBlur = (event: React.FocusEvent<HTMLInputElement>) => {
    onBlur?.();
  };

  const renderContent = () => {
    if (mode === ViewMode.READ) {
      // Read-only display
      return (
        <span className="meta-view__text-display" {...fieldProps}>
          {displayValue || <em className="meta-view__empty">No value</em>}
        </span>
      );
    }

    if (mode === ViewMode.EDIT) {
      // Editable input
      return (
        <input
          {...fieldProps}
          type="text"
          className="meta-view__text-input"
          value={value || ''}
          onChange={handleInputChange}
          onBlur={handleInputBlur}
          size={inputSize}
          maxLength={maxLength}
          placeholder={placeholder || `Enter ${field.displayName || field.name}`}
          required={field.isRequired}
        />
      );
    }

    return null;
  };

  return (
    <BaseMetaView {...baseProps} field={field} value={value} mode={mode} params={params}>
      {renderContent()}
    </BaseMetaView>
  );
};

TextView.displayName = 'TextView';