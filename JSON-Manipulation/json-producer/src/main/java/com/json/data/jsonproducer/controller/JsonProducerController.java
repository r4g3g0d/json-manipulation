package com.json.data.jsonproducer.controller;

import java.io.IOException;

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
	public ResponseEntity<String> obtainJson() throws IOException {
		String jsonContent = jsonProducer.retrieveJson();
		return new ResponseEntity<String>(jsonContent, HttpStatus.OK);
	}
	
}
