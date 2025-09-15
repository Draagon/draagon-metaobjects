package com.draagon.meta.web.react.api;

import com.draagon.meta.loader.MetaDataRegistry;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.view.MetaView;
import com.draagon.meta.web.react.MetaDataJsonSerializer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

/**
 * REST API Controller for serving MetaData as JSON to React components
 * 
 * Endpoints:
 * - GET /api/metadata/objects - List all MetaObjects
 * - GET /api/metadata/objects/{name} - Get specific MetaObject
 * - GET /api/metadata/packages/{packageName} - Get entire package
 * - GET /api/metadata/fields/{objectName}/{fieldName} - Get specific field
 * - GET /api/metadata/views/{objectName}/{fieldName}/{viewName} - Get specific view
 */
public class MetaDataApiController extends HttpServlet {
    
    private final MetaDataJsonSerializer serializer = new MetaDataJsonSerializer();
    
    /**
     * Helper method to get all MetaObjects from all registered loaders
     */
    private List<MetaObject> getAllMetaObjects() {
        List<MetaObject> allObjects = new ArrayList<>();
        Collection<MetaDataLoader> loaders = MetaDataRegistry.getDataLoaders();
        for (MetaDataLoader loader : loaders) {
            allObjects.addAll(loader.getMetaObjects());
        }
        return allObjects;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Enable CORS for development
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        
        try {
            String jsonResponse = handleGetRequest(pathInfo, request);
            PrintWriter out = response.getWriter();
            out.print(jsonResponse);
            out.flush();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = response.getWriter();
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
            out.flush();
        }
    }
    
    private String handleGetRequest(String pathInfo, HttpServletRequest request) throws Exception {
        String[] pathParts = pathInfo.split("/");
        
        // Remove empty first element from split
        if (pathParts.length > 0 && pathParts[0].isEmpty()) {
            String[] newParts = new String[pathParts.length - 1];
            System.arraycopy(pathParts, 1, newParts, 0, newParts.length);
            pathParts = newParts;
        }
        
        if (pathParts.length == 0) {
            return handleListObjects();
        }
        
        switch (pathParts[0]) {
            case "objects":
                return handleObjectsEndpoint(pathParts);
            case "packages":
                return handlePackagesEndpoint(pathParts);
            case "fields":
                return handleFieldsEndpoint(pathParts);
            case "views":
                return handleViewsEndpoint(pathParts);
            default:
                throw new IllegalArgumentException("Unknown endpoint: " + pathParts[0]);
        }
    }
    
    private String handleObjectsEndpoint(String[] pathParts) throws Exception {
        if (pathParts.length == 1) {
            // List all objects
            return handleListObjects();
        } else if (pathParts.length == 2) {
            // Get specific object
            String objectName = pathParts[1];
            MetaObject metaObject = MetaDataRegistry.findMetaObject(objectName);
            if (metaObject == null) {
                throw new IllegalArgumentException("MetaObject not found: " + objectName);
            }
            return serializer.serializeMetaObject(metaObject);
        } else {
            throw new IllegalArgumentException("Invalid objects endpoint path");
        }
    }
    
    private String handlePackagesEndpoint(String[] pathParts) throws Exception {
        if (pathParts.length != 2) {
            throw new IllegalArgumentException("Package name required");
        }
        
        String packageName = pathParts[1];
        // Get all objects for the package - this is simplified
        // In reality, you'd need a way to filter by package
        List<MetaObject> allObjects = getAllMetaObjects();
        return serializer.serializeMetaDataPackage(packageName, allObjects);
    }
    
    private String handleFieldsEndpoint(String[] pathParts) throws Exception {
        if (pathParts.length != 3) {
            throw new IllegalArgumentException("Object name and field name required");
        }
        
        String objectName = pathParts[1];
        String fieldName = pathParts[2];
        
        MetaObject metaObject = MetaDataRegistry.findMetaObject(objectName);
        if (metaObject == null) {
            throw new IllegalArgumentException("MetaObject not found: " + objectName);
        }
        
        MetaField field = metaObject.getMetaField(fieldName);
        if (field == null) {
            throw new IllegalArgumentException("MetaField not found: " + fieldName);
        }
        
        return serializer.serializeMetaField(field).toString();
    }
    
    private String handleViewsEndpoint(String[] pathParts) throws Exception {
        if (pathParts.length != 4) {
            throw new IllegalArgumentException("Object name, field name, and view name required");
        }
        
        String objectName = pathParts[1];
        String fieldName = pathParts[2];
        String viewName = pathParts[3];
        
        MetaObject metaObject = MetaDataRegistry.findMetaObject(objectName);
        if (metaObject == null) {
            throw new IllegalArgumentException("MetaObject not found: " + objectName);
        }
        
        MetaField field = metaObject.getMetaField(fieldName);
        if (field == null) {
            throw new IllegalArgumentException("MetaField not found: " + fieldName);
        }
        
        MetaView view = field.getView(viewName);
        if (view == null) {
            throw new IllegalArgumentException("MetaView not found: " + viewName);
        }
        
        return serializer.serializeMetaView(view).toString();
    }
    
    private String handleListObjects() {
        StringBuilder json = new StringBuilder();
        json.append("{\"objects\": [");
        
        List<MetaObject> objects = getAllMetaObjects();
        boolean first = true;
        for (MetaObject obj : objects) {
            if (!first) {
                json.append(",");
            }
            json.append("{\"name\": \"").append(obj.getName()).append("\", \"type\": \"").append(obj.getSubTypeName()).append("\"}");
            first = false;
        }
        
        json.append("]}");
        return json.toString();
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        // Handle CORS preflight requests
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}