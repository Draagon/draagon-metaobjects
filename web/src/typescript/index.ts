/**
 * Main export file for MetaObjects React components
 */

// Core types
export * from './types';

// Services
export * from './services/metadata/MetaDataService';
export * from './services/metadata/hooks';

// Components
export * from './components/metaviews/base/BaseMetaView';
export * from './components/metaviews/TextView';
export * from './components/metaviews/TextAreaView';
export * from './components/metaviews/DateView';
export * from './components/metaviews/NumericView';
export * from './components/metaviews/SelectView';
export * from './components/metaviews/MetaViewRenderer';
export * from './components/forms/MetaObjectForm';

// Store
export * from './store';
export * from './store/metaFormSlice';

// Utilities
export * from './utils/classnames';

// Main initialization function
export { initializeMetaDataService } from './services/metadata/MetaDataService';