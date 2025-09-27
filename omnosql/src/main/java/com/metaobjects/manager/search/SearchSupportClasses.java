/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.search;

import java.util.List;
import java.util.Map;

/**
 * Supporting classes for search engine operations
 */
public class SearchSupportClasses {

    /**
     * Search result containing documents, scores, and metadata
     */
    public static class SearchResult {
        private final List<SearchHit> hits;
        private final long totalHits;
        private final double maxScore;
        private final Map<String, Object> aggregations;
        private final String scrollId;
        private final long tookMillis;
        
        public SearchResult(List<SearchHit> hits, long totalHits, double maxScore, 
                           Map<String, Object> aggregations, String scrollId, long tookMillis) {
            this.hits = hits;
            this.totalHits = totalHits;
            this.maxScore = maxScore;
            this.aggregations = aggregations;
            this.scrollId = scrollId;
            this.tookMillis = tookMillis;
        }
        
        // Getters
        public List<SearchHit> getHits() { return hits; }
        public long getTotalHits() { return totalHits; }
        public double getMaxScore() { return maxScore; }
        public Map<String, Object> getAggregations() { return aggregations; }
        public String getScrollId() { return scrollId; }
        public long getTookMillis() { return tookMillis; }
        
        @Override
        public String toString() {
            return String.format("SearchResult{totalHits=%d, maxScore=%.3f, took=%dms}", 
                    totalHits, maxScore, tookMillis);
        }
    }

    /**
     * Individual search hit with document, score, and highlights
     */
    public static class SearchHit {
        private final String id;
        private final Object document;
        private final double score;
        private final Map<String, List<String>> highlights;
        private final Map<String, Object> explanation;
        
        public SearchHit(String id, Object document, double score, 
                        Map<String, List<String>> highlights, Map<String, Object> explanation) {
            this.id = id;
            this.document = document;
            this.score = score;
            this.highlights = highlights;
            this.explanation = explanation;
        }
        
        // Getters
        public String getId() { return id; }
        public Object getDocument() { return document; }
        public double getScore() { return score; }
        public Map<String, List<String>> getHighlights() { return highlights; }
        public Map<String, Object> getExplanation() { return explanation; }
        
        @Override
        public String toString() {
            return String.format("SearchHit{id='%s', score=%.3f}", id, score);
        }
    }

    /**
     * Search configuration options
     */
    public static class SearchOptions {
        private Integer from = 0;
        private Integer size = 10;
        private List<String> fields;
        private List<SortField> sortFields;
        private Map<String, Object> highlightOptions;
        private boolean includeExplanation = false;
        private Double minScore;
        private String routing;
        private String preference;
        private Integer timeoutSeconds;
        
        // Builder pattern methods
        public SearchOptions from(Integer from) { this.from = from; return this; }
        public SearchOptions size(Integer size) { this.size = size; return this; }
        public SearchOptions fields(List<String> fields) { this.fields = fields; return this; }
        public SearchOptions sort(List<SortField> sortFields) { this.sortFields = sortFields; return this; }
        public SearchOptions highlight(Map<String, Object> highlightOptions) { this.highlightOptions = highlightOptions; return this; }
        public SearchOptions explain(boolean includeExplanation) { this.includeExplanation = includeExplanation; return this; }
        public SearchOptions minScore(Double minScore) { this.minScore = minScore; return this; }
        public SearchOptions routing(String routing) { this.routing = routing; return this; }
        public SearchOptions preference(String preference) { this.preference = preference; return this; }
        public SearchOptions timeout(Integer timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; return this; }
        
        // Getters
        public Integer getFrom() { return from; }
        public Integer getSize() { return size; }
        public List<String> getFields() { return fields; }
        public List<SortField> getSortFields() { return sortFields; }
        public Map<String, Object> getHighlightOptions() { return highlightOptions; }
        public boolean isIncludeExplanation() { return includeExplanation; }
        public Double getMinScore() { return minScore; }
        public String getRouting() { return routing; }
        public String getPreference() { return preference; }
        public Integer getTimeoutSeconds() { return timeoutSeconds; }
    }

    /**
     * Structured query for complex search operations
     */
    public static class StructuredQuery {
        public enum QueryType {
            MATCH_ALL, MATCH, MULTI_MATCH, TERM, TERMS, RANGE, 
            BOOL, FUZZY, WILDCARD, REGEXP, PREFIX,
            GEO_DISTANCE, GEO_BOUNDING_BOX, GEO_POLYGON
        }
        
        private final QueryType type;
        private final Map<String, Object> parameters;
        private final List<StructuredQuery> mustQueries;
        private final List<StructuredQuery> shouldQueries;
        private final List<StructuredQuery> mustNotQueries;
        private final List<StructuredQuery> filterQueries;
        
        public StructuredQuery(QueryType type, Map<String, Object> parameters) {
            this(type, parameters, null, null, null, null);
        }
        
