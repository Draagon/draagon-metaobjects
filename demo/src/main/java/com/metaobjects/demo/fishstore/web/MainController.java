/*
 * Copyright (c) 2012 Doug Mealing LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.metaobjects.demo.fishstore.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Main Controller for the Fishstore Web App
 * 
 * @author dmealing
 */
@Controller
public class MainController {
    
    @RequestMapping(value="/home",method=RequestMethod.GET)
    public String showHome() { 
        return "home";
    }
}
