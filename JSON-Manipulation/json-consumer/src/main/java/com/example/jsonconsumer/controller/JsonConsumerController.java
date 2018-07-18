package com.example.jsonconsumer.controller;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.jsonconsumer.model.Company;
import com.example.jsonconsumer.repository.CommandInterpreter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class JsonConsumerController {

	@Autowired
	CommandInterpreter commandInterpreter;

	@RequestMapping(value = "commands", method = RequestMethod.GET)
	private ResponseEntity<List<String>> interpretCommands() throws IOException {
		// List<String> commands = commandInterpreter.retrieveCommands();
		commandInterpreter.interpretCommands();
		// return new ResponseEntity<List<String>>(commands, HttpStatus.OK);
		return null;
	}

	@RequestMapping(value = "fetch-json", method = RequestMethod.GET)
	private ResponseEntity<String> fetchJson() {

		RestTemplate restTemplate = new RestTemplate();
		String endpoint = "http://localhost:8080/get-json";
		ResponseEntity<String> jsonContent;
		try {
			RequestEntity<Object> request = new RequestEntity<>(HttpMethod.GET, URI.create(endpoint));
			jsonContent = restTemplate.exchange(request, String.class);

			// ------

			ObjectMapper objectMapper = new ObjectMapper();
			try {
				JsonNode rootNode = objectMapper.readTree(jsonContent.getBody());
				Map<String, Company> companyMap = new HashMap<String, Company>();

				JsonNode companiesList = rootNode.path("result").path("companies").path("company");
				Iterator<JsonNode> iter = companiesList.iterator();

				while (iter.hasNext()) {
					JsonNode nextCompany = iter.next();
					String id = nextCompany.path("id").toString();

					JsonNode addressArray = nextCompany.path("address").get(0);
					Double coordinateNorth = addressArray.path("coordinate").path("north").asDouble();
					Double coordinateEast = addressArray.path("coordinate").path("east").asDouble();
					String displayName = nextCompany.path("displayName").toString();
					companyMap.put(id, new Company(coordinateNorth, coordinateEast, displayName));

				}
				System.out.println(companyMap);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// ------

		} catch (HttpClientErrorException e) {
			throw new RuntimeException("it was not possible to retrieve user profile");
		}
		return jsonContent;
	}

	@RequestMapping(value = "address-info", method = RequestMethod.GET)
	private ResponseEntity<HashMap<String, Company>> getCompanyAddressInfo() {
		ResponseEntity<String> jsonContent = fetchJson();

		return null;

	}
}
