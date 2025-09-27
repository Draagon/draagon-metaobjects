/**
 * Redux Toolkit slice for managing MetaObject form state
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { ObjectFormState, FieldValue, ViewMode } from '@/types/metadata';

interface MetaFormState {
  forms: Record<string, ObjectFormState>;
  activeFormId: string | null;
}

const initialState: MetaFormState = {
  forms: {},
  activeFormId: null,
};

export const metaFormSlice = createSlice({
  name: 'metaForm',
  initialState,
  reducers: {
    // Initialize a new form
    initializeForm: (state, action: PayloadAction<{
      formId: string;
      objectName: string;
      initialValues?: Record<string, unknown>;
    }>) => {
      const { formId, objectName, initialValues = {} } = action.payload;
      
      const formState: ObjectFormState = {
        objectName,
        values: {},
        isValid: true,
        isDirty: false,
        isSubmitting: false,
        globalErrors: [],
      };

      // Initialize field values
      Object.entries(initialValues).forEach(([fieldName, value]) => {
        formState.values[fieldName] = {
          value,
          displayValue: String(value || ''),
          isValid: true,
          errors: [],
          touched: false,
          mode: ViewMode.EDIT,
        };
      });

      state.forms[formId] = formState;
      state.activeFormId = formId;
    },

    // Set active form
    setActiveForm: (state, action: PayloadAction<string>) => {
      state.activeFormId = action.payload;
    },

    // Update field value
    updateFieldValue: (state, action: PayloadAction<{
      formId: string;
      fieldName: string;
      value: unknown;
    }>) => {
      const { formId, fieldName, value } = action.payload;
      const form = state.forms[formId];
      
      if (!form) return;

      if (!form.values[fieldName]) {
        form.values[fieldName] = {
          value: null,
          displayValue: '',
          isValid: true,
          errors: [],
          touched: false,
          mode: ViewMode.EDIT,
        };
      }

      form.values[fieldName].value = value;
      form.values[fieldName].displayValue = String(value || '');
      form.values[fieldName].touched = true;
      form.isDirty = true;
    },

    // Set field mode
    setFieldMode: (state, action: PayloadAction<{
      formId: string;
      fieldName: string;
      mode: ViewMode;
    }>) => {
      const { formId, fieldName, mode } = action.payload;
      const form = state.forms[formId];
      
      if (!form || !form.values[fieldName]) return;

      form.values[fieldName].mode = mode;
    },

    // Set field errors
    setFieldErrors: (state, action: PayloadAction<{
      formId: string;
      fieldName: string;
      errors: string[];
    }>) => {
      const { formId, fieldName, errors } = action.payload;
      const form = state.forms[formId];
      
      if (!form) return;

      if (!form.values[fieldName]) {
        form.values[fieldName] = {
          value: null,
          displayValue: '',
          isValid: errors.length === 0,
          errors,
          touched: false,
          mode: ViewMode.EDIT,
        };
      } else {
        form.values[fieldName].errors = errors;
        form.values[fieldName].isValid = errors.length === 0;
      }

      // Update form validity
      form.isValid = Object.values(form.values).every(field => field.isValid);
    },

    // Set global form errors
    setGlobalErrors: (state, action: PayloadAction<{
      formId: string;
      errors: string[];
    }>) => {
      const { formId, errors } = action.payload;
      const form = state.forms[formId];
      
      if (!form) return;

      form.globalErrors = errors;
      form.isValid = errors.length === 0 && Object.values(form.values).every(field => field.isValid);
    },

    // Set submitting state
    setSubmitting: (state, action: PayloadAction<{
      formId: string;
      isSubmitting: boolean;
    }>) => {
      const { formId, isSubmitting } = action.payload;
      const form = state.forms[formId];
      
      if (!form) return;

      form.isSubmitting = isSubmitting;
    },

    // Reset form to initial state
    resetForm: (state, action: PayloadAction<{
      formId: string;
      initialValues?: Record<string, unknown>;
    }>) => {
      const { formId, initialValues = {} } = action.payload;
      const form = state.forms[formId];
      
      if (!form) return;

      // Reset all fields
      Object.keys(form.values).forEach(fieldName => {
        const initialValue = initialValues[fieldName];
        form.values[fieldName] = {
          value: initialValue,
          displayValue: String(initialValue || ''),
          isValid: true,
          errors: [],
          touched: false,
          mode: ViewMode.EDIT,
        };
      });

      form.isDirty = false;
      form.isValid = true;
      form.isSubmitting = false;
      form.globalErrors = [];
    },

    // Remove form
    removeForm: (state, action: PayloadAction<string>) => {
      const formId = action.payload;
      delete state.forms[formId];
      
      if (state.activeFormId === formId) {
        state.activeFormId = null;
      }
    },

    // Bulk update field values
    bulkUpdateFields: (state, action: PayloadAction<{
      formId: string;
      updates: Record<string, {
        value?: unknown;
        errors?: string[];
        mode?: ViewMode;
      }>;
    }>) => {
      const { formId, updates } = action.payload;
      const form = state.forms[formId];
      
      if (!form) return;

      Object.entries(updates).forEach(([fieldName, update]) => {
        if (!form.values[fieldName]) {
          form.values[fieldName] = {
            value: null,
            displayValue: '',
            isValid: true,
            errors: [],
            touched: false,
            mode: ViewMode.EDIT,
          };
        }

        const field = form.values[fieldName];
        
        if (update.value !== undefined) {
          field.value = update.value;
          field.displayValue = String(update.value || '');
          field.touched = true;
          form.isDirty = true;
        }
        
        if (update.errors !== undefined) {
          field.errors = update.errors;
          field.isValid = update.errors.length === 0;
        }
        
        if (update.mode !== undefined) {
          field.mode = update.mode;
        }
      });

      // Update form validity
      form.isValid = form.globalErrors.length === 0 && 
        Object.values(form.values).every(field => field.isValid);
    },
  },
});

export const {
  initializeForm,
  setActiveForm,
  updateFieldValue,
  setFieldMode,
  setFieldErrors,
  setGlobalErrors,
  setSubmitting,
  resetForm,
  removeForm,
  bulkUpdateFields,
} = metaFormSlice.actions;

export default metaFormSlice.reducer;