/**
 * MetaData service for demo
 */

export interface MetaDataServiceConfig {
  baseUrl: string;
}

// Simplified service for demo
export function initializeMetaDataService(config: MetaDataServiceConfig) {
  console.log('MetaData service initialized with config:', config);
  // In a real implementation, this would initialize the service from the web module
}