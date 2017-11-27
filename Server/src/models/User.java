package models;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import utilities.SQLHelper;

public class User {
	/**
	 * TODO: Make sure constructors sanitize data.
	 */
	

	private String email;
	private String password;
	private ArrayList<String> party;
	private ArrayList<String> encounteredHumons;
	private ArrayList<String> friends;
	private ArrayList<String> friendRequests;
	private int hcount;
	private boolean isDirty;
	private String deviceToken;

	/**
	 * Default Constructor for Jackson.
	 */
	public User() {
		// This needs to be here for Jackson to be able to take from JSON string back to object for us.
	}
	
	/**
	 * Create a new object. Potentially registering a new user.
	 * @param mapper
	 * @param email - email
	 * @param password - password
	 * @param hcount - number of humons encountered
	 */
	public User(String email, String password, int hcount, String deviceToken, boolean isDirty) {
		this.email = email;
		this.password = password;
		this.party = new ArrayList<String>(); 
		this.encounteredHumons = new ArrayList<String>();
		this.friends = new ArrayList<String>();
		this.friendRequests = new ArrayList<String>();
		this.hcount = hcount;
		this.deviceToken = deviceToken;
		this.isDirty = isDirty;
	}
	
	/**
	 * From database to object.
	 * @param mapper
	 * @param email
	 * @param password
	 * @param party
	 * @param encounteredHumons
	 * @param friends
	 * @param hcount
	 * @param isDirty
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public User(String email, String password, String party, String encounteredHumons, String friends, String friendRequests, int hcount, String deviceToken, boolean isDirty) throws JsonParseException, JsonMappingException, IOException {
		this.email = email;
		this.password = password;
		this.party = (party == null) ? new ArrayList<String>() : arrayCleaner(new ArrayList<String>(Arrays.asList(party.substring(party.indexOf('[') + 1, party.indexOf(']')).split(","))));
		this.encounteredHumons = (encounteredHumons == null) ? new ArrayList<String>() : arrayCleaner(new ArrayList<String>(Arrays.asList(encounteredHumons.substring(encounteredHumons.indexOf('[') + 1, encounteredHumons.indexOf(']')).split(","))));
		this.friends =  (friends == null) ? new ArrayList<String>() : arrayCleaner(new ArrayList<String>(Arrays.asList(friends.substring(friends.indexOf('[') + 1, friends.indexOf(']')).split(","))));
		this.friendRequests =  (friendRequests == null) ? new ArrayList<String>() : arrayCleaner(new ArrayList<String>(Arrays.asList(friendRequests.substring(friendRequests.indexOf('[') + 1, friendRequests.indexOf(']')).split(","))));
		this.hcount = hcount;
		this.deviceToken = deviceToken;
		this.isDirty = isDirty;
	}
	
	public User(ResultSet resultSet) throws SQLException, JsonParseException, JsonMappingException, IOException {
		//String email, String password, String party, String encounteredHumons, String friends, String friendRequests, int hcount, String deviceToken, boolean isDirty
		this(resultSet.getString(2), resultSet.getString(3), resultSet.getString(4),
				resultSet.getString(5), resultSet.getString(6), resultSet.getString(7), resultSet.getInt(8), resultSet.getString(9), false);
	}
	
	// Remove any empty entries and all whitespace characters
	private ArrayList<String> arrayCleaner(ArrayList<String> array) {
		ArrayList<String> cleaned = new ArrayList<String>();
		for (String item : array) {
			if (item.length() != 0) {
				// Remove all whitespace characters...
				String itemNoSpace = item.replaceAll("\\s+","");
				String itemNoBrace = itemNoSpace.replaceAll("\\[", "");
				String itemToAdd = itemNoBrace.replaceAll("\\\"", "");
				cleaned.add(itemToAdd);
			}
		}
		return cleaned;
	}

	public boolean getIsDirty() {
		return isDirty;
	}
	
	public String getDeviceToken(){
		return deviceToken;
	}
	
	public void setClean() {
		isDirty = false;
	}

	public String getEmail() {
		return email;
	}
	
	public ArrayList<String> getFriends() {
		return friends;
	}
	
	public ArrayList<String> getfriendRequests() {
		return friendRequests;
	}
	
	public void addFriend(String email) {
		if (friends == null) {
			friends = new ArrayList<String>();
		}	
		if (!friends.contains(email)) {
			friends.add(email);
			isDirty = true;
		}	
	}
	
	public void addFriendRequest(String email) {
		if (friendRequests == null) {
			friendRequests = new ArrayList<String>();
		}	
		if (!friendRequests.contains(email)) {
			friendRequests.add(email);
			isDirty = true;
		}
	}
	
	public boolean removeFriend(String email) {
		if (friends == null || friends.isEmpty()) {
			return true;
		} else {
			for (String friend : friends) {
				if (friend.equals(email)) {
					friends.remove(friend);
					isDirty = true;
					return true;
				}
			}
		}
		return false;
	}

	public String getPassword() {
		return password;
	}

	public ArrayList<String> getParty() {
		return party;
	}
	
	public void addPartyMember(String instanceID) {
		if (party == null) {
			party = new ArrayList<String>();
		} 	
		if (!party.contains(instanceID)) {
			party.add(instanceID);
			isDirty = true;
		}
	}
	
	public boolean removePartyMember(int instanceID) {
		if (party == null || party.isEmpty()) {
			return true;
		} else {
			for (String id : party) {
				if (id.equals(instanceID + "")) {
					party.remove(instanceID);
					isDirty = true;
					return true;
				}
			}
		}
		return false;
	}

	public ArrayList<String> getEncounteredHumons() {
		return encounteredHumons;
	}
	
	public void addEncounteredHumon(String humonID) {
		if (encounteredHumons == null) {
			encounteredHumons = new ArrayList<String>();
		} 
		if (encounteredHumons.contains(humonID)) {
			return;
		} else {
			encounteredHumons.add(humonID);
			isDirty = true;
		}
		
	}

	public int getHcount() {
		return hcount;
	}
	
	public void incrementHCount() {
		hcount++;
		isDirty = true;
	}

	public String toJson(ObjectMapper mapper) throws JsonProcessingException {
		return mapper.writeValueAsString(this);
	}

	public String toSqlValueString() {
		String obj = "(";
		obj += "'" + SQLHelper.sqlString(email) + "',";
		obj += "'" + SQLHelper.sqlString(password) + "',";
		obj += "'" + party + "',";
		obj += "'" + encounteredHumons + "',";
		obj += "'" + friends + "',";
		obj += "'" + friendRequests + "',";
		obj += "'" + hcount + "',";
		obj += "'" + deviceToken + "')";
		
		return obj;
	}
	
	/**
	 * Method for getting SQL update syntax for updating the user in the user table.
	 * @return
	 */
	public String updateSyntax() {
		String update = "";
		update += "email = '" + SQLHelper.sqlString(email) + "',";
		update += "password = '" + SQLHelper.sqlString(password) + "',";
		update += "party = '" + party + "',";
		update += "encountered_humons = '" + encounteredHumons + "',";
		update += "friends = '" + friends + "',";
		update += "friendRequests = '" + friendRequests + "',";
		update += "hcount = '" + hcount + "',";
		update += "deviceToken = '" + deviceToken + "' ";
		return update;
	}

	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}

}
