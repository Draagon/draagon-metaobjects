package com.metaobjects.demo.fishstore.api;

import com.metaobjects.demo.fishstore.service.FishstoreService;
import com.metaobjects.demo.fishstore.domain.Store;
import com.metaobjects.demo.fishstore.domain.Breed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring REST API Controller for CRUD operations on fishstore data
 * 
 * Endpoints:
 * - GET /api/data/stores - Get all stores
 * - GET /api/data/breeds - Get all breeds
 */
@Controller
@RequestMapping("/api/data")
public class FishstoreDataController {
    
    @Autowired
    private FishstoreService fishstoreService;

    /**
     * Enable CORS for development
     */
    @CrossOrigin(origins = "*")
    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    /**
     * Get all stores
     */
    @GetMapping("/stores")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public Map<String, Object> getAllStores(HttpServletResponse response) {
        setCorsHeaders(response);
        
        Collection<Store> stores = fishstoreService.getAllStores();
        
        Map<String, Object> result = new HashMap<>();
        result.put("stores", stores);
        result.put("count", stores.size());
        return result;
    }

    /**
     * Get all breeds
     */
    @GetMapping("/breeds")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public Map<String, Object> getAllBreeds(HttpServletResponse response) {
        setCorsHeaders(response);
        
        Collection<Breed> breeds = fishstoreService.getAllBreeds();
        
        Map<String, Object> result = new HashMap<>();
        result.put("breeds", breeds);
        result.put("count", breeds.size());
        return result;
    }

    /**
     * Handle OPTIONS requests for CORS
     */
    @RequestMapping(method = RequestMethod.OPTIONS)
    @CrossOrigin(origins = "*")
    public void handleOptions(HttpServletResponse response) {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}