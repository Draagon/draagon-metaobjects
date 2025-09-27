/**
 * React Date MetaView component - equivalent to Java DateView
 */

import React, { useMemo } from 'react';
import { format, parse, isValid } from 'date-fns';
import { BaseMetaView, useMetaViewLogic } from './base/BaseMetaView';
import { MetaViewProps, ViewMode } from '@/types/metadata';

export interface DateViewProps extends MetaViewProps {
  dateFormat?: string;
  minDate?: Date;
  maxDate?: Date;
  showTime?: boolean;
}

/**
 * Date input MetaView for date fields
 */
export const DateView: React.FC<DateViewProps> = (props) => {
  const {
    field,
    value,
    mode,
    params = {},
    onChange,
    onBlur,
    dateFormat = 'yyyy-MM-dd',
    minDate,
    maxDate,
    showTime = false,
    ...baseProps
  } = props;

  const { isReadOnly, isEditable, fieldProps } = useMetaViewLogic(props);

  // Convert value to Date object
  const dateValue = useMemo(() => {
    if (!value) return null;
    if (value instanceof Date) return value;
    if (typeof value === 'string') {
      const parsed = parse(value, dateFormat, new Date());
      return isValid(parsed) ? parsed : null;
    }
    return null;
  }, [value, dateFormat]);

  // Format date for display
  const displayValue = useMemo(() => {
    if (!dateValue) return '';
    return format(dateValue, showTime ? 'PPp' : 'PP'); // Pretty format
  }, [dateValue, showTime]);

  // Format date for input (HTML date input expects YYYY-MM-DD)
  const inputValue = useMemo(() => {
    if (!dateValue) return '';
    return format(dateValue, 'yyyy-MM-dd');
  }, [dateValue]);

  const handleInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const inputDate = event.target.value;
    if (!inputDate) {
      onChange?.(null);
      return;
    }

    try {
      const parsed = parse(inputDate, 'yyyy-MM-dd', new Date());
      if (isValid(parsed)) {
        onChange?.(parsed);
      }
    } catch (error) {
      // Invalid date, don't update
      console.warn('Invalid date input:', inputDate);
    }
  };

  const handleInputBlur = (event: React.FocusEvent<HTMLInputElement>) => {
    onBlur?.();
  };

  // Get date range constraints
  const minDateString = minDate ? format(minDate, 'yyyy-MM-dd') : undefined;
  const maxDateString = maxDate ? format(maxDate, 'yyyy-MM-dd') : undefined;

  const renderContent = () => {
    if (mode === ViewMode.READ) {
      // Read-only display
      return (
        <span className="meta-view__date-display" {...fieldProps}>
          {displayValue || <em className="meta-view__empty">No date</em>}
        </span>
      );
    }

    if (mode === ViewMode.EDIT) {
      // Editable date input
      return (
        <input
          {...fieldProps}
          type="date"
          className="meta-view__date-input"
          value={inputValue}
          onChange={handleInputChange}
          onBlur={handleInputBlur}
          min={minDateString}
          max={maxDateString}
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

DateView.displayName = 'DateView';