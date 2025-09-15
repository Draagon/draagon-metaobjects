/**
 * React TextArea MetaView component - equivalent to Java TextAreaView
 */

import React from 'react';
import { BaseMetaView, useMetaViewLogic } from './base/BaseMetaView';
import { MetaViewProps, ViewMode } from '@/types/metadata';

export interface TextAreaViewProps extends MetaViewProps {
  rows?: number;
  cols?: number;
  placeholder?: string;
}

/**
 * TextArea MetaView for longer text fields
 */
export const TextAreaView: React.FC<TextAreaViewProps> = (props) => {
  const {
    field,
    value,
    mode,
    params = {},
    onChange,
    onBlur,
    rows,
    cols,
    placeholder,
    ...baseProps
  } = props;

  const { isReadOnly, isEditable, displayValue, fieldProps } = useMetaViewLogic(props);

  // Get dimensions from props or params, with defaults
  const textAreaRows = rows || (params.rows as number) || 4;
  const textAreaCols = cols || (params.cols as number) || 50;
  const maxLength = field.length || 2000; // Default max length for text areas

  const handleInputChange = (event: React.ChangeEvent<HTMLTextAreaElement>) => {
    onChange?.(event.target.value);
  };

  const handleInputBlur = (event: React.FocusEvent<HTMLTextAreaElement>) => {
    onBlur?.();
  };

  const renderContent = () => {
    if (mode === ViewMode.READ) {
      // Read-only display with preserved line breaks
      return (
        <div className="meta-view__textarea-display" {...fieldProps}>
          {displayValue ? (
            <pre className="meta-view__textarea-text">
              {displayValue}
            </pre>
          ) : (
            <em className="meta-view__empty">No value</em>
          )}
        </div>
      );
    }

    if (mode === ViewMode.EDIT) {
      // Editable textarea
      return (
        <textarea
          {...fieldProps}
          className="meta-view__textarea-input"
          value={value || ''}
          onChange={handleInputChange}
          onBlur={handleInputBlur}
          rows={textAreaRows}
          cols={textAreaCols}
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

TextAreaView.displayName = 'TextAreaView';