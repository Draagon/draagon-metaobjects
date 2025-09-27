/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.search;

import com.metaobjects.MetaDataException;
import com.metaobjects.object.MetaObject;
import com.metaobjects.manager.ObjectConnection;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.metaobjects.manager.search.SearchSupportClasses.*;

/**
 * Interface for search engine operations beyond standard ObjectManager capabilities.
 * Supports Elasticsearch, Apache Solr, Amazon CloudSearch, and similar search engines.
 */
public interface SearchOperations {

    /**
     * Performs full-text search with query string
     * @param c Connection to the search engine
     * @param mc MetaObject describing the index/collection
     * @param queryString Search query string
     * @param searchOptions Search configuration options
     * @return Search results with scores and highlights
     */
    SearchResult search(ObjectConnection c, MetaObject mc, String queryString, SearchOptions searchOptions) throws MetaDataException;

    /**
     * Performs structured query with multiple criteria
     * @param c Connection to the search engine
     * @param mc MetaObject describing the index/collection
     * @param query Structured search query
     * @return Search results
     */
    SearchResult searchStructured(ObjectConnection c, MetaObject mc, StructuredQuery query) throws MetaDataException;

    /**
     * Creates or updates a document in the search index
     * @param c Connection to the search engine
     * @param mc MetaObject describing the index
     * @param documentId Unique identifier for the document
     * @param document Document content to index
     * @param indexOptions Indexing options (refresh, routing, etc.)
     */
    void indexDocument(ObjectConnection c, MetaObject mc, String documentId, Object document, IndexOptions indexOptions) throws MetaDataException;

    /**
     * Bulk indexes multiple documents
     * @param c Connection to the search engine
     * @param mc MetaObject describing the index
     * @param documents Map of document ID to document content
     * @param indexOptions Bulk indexing options
     * @return Results of the bulk operation
     */
    BulkIndexResult bulkIndex(ObjectConnection c, MetaObject mc, Map<String, Object> documents, IndexOptions indexOptions) throws MetaDataException;

    /**
     * Deletes a document from the search index
     * @param c Connection to the search engine
     * @param mc MetaObject describing the index
     * @param documentId Document ID to delete
     */
    void deleteDocument(ObjectConnection c, MetaObject mc, String documentId) throws MetaDataException;

    /**
     * Deletes multiple documents matching a query
     * @param c Connection to the search engine
     * @param mc MetaObject describing the index
     * @param deleteQuery Query to match documents for deletion
     * @return Number of documents deleted
     */
    long deleteByQuery(ObjectConnection c, MetaObject mc, StructuredQuery deleteQuery) throws MetaDataException;

    /**
     * Creates a search index with mapping/schema
     * @param c Connection to the search engine
     * @param mc MetaObject describing the index
     * @param indexMapping Field mappings and analysis settings
     * @param indexSettings Index configuration (shards, replicas, analyzers, etc.)
     */
    void createIndex(ObjectConnection c, MetaObject mc, Map<String, Object> indexMapping, Map<String, Object> indexSettings) throws MetaDataException;

    /**
     * Deletes a search index
     * @param c Connection to the search engine
     * @param mc MetaObject describing the index to delete
     */
    void deleteIndex(ObjectConnection c, MetaObject mc) throws MetaDataException;

    /**
     * Performs aggregations on search results
     * @param c Connection to the search engine
     * @param mc MetaObject describing the index
     * @param query Query to filter documents
     * @param aggregations Aggregation definitions
     * @return Aggregation results
     */
    Map<String, AggregationResult> aggregate(ObjectConnection c, MetaObject mc, StructuredQuery query, List<Aggregation> aggregations) throws MetaDataException;

    /**
     * Suggests completions based on partial input
     * @param c Connection to the search engine
     * @param mc MetaObject describing the index
     * @param field Field to get suggestions from
     * @param prefix Partial text to complete
     * @param size Maximum number of suggestions
     * @return List of suggested completions
     */
    List<Suggestion> suggest(ObjectConnection c, MetaObject mc, String field, String prefix, int size) throws MetaDataException;

