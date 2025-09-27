/**
 * Main form component for editing MetaObject instances
 */

import React, { useEffect, useMemo } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { RootState } from '@/store';
import { 
  initializeForm,
  updateFieldValue,
  setFieldErrors,
  setSubmitting,
  resetForm,
} from '@/store/metaFormSlice';
import { MetaObject, ViewMode } from '@/types/metadata';
import { useMetaObject } from '@/services/metadata/hooks';
import { MetaViewRenderer } from '../metaviews/MetaViewRenderer';
import { cn } from '@/utils/classnames';

export interface MetaObjectFormProps {
  formId: string;
  objectName: string;
  initialValues?: Record<string, unknown>;
  mode?: ViewMode;
  onSubmit?: (values: Record<string, unknown>) => Promise<void> | void;
  onCancel?: () => void;
  className?: string;
  fieldOrder?: string[]; // Control field display order
  excludeFields?: string[]; // Fields to exclude from the form
  includeFields?: string[]; // Only include these fields
}

/**
 * Form component that renders all fields of a MetaObject
 */
export const MetaObjectForm: React.FC<MetaObjectFormProps> = ({
  formId,
  objectName,
  initialValues = {},
  mode = ViewMode.EDIT,
  onSubmit,
  onCancel,
  className,
  fieldOrder,
  excludeFields = [],
  includeFields,
}) => {
  const dispatch = useDispatch();
  
  // Get MetaObject definition
  const { data: metaObject, isLoading: isMetaObjectLoading, error: metaObjectError } = useMetaObject(objectName);
  
  // Get form state from Redux
  const formState = useSelector((state: RootState) => state.metaForm.forms[formId]);
  
  // Initialize form when component mounts
  useEffect(() => {
    if (metaObject && !formState) {
      dispatch(initializeForm({
        formId,
        objectName,
        initialValues,
      }));
    }
  }, [dispatch, formId, objectName, metaObject, initialValues, formState]);

  // Determine which fields to display
  const fieldsToDisplay = useMemo(() => {
    if (!metaObject) return [];
    
    let fields = Object.values(metaObject.fields);
    
    // Filter by includeFields if specified
    if (includeFields && includeFields.length > 0) {
      fields = fields.filter(field => includeFields.includes(field.name));
    }
    
    // Exclude specified fields
    if (excludeFields.length > 0) {
      fields = fields.filter(field => !excludeFields.includes(field.name));
    }
    
    // Sort by fieldOrder if specified
    if (fieldOrder && fieldOrder.length > 0) {
      fields.sort((a, b) => {
        const indexA = fieldOrder.indexOf(a.name);
        const indexB = fieldOrder.indexOf(b.name);
        
        // Fields not in fieldOrder go to the end
        if (indexA === -1 && indexB === -1) return 0;
        if (indexA === -1) return 1;
        if (indexB === -1) return -1;
        
        return indexA - indexB;
      });
    }
    
    return fields;
  }, [metaObject, fieldOrder, excludeFields, includeFields]);

  // Handle field value changes
  const handleFieldChange = (fieldName: string, value: unknown) => {
    dispatch(updateFieldValue({
      formId,
      fieldName,
      value,
    }));
  };

  // Handle field blur (for validation)
  const handleFieldBlur = (fieldName: string) => {
    // Trigger validation here if needed
    // For now, we'll keep it simple
  };

  // Handle form submission
  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    
    if (!formState || !onSubmit) return;
    
    dispatch(setSubmitting({ formId, isSubmitting: true }));
    
    try {
      // Extract values from form state
      const values: Record<string, unknown> = {};
      Object.entries(formState.values).forEach(([fieldName, fieldValue]) => {
        values[fieldName] = fieldValue.value;
      });
      
      await onSubmit(values);
    } catch (error) {
      console.error('Form submission error:', error);
      // Handle submission errors here
    } finally {
      dispatch(setSubmitting({ formId, isSubmitting: false }));
    }
  };

  // Handle form reset
  const handleReset = () => {
    dispatch(resetForm({ formId, initialValues }));
  };

  // Show loading state
  if (isMetaObjectLoading) {
    return (
      <div className="meta-form meta-form--loading">
        <div className="meta-form__spinner">Loading...</div>
      </div>
    );
  }

  // Show error state
  if (metaObjectError) {
    return (
      <div className="meta-form meta-form--error">
        <div className="meta-form__error">
          Error loading object definition: {metaObjectError.message}
        </div>
      </div>
    );
  }

  // Show form not initialized
  if (!formState) {
    return (
      <div className="meta-form meta-form--initializing">
        <div className="meta-form__message">Initializing form...</div>
      </div>
    );
  }

  return (
    <form 
      className={cn('meta-form', className)}
      onSubmit={handleSubmit}
      noValidate
    >
      {/* Global errors */}
      {formState.globalErrors.length > 0 && (
        <div className="meta-form__global-errors">
          {formState.globalErrors.map((error, index) => (
            <div key={index} className="meta-form__global-error">
              {error}
            </div>
          ))}
        </div>
      )}

      {/* Form fields */}
      <div className="meta-form__fields">
        {fieldsToDisplay.map((field) => {
          const fieldValue = formState.values[field.name];
          const fieldMode = fieldValue?.mode || mode;
          
          return (
            <div key={field.name} className="meta-form__field">
              <MetaViewRenderer
                field={field}
                value={fieldValue?.value}
                mode={fieldMode}
                onChange={(value) => handleFieldChange(field.name, value)}
                onBlur={() => handleFieldBlur(field.name)}
                errors={fieldValue?.errors || []}
              />
            </div>
          );
        })}
      </div>

      {/* Form actions */}
      {mode === ViewMode.EDIT && (
        <div className="meta-form__actions">
          <button
            type="submit"
            className="meta-form__submit"
            disabled={formState.isSubmitting || !formState.isValid}
          >
            {formState.isSubmitting ? 'Saving...' : 'Save'}
          </button>
          
          <button
            type="button"
            className="meta-form__reset"
            onClick={handleReset}
            disabled={formState.isSubmitting}
          >
            Reset
          </button>
          
          {onCancel && (
            <button
              type="button"
              className="meta-form__cancel"
              onClick={onCancel}
              disabled={formState.isSubmitting}
            >
              Cancel
            </button>
          )}
        </div>
      )}
    </form>
  );
};

MetaObjectForm.displayName = 'MetaObjectForm';