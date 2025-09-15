# MetaObjects React Components

This directory contains a comprehensive React implementation of MetaViews that extends the MetaObjects framework to support modern web development with React.js.

## Overview

The React MetaView solution provides a modern, type-safe way to render forms and UI components based on MetaObject metadata definitions. It follows the same architectural principles as the original JSP MetaView implementation but leverages React's component model and modern JavaScript ecosystem.

## Architecture

### Core Components

1. **MetaView Components** (`src/typescript/components/metaviews/`)
   - `BaseMetaView.tsx` - Base component with common functionality
   - `TextView.tsx` - Text input fields
   - `TextAreaView.tsx` - Multi-line text areas
   - `DateView.tsx` - Date picker components
   - `NumericView.tsx` - Numeric input fields
   - `SelectView.tsx` - Dropdown selection components
   - `MetaViewRenderer.tsx` - Main orchestrator component

2. **Form Management** (`src/typescript/components/forms/`)
   - `MetaObjectForm.tsx` - Complete form component for MetaObjects
   - Redux-based state management with `@reduxjs/toolkit`
   - Form validation and error handling

3. **Services** (`src/typescript/services/`)
   - `MetaDataService.ts` - REST API client for metadata
   - React Query hooks for data fetching and caching
   - Type-safe API communication

4. **Java Backend** (`src/main/java/com/draagon/meta/web/react/`)
   - `MetaDataJsonSerializer.java` - Converts MetaData to JSON
   - `MetaDataApiController.java` - REST API endpoints
   - CORS support for development

### MVC Pattern

The implementation follows a clean MVC pattern:

- **Model**: MetaData definitions served as JSON from Java backend
- **View**: React components that render based on metadata
- **Controller**: Redux store manages form state and user interactions

### Technology Stack

- **React 18** with TypeScript for type safety
- **Redux Toolkit** for state management
- **React Query** for data fetching and caching
- **Java Servlet API** for REST endpoints
- **GSON** for JSON serialization
- **Jest** for unit testing

## Key Features

### 1. Metadata-Driven Forms
Forms are automatically generated from MetaObject definitions:
```typescript
<MetaObjectForm
  formId="store-form"
  objectName="Store"
  initialValues={{ name: "", maxTanks: 1 }}
  onSubmit={handleSubmit}
/>
```

### 2. Type Safety
Full TypeScript support with generated types from Java metadata:
```typescript
interface MetaField {
  name: string;
  type: FieldType;
  displayName?: string;
  isRequired?: boolean;
  validators: ValidationRule[];
}
```

### 3. Flexible Rendering
Multiple view modes and customizable components:
```typescript
<MetaViewRenderer
  field={field}
  value={value}
  mode={ViewMode.EDIT}
  viewType="text"
  onChange={handleChange}
/>
```

### 4. Validation Integration
Client-side validation derived from metadata:
```json
{
  "validators": [
    {
      "type": "required",
      "message": "This field is required"
    },
    {
      "type": "length",
      "message": "Must be between 1 and 50 characters",
      "params": { "min": 1, "max": 50 }
    }
  ]
}
```

## API Endpoints

The Java backend provides RESTful endpoints for metadata access:

- `GET /api/metadata/objects` - List all MetaObjects
- `GET /api/metadata/objects/{name}` - Get specific MetaObject
- `GET /api/metadata/packages/{packageName}` - Get entire package
- `GET /api/metadata/fields/{objectName}/{fieldName}` - Get specific field
- `GET /api/metadata/views/{objectName}/{fieldName}/{viewName}` - Get specific view

## Demo Application

The `/demo` directory contains a complete fishstore application demonstrating the React MetaView components:

### Running the Demo

1. **Backend Setup**:
   ```bash
   cd /path/to/metaobjects
   mvn clean compile
   # Deploy WAR to servlet container
   ```

2. **Frontend Setup**:
   ```bash
   cd demo/src/main/webapp/react
   npm install
   npm run dev
   ```

3. **Access**: Navigate to `http://localhost:3000`

### Demo Features

- **Dashboard**: Overview of the fishstore with navigation
- **Store Management**: Full CRUD operations using MetaObject forms
- **Tank Management**: Numeric field demonstrations
- **Breed Management**: Select view with aggression levels
- **Fish Management**: Complex forms with multiple field types

## Development

### Building the Components

```bash
cd web
npm install
npm run build
```

### Running Tests

```bash
npm test
```

### Linting

```bash
npm run lint
npm run lint:fix
```

## Deployment

### Production Build

```bash
npm run build
```

The built components can be published as an npm package and consumed by other React applications:

```bash
npm publish
```

### Integration

To use in other projects:

```bash
npm install @draagon/metaobjects-web-react
```

```typescript
import { MetaObjectForm, initializeMetaDataService } from '@draagon/metaobjects-web-react';
import '@draagon/metaobjects-web-react/dist/styles/main.css';

// Initialize the service
initializeMetaDataService({
  baseUrl: 'https://your-api-server.com'
});

// Use in your components
<MetaObjectForm
  objectName="YourObject"
  onSubmit={handleSubmit}
/>
```

## Benefits

### For Developers
- **Rapid Development**: Forms generated automatically from metadata
- **Type Safety**: Full TypeScript support prevents runtime errors
- **Consistency**: All forms follow the same patterns and styling
- **Maintainability**: Changes to metadata automatically update UI

### For Applications
- **Performance**: React Query caching and optimized re-renders
- **Accessibility**: Built-in ARIA support and semantic HTML
- **Responsive**: Mobile-friendly responsive design
- **Extensible**: Easy to add custom view components

### For Enterprise
- **Standardization**: Consistent UI patterns across applications
- **Governance**: Metadata controls UI behavior and validation
- **Portability**: Same patterns work for C# and TypeScript ports
- **Integration**: Seamless integration with existing MetaObjects backend

## Comparison with JSP Implementation

| Feature | JSP MetaViews | React MetaViews |
|---------|---------------|-----------------|
| **Rendering** | Server-side HTML generation | Client-side component rendering |
| **State Management** | Form POST/GET | Redux + React state |
| **Validation** | Server-side only | Client + server validation |
| **User Experience** | Page reloads | Single-page app experience |
| **Type Safety** | Runtime errors | Compile-time checking |
| **Testing** | Integration tests | Unit + integration tests |
| **Performance** | Server round-trips | Optimistic updates |
| **Mobile Support** | Limited | Responsive design |

## Future Enhancements

1. **Advanced Components**
   - File upload views
   - Rich text editors
   - Data grids for arrays
   - Calendar/scheduling components

2. **Performance Optimizations**
   - Virtual scrolling for large lists
   - Progressive loading
   - Component lazy loading

3. **Developer Experience**
   - CLI tools for code generation
   - Visual form builder
   - Storybook integration
   - Hot reload for metadata changes

4. **Enterprise Features**
   - Role-based field visibility
   - Audit logging
   - Multi-language support
   - Theme customization

This React implementation provides a solid foundation for modern, metadata-driven web applications while maintaining compatibility with the existing MetaObjects ecosystem.