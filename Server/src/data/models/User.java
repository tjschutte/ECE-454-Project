package data.models;

import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

public class User {
	/**
	 * TODO: Make sure constructors sanitize data.
	 */
	
	ObjectMapper mapper;

	private String email;
	private String password;
	private ArrayList<Integer> party;
	private ArrayList<Integer> encounteredHumons;
	private int hCount;

	/**
	 * 
	 * @param mapper
	 * @param email - email
	 * @param password - password
	 * @param party - list of instanceIDs
	 * @param encounteredHumons - list of hIDs
	 * @param hCount - number of humons encountered
	 */
	public User(ObjectMapper mapper, String email, String password, ArrayList<Integer> party,
			ArrayList<Integer> encounteredHumons, int hCount) {
		this.mapper = mapper;
		this.email = email;
		this.password = password;
		this.party = party; 
		this.encounteredHumons = encounteredHumons;
		this.hCount = hCount;
	}

	public User(ObjectMapper mapper, String json) throws JsonParseException, IOException {
		this.mapper = mapper;
		// get an instance of the json parser from the json factory
		JsonFactory factory = new JsonFactory();
		JsonParser parser = factory.createParser(json);

		// continue parsing the token till the end of input is reached
		while (!parser.isClosed()) {
			// get the token
			JsonToken token = parser.nextToken();
			// if its the last token then we are done
			if (token == null) {
				break;
			}
			
			// get userID
			if (JsonToken.FIELD_NAME.equals(token) && "email".equals(parser.getCurrentName())) {
				token = parser.nextToken();
				email = parser.getText();
			}

			// get passHash
			if (JsonToken.FIELD_NAME.equals(token) && "password".equals(parser.getCurrentName())) {
				token = parser.nextToken();
				password = parser.getText();
			}

			// get Party
			if (JsonToken.FIELD_NAME.equals(token) && "party".equals(parser.getCurrentName())) {
				if (parser.nextToken() != JsonToken.START_ARRAY) {
					throw new IllegalStateException("Expected an array");
				}
				party = new ArrayList<Integer>();
				while (parser.nextToken() != JsonToken.END_ARRAY) {
					party.add(new Integer(parser.getIntValue()));
				}
			}

			// get Party
			if (JsonToken.FIELD_NAME.equals(token) && "encounteredHumons".equals(parser.getCurrentName())) {
				if (parser.nextToken() != JsonToken.START_ARRAY) {
					throw new IllegalStateException("Expected an array");
				}
				encounteredHumons = new ArrayList<Integer>();
				while (parser.nextToken() != JsonToken.END_ARRAY) {
					encounteredHumons.add(new Integer(parser.getIntValue()));
				}
			}
			
			if (JsonToken.FIELD_NAME.equals(token) && "hCount".equals(parser.getCurrentName())) {
				token = parser.nextToken();
				hCount = Integer.parseInt(parser.getText());
				
			}

		}
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public ArrayList<Integer> getParty() {
		return party;
	}

	public ArrayList<Integer> getEncounteredHumons() {
		return encounteredHumons;
	}

	public int getHcount(int hCount) {
		return hCount;
	}

	public String toJson() throws JsonProcessingException {
		return mapper.writeValueAsString(this);
	}

	public String toSqlValueString() {
		String obj = "";
		obj += "'" + email + "',";
		obj += "'" + password + "',";
		obj += "'" + party + "',";
		obj += "'" + encounteredHumons + "',";
		obj += "'" + hCount + "'";
		
		return obj;
	}

}