        public StructuredQuery(QueryType type, Map<String, Object> parameters,
                             List<StructuredQuery> mustQueries, List<StructuredQuery> shouldQueries,
                             List<StructuredQuery> mustNotQueries, List<StructuredQuery> filterQueries) {
            this.type = type;
            this.parameters = parameters;
            this.mustQueries = mustQueries;
            this.shouldQueries = shouldQueries;
            this.mustNotQueries = mustNotQueries;
            this.filterQueries = filterQueries;
        }
        
        // Factory methods for common query types
        public static StructuredQuery matchAll() {
            return new StructuredQuery(QueryType.MATCH_ALL, Map.of());
        }
        
        public static StructuredQuery match(String field, String query) {
            return new StructuredQuery(QueryType.MATCH, Map.of("field", field, "query", query));
        }
        
        public static StructuredQuery term(String field, Object value) {
            return new StructuredQuery(QueryType.TERM, Map.of("field", field, "value", value));
        }
        
        public static StructuredQuery range(String field, Object from, Object to) {
            return new StructuredQuery(QueryType.RANGE, Map.of("field", field, "from", from, "to", to));
        }
        
        // Getters
        public QueryType getType() { return type; }
        public Map<String, Object> getParameters() { return parameters; }
        public List<StructuredQuery> getMustQueries() { return mustQueries; }
        public List<StructuredQuery> getShouldQueries() { return shouldQueries; }
        public List<StructuredQuery> getMustNotQueries() { return mustNotQueries; }
        public List<StructuredQuery> getFilterQueries() { return filterQueries; }
        
        @Override
        public String toString() {
            return String.format("StructuredQuery{type=%s, parameters=%s}", type, parameters);
        }
    }

    /**
     * Sort field specification
     */
    public static class SortField {
        public enum SortOrder { ASC, DESC }
        
        private final String fieldName;
        private final SortOrder order;
        private final String mode; // min, max, sum, avg for multi-value fields
        
        public SortField(String fieldName, SortOrder order) {
            this(fieldName, order, null);
        }
        
        public SortField(String fieldName, SortOrder order, String mode) {
            this.fieldName = fieldName;
            this.order = order;
            this.mode = mode;
        }
        
        public String getFieldName() { return fieldName; }
        public SortOrder getOrder() { return order; }
        public String getMode() { return mode; }
        
        @Override
        public String toString() {
            return String.format("SortField{field='%s', order=%s}", fieldName, order);
        }
    }

    /**
     * Aggregation definition
     */
    public static class Aggregation {
        public enum AggregationType {
            TERMS, DATE_HISTOGRAM, HISTOGRAM, RANGE, STATS, CARDINALITY,
            AVG, MAX, MIN, SUM, VALUE_COUNT, PERCENTILES, GEO_BOUNDS
        }
        
        private final String name;
        private final AggregationType type;
        private final Map<String, Object> parameters;
        private final List<Aggregation> subAggregations;
        
        public Aggregation(String name, AggregationType type, Map<String, Object> parameters) {
            this(name, type, parameters, null);
        }
        
        public Aggregation(String name, AggregationType type, Map<String, Object> parameters, List<Aggregation> subAggregations) {
            this.name = name;
            this.type = type;
            this.parameters = parameters;
            this.subAggregations = subAggregations;
        }
        
        // Getters
        public String getName() { return name; }
        public AggregationType getType() { return type; }
        public Map<String, Object> getParameters() { return parameters; }
        public List<Aggregation> getSubAggregations() { return subAggregations; }
    }

    /**
     * Aggregation result
     */
    public static class AggregationResult {
        private final String name;
        private final Map<String, Object> result;
        private final Map<String, AggregationResult> subResults;
        
        public AggregationResult(String name, Map<String, Object> result, Map<String, AggregationResult> subResults) {
            this.name = name;
            this.result = result;
            this.subResults = subResults;
        }
        
        public String getName() { return name; }
        public Map<String, Object> getResult() { return result; }
        public Map<String, AggregationResult> getSubResults() { return subResults; }
        
        public Object getValue(String key) { return result.get(key); }
        public AggregationResult getSubResult(String name) { return subResults.get(name); }
    }

    // Additional supporting classes...
    public static class Suggestion {
        private final String text;
        private final double score;
        private final Map<String, Object> payload;
        
        public Suggestion(String text, double score, Map<String, Object> payload) {
            this.text = text;
            this.score = score;
            this.payload = payload;
        }
        
        public String getText() { return text; }
        public double getScore() { return score; }
        public Map<String, Object> getPayload() { return payload; }
    }

    public static class IndexOptions {
        private boolean refresh = false;
        private String routing;
        private Integer timeout;
        
        public IndexOptions refresh(boolean refresh) { this.refresh = refresh; return this; }
        public IndexOptions routing(String routing) { this.routing = routing; return this; }
        public IndexOptions timeout(Integer timeout) { this.timeout = timeout; return this; }
        
