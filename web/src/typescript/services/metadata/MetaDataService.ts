/**
 * Service for fetching metadata from the Java API
 */

import { 
  MetaObject, 
  MetaField, 
  MetaViewDefinition, 
  MetaDataPackage 
} from '@/types/metadata';

export interface MetaDataServiceConfig {
  baseUrl: string;
  timeout?: number;
}

export class MetaDataService {
  private readonly baseUrl: string;
  private readonly timeout: number;

  constructor(config: MetaDataServiceConfig) {
    this.baseUrl = config.baseUrl.replace(/\/$/, ''); // Remove trailing slash
    this.timeout = config.timeout || 10000;
  }

  /**
   * Fetch all available MetaObjects
   */
  async listObjects(): Promise<Array<{ name: string; type: string }>> {
    const response = await this.fetch('/api/metadata/objects');
    const data = await response.json();
    return data.objects;
  }

  /**
   * Fetch a specific MetaObject by name
   */
  async getMetaObject(name: string): Promise<MetaObject> {
    const response = await this.fetch(`/api/metadata/objects/${encodeURIComponent(name)}`);
    return await response.json();
  }

  /**
   * Fetch an entire metadata package
   */
  async getMetaDataPackage(packageName: string): Promise<MetaDataPackage> {
    const response = await this.fetch(`/api/metadata/packages/${encodeURIComponent(packageName)}`);
    return await response.json();
  }

  /**
   * Fetch a specific MetaField
   */
  async getMetaField(objectName: string, fieldName: string): Promise<MetaField> {
    const response = await this.fetch(
      `/api/metadata/fields/${encodeURIComponent(objectName)}/${encodeURIComponent(fieldName)}`
    );
    return await response.json();
  }

  /**
   * Fetch a specific MetaView definition
   */
  async getMetaView(objectName: string, fieldName: string, viewName: string): Promise<MetaViewDefinition> {
    const response = await this.fetch(
      `/api/metadata/views/${encodeURIComponent(objectName)}/${encodeURIComponent(fieldName)}/${encodeURIComponent(viewName)}`
    );
    return await response.json();
  }

  /**
   * Internal fetch wrapper with timeout and error handling
   */
  private async fetch(path: string): Promise<Response> {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), this.timeout);

    try {
      const response = await fetch(`${this.baseUrl}${path}`, {
        signal: controller.signal,
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json',
        },
      });

      clearTimeout(timeoutId);

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`API Error ${response.status}: ${errorText}`);
      }

      return response;
    } catch (error) {
      clearTimeout(timeoutId);
      
      if (error instanceof Error && error.name === 'AbortError') {
        throw new Error('Request timeout');
      }
      
      throw error;
    }
  }
}

// Singleton instance
let metaDataService: MetaDataService | null = null;

export function getMetaDataService(config?: MetaDataServiceConfig): MetaDataService {
  if (!metaDataService && config) {
    metaDataService = new MetaDataService(config);
  }
  
  if (!metaDataService) {
    throw new Error('MetaDataService not initialized. Call with config first.');
  }
  
  return metaDataService;
}

export function initializeMetaDataService(config: MetaDataServiceConfig): MetaDataService {
  metaDataService = new MetaDataService(config);
  return metaDataService;
}