package data.models;

import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class User {
	ObjectMapper mapper;
	
	private String userID;
	private String passHash;
	private ArrayList<Humon> party;

	public User(ObjectMapper mapper, String userID, String passHash, ArrayList<Humon> party) {
		this.mapper = mapper;
		this.userID = userID;
		this.passHash = passHash;
		this.party = party;
	}
	
	public String getUserID() {
		return userID;
	}
	
	public String getPassHash() {
		return passHash;
	}
	
	public ArrayList<Humon> getParty() {
		return party;
	}

	
	public String toJson() throws JsonProcessingException {
		return mapper.writeValueAsString(this);
	}
	
}