        public boolean isRefresh() { return refresh; }
        public String getRouting() { return routing; }
        public Integer getTimeout() { return timeout; }
    }

    public static class BulkIndexResult {
        private final int successCount;
        private final int failureCount;
        private final List<String> errors;
        private final long tookMillis;
        
        public BulkIndexResult(int successCount, int failureCount, List<String> errors, long tookMillis) {
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.errors = errors;
            this.tookMillis = tookMillis;
        }
        
        public int getSuccessCount() { return successCount; }
        public int getFailureCount() { return failureCount; }
        public List<String> getErrors() { return errors; }
        public long getTookMillis() { return tookMillis; }
    }

    public static class GeoQuery {
        private final String field;
        private final double lat;
        private final double lon;
        private final String distance;
        
        public GeoQuery(String field, double lat, double lon, String distance) {
            this.field = field;
            this.lat = lat;
            this.lon = lon;
            this.distance = distance;
        }
        
        public String getField() { return field; }
        public double getLat() { return lat; }
        public double getLon() { return lon; }
        public String getDistance() { return distance; }
    }

    public static class MoreLikeThisOptions {
        private Integer minTermFreq = 2;
        private Integer minDocFreq = 5;
        private Integer maxQueryTerms = 25;
        private Integer size = 10;
        
        // Builder methods and getters...
        public MoreLikeThisOptions minTermFreq(Integer minTermFreq) { this.minTermFreq = minTermFreq; return this; }
        public MoreLikeThisOptions minDocFreq(Integer minDocFreq) { this.minDocFreq = minDocFreq; return this; }
        public MoreLikeThisOptions maxQueryTerms(Integer maxQueryTerms) { this.maxQueryTerms = maxQueryTerms; return this; }
        public MoreLikeThisOptions size(Integer size) { this.size = size; return this; }
        
        public Integer getMinTermFreq() { return minTermFreq; }
        public Integer getMinDocFreq() { return minDocFreq; }
        public Integer getMaxQueryTerms() { return maxQueryTerms; }
        public Integer getSize() { return size; }
    }

    public static class ScrollOptions {
        private final String scrollTime;
        private final Integer size;
        
        public ScrollOptions(String scrollTime, Integer size) {
            this.scrollTime = scrollTime;
            this.size = size;
        }
        
        public String getScrollTime() { return scrollTime; }
        public Integer getSize() { return size; }
    }

    public static class AnalysisResult {
        private final List<AnalysisToken> tokens;
        
        public AnalysisResult(List<AnalysisToken> tokens) {
            this.tokens = tokens;
        }
        
        public List<AnalysisToken> getTokens() { return tokens; }
        
        public static class AnalysisToken {
            private final String token;
            private final int startOffset;
            private final int endOffset;
            private final String type;
            private final int position;
            
            public AnalysisToken(String token, int startOffset, int endOffset, String type, int position) {
                this.token = token;
                this.startOffset = startOffset;
                this.endOffset = endOffset;
                this.type = type;
                this.position = position;
            }
            
            public String getToken() { return token; }
            public int getStartOffset() { return startOffset; }
            public int getEndOffset() { return endOffset; }
            public String getType() { return type; }
            public int getPosition() { return position; }
        }
    }

    public static class IndexInfo {
        private final String indexName;
        private final long documentCount;
        private final long deletedDocumentCount;
        private final long sizeInBytes;
        private final Map<String, Object> settings;
        private final Map<String, Object> mappings;
        
        public IndexInfo(String indexName, long documentCount, long deletedDocumentCount, long sizeInBytes,
                        Map<String, Object> settings, Map<String, Object> mappings) {
            this.indexName = indexName;
            this.documentCount = documentCount;
            this.deletedDocumentCount = deletedDocumentCount;
            this.sizeInBytes = sizeInBytes;
            this.settings = settings;
            this.mappings = mappings;
        }
        
        // Getters
        public String getIndexName() { return indexName; }
        public long getDocumentCount() { return documentCount; }
        public long getDeletedDocumentCount() { return deletedDocumentCount; }
        public long getSizeInBytes() { return sizeInBytes; }
        public Map<String, Object> getSettings() { return settings; }
        public Map<String, Object> getMappings() { return mappings; }
    }

    public static class ExplanationResult {
        private final boolean match;
        private final double score;
        private final String description;
        private final Map<String, Object> details;
        
        public ExplanationResult(boolean match, double score, String description, Map<String, Object> details) {
            this.match = match;
            this.score = score;
            this.description = description;
            this.details = details;
        }
        
        public boolean isMatch() { return match; }
        public double getScore() { return score; }
        public String getDescription() { return description; }
        public Map<String, Object> getDetails() { return details; }
    }

    @FunctionalInterface
    public interface SearchResultProcessor {
        void processBatch(SearchResult batch);
    }
}