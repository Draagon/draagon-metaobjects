/**
 * React Query hooks for metadata fetching
 */

import { useQuery, UseQueryOptions } from '@tanstack/react-query';
import { 
  MetaObject, 
  MetaField, 
  MetaViewDefinition, 
  MetaDataPackage 
} from '@/types/metadata';
import { getMetaDataService } from './MetaDataService';

// Query keys for caching
export const metaDataKeys = {
  all: ['metadata'] as const,
  objects: () => [...metaDataKeys.all, 'objects'] as const,
  object: (name: string) => [...metaDataKeys.objects(), name] as const,
  packages: () => [...metaDataKeys.all, 'packages'] as const,
  package: (name: string) => [...metaDataKeys.packages(), name] as const,
  field: (objectName: string, fieldName: string) => 
    [...metaDataKeys.object(objectName), 'field', fieldName] as const,
  view: (objectName: string, fieldName: string, viewName: string) => 
    [...metaDataKeys.field(objectName, fieldName), 'view', viewName] as const,
};

/**
 * Hook to fetch list of available MetaObjects
 */
export function useMetaObjects(
  options?: UseQueryOptions<Array<{ name: string; type: string }>>
) {
  return useQuery({
    queryKey: metaDataKeys.objects(),
    queryFn: () => getMetaDataService().listObjects(),
    staleTime: 5 * 60 * 1000, // 5 minutes
    ...options,
  });
}

/**
 * Hook to fetch a specific MetaObject
 */
export function useMetaObject(
  objectName: string,
  options?: UseQueryOptions<MetaObject>
) {
  return useQuery({
    queryKey: metaDataKeys.object(objectName),
    queryFn: () => getMetaDataService().getMetaObject(objectName),
    enabled: !!objectName,
    staleTime: 10 * 60 * 1000, // 10 minutes - metadata doesn't change often
    ...options,
  });
}

/**
 * Hook to fetch an entire metadata package
 */
export function useMetaDataPackage(
  packageName: string,
  options?: UseQueryOptions<MetaDataPackage>
) {
  return useQuery({
    queryKey: metaDataKeys.package(packageName),
    queryFn: () => getMetaDataService().getMetaDataPackage(packageName),
    enabled: !!packageName,
    staleTime: 10 * 60 * 1000,
    ...options,
  });
}

/**
 * Hook to fetch a specific MetaField
 */
export function useMetaField(
  objectName: string,
  fieldName: string,
  options?: UseQueryOptions<MetaField>
) {
  return useQuery({
    queryKey: metaDataKeys.field(objectName, fieldName),
    queryFn: () => getMetaDataService().getMetaField(objectName, fieldName),
    enabled: !!objectName && !!fieldName,
    staleTime: 10 * 60 * 1000,
    ...options,
  });
}

/**
 * Hook to fetch a specific MetaView definition
 */
export function useMetaView(
  objectName: string,
  fieldName: string,
  viewName: string,
  options?: UseQueryOptions<MetaViewDefinition>
) {
  return useQuery({
    queryKey: metaDataKeys.view(objectName, fieldName, viewName),
    queryFn: () => getMetaDataService().getMetaView(objectName, fieldName, viewName),
    enabled: !!objectName && !!fieldName && !!viewName,
    staleTime: 15 * 60 * 1000, // View definitions are very stable
    ...options,
  });
}

/**
 * Hook to prefetch metadata for better performance
 */
export function usePrefetchMetaData() {
  const queryClient = useQueryClient();
  
  const prefetchMetaObject = (objectName: string) => {
    return queryClient.prefetchQuery({
      queryKey: metaDataKeys.object(objectName),
      queryFn: () => getMetaDataService().getMetaObject(objectName),
      staleTime: 10 * 60 * 1000,
    });
  };
  
  const prefetchMetaDataPackage = (packageName: string) => {
    return queryClient.prefetchQuery({
      queryKey: metaDataKeys.package(packageName),
      queryFn: () => getMetaDataService().getMetaDataPackage(packageName),
      staleTime: 10 * 60 * 1000,
    });
  };
  
  return {
    prefetchMetaObject,
    prefetchMetaDataPackage,
  };
}

// Need to import useQueryClient for prefetch hook
import { useQueryClient } from '@tanstack/react-query';