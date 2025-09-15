/**
 * MetaObjectForm wrapper that imports from the web module
 * In a real implementation, this would be imported from the published npm package
 */

// For demo purposes, we'll create a simplified version
// In production, this would be: import { MetaObjectForm } from '@draagon/metaobjects-web-react';

import React, { useState, useEffect } from 'react';
import { ViewMode } from '../types/metadata';

interface MetaObjectFormProps {
  formId: string;
  objectName: string;
  initialValues?: Record<string, unknown>;
  mode?: ViewMode;
  onSubmit?: (values: Record<string, unknown>) => Promise<void> | void;
  onCancel?: () => void;
  className?: string;
  fieldOrder?: string[];
  excludeFields?: string[];
  includeFields?: string[];
}

// Simplified demo implementation
export const MetaObjectForm: React.FC<MetaObjectFormProps> = ({
  formId,
  objectName,
  initialValues = {},
  mode = ViewMode.EDIT,
  onSubmit,
  onCancel,
  excludeFields = [],
}) => {
  const [values, setValues] = useState(initialValues);
  const [errors, setErrors] = useState<Record<string, string[]>>({});

  // Simplified field definitions for demo
  const getFieldsForObject = (objectName: string) => {
    switch (objectName) {
      case 'Store':
        return [
          { name: 'name', type: 'text', label: 'Store Name', required: true },
          { name: 'maxTanks', type: 'number', label: 'Maximum Tanks', required: true },
        ];
      case 'Tank':
        return [
          { name: 'num', type: 'number', label: 'Tank Number', required: true },
          { name: 'maxFish', type: 'number', label: 'Maximum Fish', required: true },
        ];
      case 'Breed':
        return [
          { name: 'name', type: 'text', label: 'Breed Name', required: true },
          { name: 'agressionLevel', type: 'select', label: 'Aggression Level', required: true },
        ];
      case 'Fish':
        return [
          { name: 'breedName', type: 'text', label: 'Breed Name', required: true },
          { name: 'length', type: 'number', label: 'Length (inches)', required: true },
          { name: 'weight', type: 'number', label: 'Weight (grams)', required: true },
        ];
      default:
        return [];
    }
  };

  const fields = getFieldsForObject(objectName).filter(
    field => !excludeFields.includes(field.name)
  );

  const handleFieldChange = (fieldName: string, value: any) => {
    setValues(prev => ({ ...prev, [fieldName]: value }));
    // Clear errors when user starts typing
    if (errors[fieldName]) {
      setErrors(prev => ({ ...prev, [fieldName]: [] }));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Simple validation
    const newErrors: Record<string, string[]> = {};
    fields.forEach(field => {
      if (field.required && !values[field.name]) {
        newErrors[field.name] = [`${field.label} is required`];
      }
    });

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    try {
      await onSubmit?.(values);
    } catch (error) {
      console.error('Submit error:', error);
    }
  };

  const renderField = (field: any) => {
    const fieldValue = values[field.name] || '';
    const fieldErrors = errors[field.name] || [];

    if (mode === ViewMode.READ) {
      return (
        <div key={field.name} className="meta-view">
          <label className="meta-view__label">{field.label}</label>
          <div className="meta-view__content">
            <span className="meta-view__text-display">
              {fieldValue || <em className="meta-view__empty">No value</em>}
            </span>
          </div>
        </div>
      );
    }

    return (
      <div key={field.name} className="meta-view">
        <label className="meta-view__label" htmlFor={field.name}>
          {field.label}
          {field.required && <span className="meta-view__required">*</span>}
        </label>
        <div className="meta-view__content">
          {field.type === 'select' && field.name === 'agressionLevel' ? (
            <select
              id={field.name}
              className="meta-view__select-input"
              value={fieldValue}
              onChange={(e) => handleFieldChange(field.name, parseInt(e.target.value))}
            >
              <option value="">Select aggression level...</option>
              {[1,2,3,4,5,6,7,8,9,10].map(level => (
                <option key={level} value={level}>
                  {level} - {level <= 3 ? 'Peaceful' : level <= 6 ? 'Semi-Aggressive' : 'Aggressive'}
                </option>
              ))}
            </select>
          ) : field.type === 'number' ? (
            <input
              id={field.name}
              type="number"
              className="meta-view__numeric-input"
              value={fieldValue}
              onChange={(e) => handleFieldChange(field.name, parseInt(e.target.value))}
            />
          ) : (
            <input
              id={field.name}
              type="text"
              className="meta-view__text-input"
              value={fieldValue}
              onChange={(e) => handleFieldChange(field.name, e.target.value)}
            />
          )}
        </div>
        {fieldErrors.length > 0 && (
          <div className="meta-view__errors">
            {fieldErrors.map((error, index) => (
              <div key={index} className="meta-view__error">{error}</div>
            ))}
          </div>
        )}
      </div>
    );
  };

  return (
    <form className="meta-form" onSubmit={handleSubmit} noValidate>
      <div className="meta-form__fields">
        {fields.map(renderField)}
      </div>

      {mode === ViewMode.EDIT && (
        <div className="meta-form__actions">
          <button type="submit" className="meta-form__submit">
            Save
          </button>
          <button type="button" className="meta-form__reset" onClick={() => setValues(initialValues)}>
            Reset
          </button>
          {onCancel && (
            <button type="button" className="meta-form__cancel" onClick={onCancel}>
              Cancel
            </button>
          )}
        </div>
      )}
    </form>
  );
};