/**
 * Tests for TextView component
 */

import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { TextView } from '../TextView';
import { ViewMode, FieldType } from '@/types/metadata';

const mockField = {
  name: 'testField',
  type: FieldType.STRING,
  displayName: 'Test Field',
  length: 50,
  isRequired: false,
  attributes: {},
  validators: [],
  views: {},
};

describe('TextView', () => {
  it('renders in READ mode', () => {
    render(
      <TextView
        field={mockField}
        value="Test Value"
        mode={ViewMode.READ}
      />
    );

    expect(screen.getByText('Test Field')).toBeInTheDocument();
    expect(screen.getByText('Test Value')).toBeInTheDocument();
  });

  it('renders in EDIT mode', () => {
    render(
      <TextView
        field={mockField}
        value="Test Value"
        mode={ViewMode.EDIT}
      />
    );

    const input = screen.getByDisplayValue('Test Value');
    expect(input).toBeInTheDocument();
    expect(input).toHaveAttribute('type', 'text');
  });

  it('calls onChange when value changes', () => {
    const mockOnChange = jest.fn();
    
    render(
      <TextView
        field={mockField}
        value="Initial Value"
        mode={ViewMode.EDIT}
        onChange={mockOnChange}
      />
    );

    const input = screen.getByDisplayValue('Initial Value');
    fireEvent.change(input, { target: { value: 'New Value' } });

    expect(mockOnChange).toHaveBeenCalledWith('New Value');
  });

  it('displays errors', () => {
    render(
      <TextView
        field={mockField}
        value="Test Value"
        mode={ViewMode.EDIT}
        errors={['This field is required']}
      />
    );

    expect(screen.getByText('This field is required')).toBeInTheDocument();
  });

  it('does not render when HIDE mode', () => {
    const { container } = render(
      <TextView
        field={mockField}
        value="Test Value"
        mode={ViewMode.HIDE}
      />
    );

    expect(container.firstChild).toBeNull();
  });

  it('shows required indicator', () => {
    const requiredField = { ...mockField, isRequired: true };
    
    render(
      <TextView
        field={requiredField}
        value="Test Value"
        mode={ViewMode.EDIT}
      />
    );

    expect(screen.getByText('*')).toBeInTheDocument();
  });

  it('respects maxLength attribute', () => {
    render(
      <TextView
        field={mockField}
        value="Test Value"
        mode={ViewMode.EDIT}
      />
    );

    const input = screen.getByDisplayValue('Test Value');
    expect(input).toHaveAttribute('maxLength', '50');
  });
});