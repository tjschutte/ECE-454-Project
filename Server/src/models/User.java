package models;

import java.util.ArrayList;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonProcessingException;
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
		this.party = null; 
		this.encounteredHumons = null;
		this.friends = null;
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
	 */
	public User(String email, String password, String party, String encounteredHumons, String friends, int hcount, String deviceToken, boolean isDirty) {
		this.email = email;
		this.password = password;
		this.party = (party != null) ? new ArrayList<String>(Arrays.asList(party.split(","))) : new ArrayList<String>();
		this.encounteredHumons = (encounteredHumons != null) ? new ArrayList<String>(Arrays.asList(encounteredHumons.split(","))) : new ArrayList<String>();
		this.friends = (friends != null) ? new ArrayList<String>(Arrays.asList(friends.split(","))) : new ArrayList<String>();
		this.hcount = hcount;
		this.deviceToken = deviceToken;
		this.isDirty = isDirty;
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
	
	public void addFriend(String email) {
		if (friends == null) {
			friends = new ArrayList<String>();
		}	
		if (!friends.contains(email)) {
			friends.add(email);
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
				if (id.equals(instanceID)) {
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
		update += "hcount = '" + hcount + "',";
		update += "deviceToken = '" + deviceToken + "' ";
		return update;
	}

	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}

}
