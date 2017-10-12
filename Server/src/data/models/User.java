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
	ObjectMapper mapper;

	private String userName;
	private String passHash;
	private ArrayList<Integer> party;
	private ArrayList<Integer> encounteredHumons;

	/**
	 * 
	 * @param mapper
	 * @param userName - username
	 * @param passHash - password
	 * @param party - list of instanceIDs
	 * @param encounteredHumons - list of hIDs
	 */
	public User(ObjectMapper mapper, String userName, String passHash, ArrayList<Integer> party,
			ArrayList<Integer> encounteredHumons) {
		this.mapper = mapper;
		this.userName = userName;
		this.passHash = passHash;
		this.party = party; 
		this.encounteredHumons = encounteredHumons;
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
			if (JsonToken.FIELD_NAME.equals(token) && "userName".equals(parser.getCurrentName())) {
				token = parser.nextToken();
				userName = parser.getText();
			}

			// get passHash
			if (JsonToken.FIELD_NAME.equals(token) && "passHash".equals(parser.getCurrentName())) {
				token = parser.nextToken();
				passHash = parser.getText();
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

		}
	}

	public String getUserName() {
		return userName;
	}

	public String getPassHash() {
		return passHash;
	}

	public ArrayList<Integer> getParty() {
		return party;
	}

	public ArrayList<Integer> getEncounteredHumons() {
		return encounteredHumons;
	}

	public String toJson() throws JsonProcessingException {
		return mapper.writeValueAsString(this);
	}

	public String toString() {
		return "[userID: " + userName + ", passHash: " + passHash
				+ ", probably some other stuff at some point as well....]";
	}

}
