package data.models;

import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Humon {
	ObjectMapper mapper;
	private String name; 			// humon name
	private String image;			// image of humon (Base64 representation for serialization purposes)
	private String hID; 			// hID will map a humon to is details in storage (picture, name, moves, etc)
	private String uID; 			// uID will map a humon instance to a user's party
	private String iID;				// iID will map a humon to an instance. iID will be a concatination of hID, uID and a 3 digit count
	private String hp; 				// the health of a humon.  Only applies if iID is not null
	private ArrayList<Move> moves; // list of moves a humon can perform

	public Humon(ObjectMapper mapper, String name, String image, String hID, String uID, String hp, String iID, ArrayList<Move> moves) {
		this.mapper = mapper;
		this.name = name;	// CANNOT BE NULL
		this.image = image; // CANNOT BE NULL
		this.hID = hID; 	// CANNONT BE NULL
		this.uID = uID; 	// CAN BE NULL
		this.hp = hp; 		// CAN BE NULL
		this.iID = iID;		// CAN BE NULL
		this.moves = moves; // CANNOT BE NULL
	}
	
	/**
	 * Maps a humon object from a JSON string.
	 * @param mapper - global mapper
	 * @param json - the JSON string to map from
	 * @throws JsonParseException
	 * @throws IOException
	 */
	public Humon(ObjectMapper mapper, String json) throws JsonParseException, IOException {
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
				parser.close();
				break;
			}
			// get userID
			if (JsonToken.FIELD_NAME.equals(token) && "name".equals(parser.getCurrentName())) {
				token = parser.nextToken();
				this.name = parser.getText();
			}
		}
		
		System.out.println("This is not yet supported");
		System.exit(1);
		
	}
	

	public String getName() {
		return name;
	}
	
	public String getImage() {
		return image;
	}

	public String gethID() {
		return hID;
	}

	public String getuID() {
		return uID;
	}

	public String getiID() {
		return iID;
	}

	public String getHp() {
		return hp;
	}

	public ArrayList<Move> getMoves() {
		return moves;
	}
	
	/**
	 * Process the object into a JSON representation.  Will only expose public fields, or private
	 * fields with getters.
	 * 
	 * @return JSON string
	 * @throws JsonProcessingException
	 */
	public String toJson() throws JsonProcessingException {
		return mapper.writeValueAsString(this);
	}
	
	public String toString() {
		return "To-Do: Implement Humon.toString()";
	}
}