    /**
     * Performs "More Like This" search
     * @param c Connection to the search engine
     * @param mc MetaObject describing the index
     * @param documentId ID of the reference document
     * @param fields Fields to consider for similarity
     * @param options MLT search options
     * @return Documents similar to the reference
     */
    SearchResult moreLikeThis(ObjectConnection c, MetaObject mc, String documentId, List<String> fields, MoreLikeThisOptions options) throws MetaDataException;

    /**
     * Performs geospatial search
     * @param c Connection to the search engine
     * @param mc MetaObject describing the index
     * @param geoQuery Geospatial query parameters
     * @param searchOptions Additional search options
     * @return Search results within the geographic criteria
     */
    SearchResult geoSearch(ObjectConnection c, MetaObject mc, GeoQuery geoQuery, SearchOptions searchOptions) throws MetaDataException;

    /**
     * Creates a percolator query for real-time matching
     * @param c Connection to the search engine
     * @param mc MetaObject describing the percolator index
     * @param queryId Unique identifier for the stored query
     * @param query Query to store for percolation
     */
    void createPercolatorQuery(ObjectConnection c, MetaObject mc, String queryId, StructuredQuery query) throws MetaDataException;

    /**
     * Tests a document against percolator queries
     * @param c Connection to the search engine
     * @param mc MetaObject describing the percolator index
     * @param document Document to test
     * @return List of matching percolator query IDs
     */
    List<String> percolate(ObjectConnection c, MetaObject mc, Object document) throws MetaDataException;

    /**
     * Analyzes text using the search engine's text analysis
     * @param c Connection to the search engine
     * @param analyzer Name of the analyzer to use
     * @param text Text to analyze
     * @return Analysis results (tokens, positions, etc.)
     */
    AnalysisResult analyzeText(ObjectConnection c, String analyzer, String text) throws MetaDataException;

    /**
     * Gets detailed information about an index
     * @param c Connection to the search engine
     * @param mc MetaObject describing the index
     * @return Index statistics and configuration
     */
    IndexInfo getIndexInfo(ObjectConnection c, MetaObject mc) throws MetaDataException;

    /**
     * Optimizes/forces merge of index segments
     * @param c Connection to the search engine
     * @param mc MetaObject describing the index
     * @param maxSegments Target number of segments (null for default)
     */
    void optimizeIndex(ObjectConnection c, MetaObject mc, Integer maxSegments) throws MetaDataException;

    /**
     * Refreshes the index to make recent changes searchable
     * @param c Connection to the search engine
     * @param mc MetaObject describing the index
     */
    void refreshIndex(ObjectConnection c, MetaObject mc) throws MetaDataException;

    /**
     * Performs asynchronous search
     * @param c Connection to the search engine
     * @param mc MetaObject describing the index
     * @param queryString Search query string
     * @param searchOptions Search options
     * @return CompletableFuture containing search results
     */
    CompletableFuture<SearchResult> searchAsync(ObjectConnection c, MetaObject mc, String queryString, SearchOptions searchOptions) throws MetaDataException;

    /**
     * Scrolls through large result sets efficiently
     * @param c Connection to the search engine
     * @param mc MetaObject describing the index
     * @param query Search query
     * @param scrollOptions Scroll configuration
     * @param processor Function to process each batch of results
     */
    void scroll(ObjectConnection c, MetaObject mc, StructuredQuery query, ScrollOptions scrollOptions, SearchResultProcessor processor) throws MetaDataException;

    /**
     * Explains why a document matches (or doesn't match) a query
     * @param c Connection to the search engine
     * @param mc MetaObject describing the index
     * @param documentId Document to explain
     * @param query Query to explain against
     * @return Detailed explanation of the score calculation
     */
    ExplanationResult explain(ObjectConnection c, MetaObject mc, String documentId, StructuredQuery query) throws MetaDataException;
}