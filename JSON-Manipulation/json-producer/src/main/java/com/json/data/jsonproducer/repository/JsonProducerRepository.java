package com.json.data.jsonproducer.repository;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;


@Service
@Configuration
//@PropertySource("classpath:filepath")
public class JsonProducerRepository {

	@Value("${filepath}")
	public String fileName;
	
	public String retrieveJson() throws IOException {
		
		File file = ResourceUtils.getFile(fileName);
		//String jsonContent = FileUtils.readFileToString(file,"UTF-8");
		String jsonContent = FileUtils.readFileToString(file,"ISO-8859-1");
		
	    return jsonContent;
	}
}
