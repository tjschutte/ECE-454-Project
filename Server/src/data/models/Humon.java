package data.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Humon {
	ObjectMapper mapper;
	private String name;

	public Humon(ObjectMapper mapper, String name) {
		this.mapper = mapper;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String toJson() throws JsonProcessingException {
		return mapper.writeValueAsString(this);
	}
}
