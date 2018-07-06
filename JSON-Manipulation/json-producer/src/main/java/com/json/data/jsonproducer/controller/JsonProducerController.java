package com.json.data.jsonproducer.controller;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.json.data.jsonproducer.repository.JsonProducerRepository;

@Controller
public class JsonProducerController {

	@Autowired
	JsonProducerRepository jsonProducer;

	@RequestMapping(value = "get-json", method = RequestMethod.GET)
	public ResponseEntity<JSONObject> obtainJson() {
		JSONObject jsonContent = jsonProducer.retrieveJson();
		System.out.println("beb");
		return new ResponseEntity<JSONObject>(jsonContent, HttpStatus.OK);
	}
	@RequestMapping(value = "whatever_path", method = RequestMethod.GET)
	public ResponseEntity<String> getResult() {
	    return new ResponseEntity<>("Hello World", HttpStatus.OK);
	}
}
