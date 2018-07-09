package com.example.jsonconsumer.controller;

import java.net.URI;

import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class JsonConsumerController {
	
	@RequestMapping(value = "fetch-json", method = RequestMethod.GET)
	private ResponseEntity<String> fetchJson(ModelAndView mv, String token) {
        
		RestTemplate restTemplate = new RestTemplate();
        String endpoint = "http://localhost:8080/get-json";
        ResponseEntity<String> jsonContent;
        try {
            RequestEntity<Object> request = new RequestEntity<>( HttpMethod.GET, URI.create(endpoint));
            jsonContent = restTemplate.exchange(request, String.class);  
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("it was not possible to retrieve user profile");
        }
        return jsonContent;
    }
}
