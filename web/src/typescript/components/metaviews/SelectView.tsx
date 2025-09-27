/**
 * React Select MetaView component for dropdown selections
 */

import React, { useMemo } from 'react';
import { BaseMetaView, useMetaViewLogic } from './base/BaseMetaView';
import { MetaViewProps, ViewMode } from '@/types/metadata';

export interface SelectOption {
  value: unknown;
  label: string;
  disabled?: boolean;
}

export interface SelectViewProps extends MetaViewProps {
  options?: SelectOption[];
  allowEmpty?: boolean;
  emptyLabel?: string;
  placeholder?: string;
}

/**
 * Select dropdown MetaView for enumerated values
 */
export const SelectView: React.FC<SelectViewProps> = (props) => {
  const {
    field,
    value,
    mode,
    params = {},
    onChange,
    onBlur,
    options = [],
    allowEmpty = true,
    emptyLabel = 'Select...',
    placeholder,
    ...baseProps
  } = props;

  const { isReadOnly, isEditable, fieldProps } = useMetaViewLogic(props);

  // Find the selected option
  const selectedOption = useMemo(() => {
    return options.find(option => option.value === value);
  }, [options, value]);

  // Display value for read mode
  const displayValue = useMemo(() => {
    if (selectedOption) {
      return selectedOption.label;
    }
    if (value != null) {
      return String(value);
    }
    return '';
  }, [selectedOption, value]);

  const handleSelectChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedValue = event.target.value;
    
    if (selectedValue === '') {
      onChange?.(null);
      return;
    }

    // Find the option that matches the selected value
    const option = options.find(opt => String(opt.value) === selectedValue);
    onChange?.(option ? option.value : selectedValue);
  };

  const handleSelectBlur = (event: React.FocusEvent<HTMLSelectElement>) => {
    onBlur?.();
  };

  const renderContent = () => {
    if (mode === ViewMode.READ) {
      // Read-only display
      return (
        <span className="meta-view__select-display" {...fieldProps}>
          {displayValue || <em className="meta-view__empty">No selection</em>}
        </span>
      );
    }

    if (mode === ViewMode.EDIT) {
      // Editable select dropdown
      return (
        <select
          {...fieldProps}
          className="meta-view__select-input"
          value={value != null ? String(value) : ''}
          onChange={handleSelectChange}
          onBlur={handleSelectBlur}
          required={field.isRequired && !allowEmpty}
        >
          {allowEmpty && (
            <option value="">
              {placeholder || emptyLabel}
            </option>
          )}
          {options.map((option, index) => (
            <option
              key={index}
              value={String(option.value)}
              disabled={option.disabled}
            >
              {option.label}
            </option>
          ))}
        </select>
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

SelectView.displayName = 'SelectView';