/**
 * Main MetaView Renderer - orchestrates rendering of different MetaView types
 */

import React, { useMemo } from 'react';
import { MetaField, MetaViewProps, ViewMode, FieldType } from '@/types/metadata';
import { TextView } from './TextView';
import { TextAreaView } from './TextAreaView';
import { DateView } from './DateView';
import { NumericView } from './NumericView';
import { SelectView, SelectOption } from './SelectView';

// Registry of available MetaView components
export type MetaViewComponent = React.ComponentType<MetaViewProps>;

export interface MetaViewRegistry {
  [viewType: string]: MetaViewComponent;
}

// Default view registry
const defaultViewRegistry: MetaViewRegistry = {
  text: TextView,
  textarea: TextAreaView,
  date: DateView,
  numeric: NumericView,
  int: NumericView,
  long: NumericView,
  double: NumericView,
  select: SelectView,
};

export interface MetaViewRendererProps {
  field: MetaField;
  value: unknown;
  mode: ViewMode;
  label?: string;
  viewType?: string; // Override default view type
  viewParams?: Record<string, unknown>;
  onChange?: (value: unknown) => void;
  onBlur?: () => void;
  errors?: string[];
  className?: string;
  registry?: MetaViewRegistry; // Custom view registry
}

/**
 * Main renderer that selects and renders the appropriate MetaView component
 */
export const MetaViewRenderer: React.FC<MetaViewRendererProps> = ({
  field,
  value,
  mode,
  label,
  viewType,
  viewParams = {},
  onChange,
  onBlur,
  errors = [],
  className,
  registry = defaultViewRegistry,
}) => {
  // Determine which view component to use
  const ViewComponent = useMemo(() => {
    // First try explicit viewType prop
    if (viewType && registry[viewType]) {
      return registry[viewType];
    }

    // Try field's default view
    if (field.defaultView && registry[field.defaultView]) {
      return registry[field.defaultView];
    }

    // Try field-specific views
    const fieldViews = Object.keys(field.views || {});
    for (const viewName of fieldViews) {
      if (registry[viewName]) {
        return registry[viewName];
      }
    }

    // Fall back to field type mapping
    switch (field.type) {
      case FieldType.STRING:
        // Choose textarea for longer fields
        if (field.length && field.length > 255) {
          return registry.textarea || registry.text;
        }
        return registry.text;
      
      case FieldType.INT:
      case FieldType.LONG:
      case FieldType.DOUBLE:
        return registry.numeric || registry.text;
      
      case FieldType.DATE:
        return registry.date || registry.text;
      
      case FieldType.BOOLEAN:
        return registry.select || registry.text;
      
      case FieldType.OBJECT:
      case FieldType.OBJECT_ARRAY:
        return registry.select || registry.text; // Could be enhanced with object picker
      
      default:
        return registry.text;
    }
  }, [field, viewType, registry]);

  // Prepare props for the selected component
  const componentProps = useMemo((): MetaViewProps => {
    const baseProps: MetaViewProps = {
      field,
      value,
      mode,
      label,
      params: viewParams,
      onChange,
      onBlur,
      errors,
      className,
    };

    // Add field-type specific props
    if (field.type === FieldType.BOOLEAN && ViewComponent === registry.select) {
      // For boolean fields using select, provide Yes/No options
      const booleanOptions: SelectOption[] = [
        { value: true, label: 'Yes' },
        { value: false, label: 'No' },
      ];
      return { ...baseProps, options: booleanOptions } as any;
    }

    return baseProps;
  }, [field, value, mode, label, viewParams, onChange, onBlur, errors, className, ViewComponent, registry]);

  if (!ViewComponent) {
    console.warn(`No MetaView component found for field ${field.name} of type ${field.type}`);
    return (
      <div className="meta-view meta-view--error">
        <span>Unsupported field type: {field.type}</span>
      </div>
    );
  }

  return <ViewComponent {...componentProps} />;
};

/**
 * Hook to create a custom view registry
 */
export function useMetaViewRegistry(customViews: Partial<MetaViewRegistry> = {}): MetaViewRegistry {
  return useMemo(() => ({
    ...defaultViewRegistry,
    ...customViews,
  }), [customViews]);
}

/**
 * Utility to register a new view type globally
 */
export function registerMetaView(viewType: string, component: MetaViewComponent) {
  defaultViewRegistry[viewType] = component;
}

MetaViewRenderer.displayName = 'MetaViewRenderer';