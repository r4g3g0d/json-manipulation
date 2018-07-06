package com.json.data.jsonproducer.repository;

import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
@Service
public class JsonProducerRepository {

	@Value("${filepath}")
	public String fileName;

	public JSONObject retrieveJson() {
		JSONObject data = null;
		try {
			JSONParser parser = new JSONParser();
			System.out.println(fileName);
			data = (JSONObject) parser.parse(new FileReader(fileName));
			
			// String json = data.toJSONString();
			System.out.println(data);
			return data;
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		return data;
	}
}
