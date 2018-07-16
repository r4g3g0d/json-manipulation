package com.example.jsonconsumer.utils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.jsonconsumer.repository.InterpreterEngine;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JsonManipulationUtils {
	@Value("${client-id}")
	public String clientId;

	@Value("${client-secret}")
	public String clientSecret;

	@Autowired
	StringRegexUtils regexUtils;

	public List<String> replaceEndpointVariables(String endpoint, List<String> endpointVariables) throws IOException {
		Map<String, List<JsonNode>> finalResult = new HashMap<String, List<JsonNode>>();

		List<String> listOfEndpoints = new ArrayList<>();

		for (String variable : endpointVariables) {
			List<JsonNode> eachVariableResult = new ArrayList();

			List<String> splitVariables = regexUtils.splitByDelimiter(variable, ".");
			String dataSetToParse = InterpreterEngine.resultCounter.get(splitVariables.get(0));
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(dataSetToParse);
			Iterator<JsonNode> iter = rootNode.iterator();

			while (iter.hasNext()) {
				JsonNode next = iter.next();
				for (String v : splitVariables.stream().skip(1).collect(Collectors.toList())) {
					if (regexUtils.containsPattern(variable, "\\[.*\\]")) {
						// if split variable contains List Indices (ex : address[0])
						if (regexUtils.containsPattern(v, "\\[.*\\]")) {

							rootNode = next.path(v.replaceAll("\\[.*\\]", ""));
							rootNode = rootNode.get(Integer.parseInt(v.substring(v.indexOf("[") + 1, v.indexOf("]"))));
						} else {
							rootNode = rootNode.path(v);
						}
					} else {
						rootNode = next.path(v);
					}

				}
				eachVariableResult.add(rootNode);

			}

			finalResult.put(variable, eachVariableResult);

		}
		// obtain length of data
		int size = finalResult.get(endpointVariables.get(0)).size();
		for (int i = 0; i < size; i++) {
			String partialEndpoint = endpoint;
			// String partialEndpoint;
			for (int j = 0; j < endpointVariables.size(); j++) {
				String valueToReplaceWith = finalResult.get(endpointVariables.get(j)).get(i).toString();
				if (valueToReplaceWith.contains(" ")) {
					valueToReplaceWith = URLEncoder.encode(valueToReplaceWith, "UTF-8");
				}

				/*
				 * partialEndpoint = partialEndpoint.replace(endpointVariables.get(j),
				 * finalResult.get(endpointVariables.get(j)).get(i).toString());
				 */
				partialEndpoint = partialEndpoint.replace(endpointVariables.get(j), valueToReplaceWith);

			}

			listOfEndpoints.add(partialEndpoint.replaceAll("\\b(\\w*CLIENT_ID\\w*)\\b", clientId)
					.replaceAll("\\b(\\w*CLIENT_SECRET\\w*)\\b", clientSecret).replaceAll("\\}|\\{|\\\"", "")
					.replaceAll(" ", "+"));
		}
		return listOfEndpoints;
	}
}
