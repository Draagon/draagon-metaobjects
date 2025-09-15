/**
 * React Numeric MetaView component for int, long, double fields
 */

import React, { useMemo } from 'react';
import { BaseMetaView, useMetaViewLogic } from './base/BaseMetaView';
import { MetaViewProps, ViewMode, FieldType } from '@/types/metadata';

export interface NumericViewProps extends MetaViewProps {
  min?: number;
  max?: number;
  step?: number;
  precision?: number;
  placeholder?: string;
}

/**
 * Numeric input MetaView for number fields (int, long, double)
 */
export const NumericView: React.FC<NumericViewProps> = (props) => {
  const {
    field,
    value,
    mode,
    params = {},
    onChange,
    onBlur,
    min,
    max,
    step,
    precision,
    placeholder,
    ...baseProps
  } = props;

  const { isReadOnly, isEditable, fieldProps } = useMetaViewLogic(props);

  // Determine input type and constraints based on field type
  const inputConfig = useMemo(() => {
    switch (field.type) {
      case FieldType.INT:
        return {
          type: 'number',
          step: step || 1,
          precision: 0,
          formatter: (val: number) => val.toFixed(0),
          parser: (str: string) => parseInt(str, 10),
        };
      case FieldType.LONG:
        return {
          type: 'number',
          step: step || 1,
          precision: 0,
          formatter: (val: number) => val.toFixed(0),
          parser: (str: string) => parseInt(str, 10),
        };
      case FieldType.DOUBLE:
        return {
          type: 'number',
          step: step || 0.01,
          precision: precision || 2,
          formatter: (val: number) => val.toFixed(precision || 2),
          parser: (str: string) => parseFloat(str),
        };
      default:
        return {
          type: 'number',
          step: step || 1,
          precision: 0,
          formatter: (val: number) => String(val),
          parser: (str: string) => Number(str),
        };
    }
  }, [field.type, step, precision]);

  // Format value for display
  const displayValue = useMemo(() => {
    if (value == null) return '';
    const numValue = Number(value);
    if (isNaN(numValue)) return String(value);
    return inputConfig.formatter(numValue);
  }, [value, inputConfig]);

  // Format value for input
  const inputValue = useMemo(() => {
    if (value == null) return '';
    return String(value);
  }, [value]);

  const handleInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const inputStr = event.target.value;
    
    if (!inputStr) {
      onChange?.(null);
      return;
    }

    try {
      const numValue = inputConfig.parser(inputStr);
      if (!isNaN(numValue)) {
        onChange?.(numValue);
      }
    } catch (error) {
      // Invalid number, don't update
      console.warn('Invalid numeric input:', inputStr);
    }
  };

  const handleInputBlur = (event: React.FocusEvent<HTMLInputElement>) => {
    onBlur?.();
  };

  const renderContent = () => {
    if (mode === ViewMode.READ) {
      // Read-only display
      return (
        <span className="meta-view__numeric-display" {...fieldProps}>
          {displayValue || <em className="meta-view__empty">No value</em>}
        </span>
      );
    }

    if (mode === ViewMode.EDIT) {
      // Editable numeric input
      return (
        <input
          {...fieldProps}
          type={inputConfig.type}
          className="meta-view__numeric-input"
          value={inputValue}
          onChange={handleInputChange}
          onBlur={handleInputBlur}
          min={min}
          max={max}
          step={inputConfig.step}
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

NumericView.displayName = 'NumericView';