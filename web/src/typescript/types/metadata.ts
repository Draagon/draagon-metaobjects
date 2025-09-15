/**
 * Core metadata type definitions for React MetaViews
 * These mirror the Java MetaData hierarchy but are optimized for JSON serialization
 */

export enum ViewMode {
  READ = 'READ',
  EDIT = 'EDIT', 
  HIDE = 'HIDE'
}

export enum FieldType {
  STRING = 'string',
  INT = 'int',
  LONG = 'long',
  DOUBLE = 'double',
  BOOLEAN = 'boolean',
  DATE = 'date',
  OBJECT = 'object',
  OBJECT_ARRAY = 'objectArray'
}

export interface MetaAttribute {
  name: string;
  value: unknown;
  type?: string;
}

export interface ValidationRule {
  type: string;
  message: string;
  params?: Record<string, unknown>;
}

export interface MetaField {
  name: string;
  type: FieldType;
  displayName?: string;
  description?: string;
  length?: number;
  isKey?: boolean;
  isRequired?: boolean;
  auto?: string;
  objectRef?: string;
  dbColumn?: string;
  defaultView?: string;
  attributes: Record<string, MetaAttribute>;
  validators: ValidationRule[];
  views: Record<string, MetaViewDefinition>;
}

export interface MetaObject {
  name: string;
  type?: string;
  super?: string;
  displayName?: string;
  description?: string;
  className?: string;
  dbTable?: string;
  attributes: Record<string, MetaAttribute>;
  fields: Record<string, MetaField>;
}

export interface MetaViewDefinition {
  name: string;
  type: string; // 'text', 'textarea', 'date', 'select', etc.
  subtype?: string;
  fieldName?: string;
  attributes: Record<string, MetaAttribute>;
  validation?: ValidationRule[];
}

export interface MetaDataPackage {
  name: string;
  version?: string;
  description?: string;
  objects: Record<string, MetaObject>;
  types?: Record<string, unknown>;
}

/**
 * Runtime field value and state
 */
export interface FieldValue {
  value: unknown;
  displayValue?: string;
  isValid: boolean;
  errors: string[];
  touched: boolean;
  mode: ViewMode;
}

/**
 * Form state for an entire MetaObject instance
 */
export interface ObjectFormState {
  objectName: string;
  values: Record<string, FieldValue>;
  isValid: boolean;
  isDirty: boolean;
  isSubmitting: boolean;
  globalErrors: string[];
}

/**
 * Parameters passed to MetaView components
 */
export interface ViewParams {
  styleClass?: string;
  onChange?: string;
  isReadOnly?: boolean;
  width?: string | number;
  height?: string | number;
  [key: string]: unknown;
}

/**
 * Props for React MetaView components
 */
export interface MetaViewProps {
  field: MetaField;
  value: unknown;
  mode: ViewMode;
  label?: string;
  params?: ViewParams;
  onChange?: (value: unknown) => void;
  onBlur?: () => void;
  errors?: string[];
  className?: string;
}

/**
 * Configuration for the MetaView renderer
 */
export interface RendererConfig {
  apiBaseUrl: string;
  defaultDateFormat: string;
  validationMode: 'onChange' | 'onBlur' | 'onSubmit';
  theme?: string;
}