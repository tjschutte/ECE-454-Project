package server;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import main.Global;
import models.Humon;
import utilities.SQLHelper;

public class HumonAction {
	
	/**
	 * Save a new Humon and New instance at the same time. Reply to the client with
	 * the HumonId (hID)
	 * 
	 * @param data - contain information about a new Humon, needs to be properly formated
	 * JSON object of a humon object
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws SQLException
	 */
	static void createNewHumon(ServerConnection connection, String data)
			throws JsonParseException, JsonMappingException, IOException, SQLException {

		if (connection.user == null || connection.user.getEmail().isEmpty()) {
			connection.error(Message.NOT_LOGGEDIN);
			return;
		}
		Humon humon = connection.mapper.readValue(data, Humon.class);
		
		// print it
		Global.log(connection.clientNumber,
				connection.user.getEmail() + " is creating a new Humon: " + humon.getName() + ", " + humon.getDescription());

		// Check to make sure it is a unique name / email / description.
		ResultSet resultSet = connection.databaseConnection.executeSQL("select * from humon where created_by='"
				+ SQLHelper.sqlString(connection.user.getEmail()) + "'" + " and name='" + SQLHelper.sqlString(humon.getName())
				+ "' and description='" + SQLHelper.sqlString(humon.getDescription()) + "';");
		if (resultSet.next()) {
			connection.error(Message.DUPLICATE_HUMON);
			Global.log(connection.clientNumber, "User attempted to create a duplicate humon");
			return;
		}

		int hID;

		// Insert into humon Table
		PreparedStatement ps = connection.databaseConnection.prepareStatement(
				"insert into humon " + Global.HUMON_TABLE_COLUMNS + " values " + humon.toSqlHumonValueString());
		// Should only get 1 row was affected.
		int rows = ps.executeUpdate();
		if (rows != 1) {
			throw new SQLException();
		}

		// Get the HID to return to the user
		resultSet = connection.databaseConnection
				.executeSQL("select humonID from humon where name='" + SQLHelper.sqlString(humon.getName())
						+ "' and description='" + SQLHelper.sqlString(humon.getDescription()) + "';");
		if (!resultSet.next()) {
			connection.sendResponse(Command.ERROR, Message.HUMON_CREATION_ERROR);
			return;
		}

		// Get the hID of the created humon, and send it as the response, as well as
		// updated hcount. User is also now dirty.
		hID = resultSet.getInt(1);

		connection.sendResponse(Command.CREATE_HUMON, "{\"hID\":\"" + hID + "\",\"name\":\"" + humon.getName() + "\","
				+ "\"description\":\"" + humon.getDescription() + "\"}");

		// Insert image into image Table
		ps = connection.databaseConnection.prepareStatement("insert into image " + Global.IMAGE_TABLE_COLUMNS + " values " + "('"
				+ hID + "','" + humon.getImage() + "')");

		// Should only get 1 row was affected.
		rows = ps.executeUpdate();
		if (rows != 1) {
			throw new SQLException();
		}
		humon.setImage();
		Global.log(connection.clientNumber, humon.toJson(connection.mapper));
	}
	
	static void saveInstance(ServerConnection connection, String data) throws JsonParseException, JsonMappingException, IOException, SQLException {
		Humon humon = connection.mapper.readValue(data, Humon.class);
		// print it
		Global.log(connection.clientNumber, humon.getuID() + " is saving Humon Instance: " + humon.getName() + ", " + humon.getDescription());
		// Check to make sure it is a unique name / email / description.
		ResultSet resultSet = connection.databaseConnection.executeSQL("select * from instance where instanceID='"
				+ humon.getiID() + "';");		
		// If the entry was already in the table, then we are probably updating the table.
		if (resultSet.next()) {
			Global.log(connection.clientNumber, "Updating exsiting instance: " + data);
			
			PreparedStatement ps = connection.databaseConnection.prepareStatement("update instance set " + humon.toSqlInstanceUpdateSyntax() + "where instanceID='"
					+ humon.getiID() + "';");
			
			int rows = ps.executeUpdate();
			// Should only get 1 row was affected.
			if (rows != 1) {
				throw new SQLException();
			}			
			connection.sendResponse(Command.SAVE_INSTANCE, Command.SUCCESS);
			return;
		}	
		Global.log(connection.clientNumber, "Creating new isntance: " + data);
		// Update into instance Table
		PreparedStatement ps = connection.databaseConnection.prepareStatement(
				"insert into instance " + Global.INSTANCE_TABLE_COLUMNS + " values " + humon.toSqlInstanceValueString());	
		// Should only get 1 row was affected.
		int rows = ps.executeUpdate();
		if (rows != 1) {
			throw new SQLException();
		}
		connection.sendResponse(Command.SAVE_INSTANCE, Command.SUCCESS);
	}

	static void getInstance(ServerConnection connection, String data) throws JsonParseException, IOException, SQLException {
		Global.log(connection.clientNumber, Command.GET_INSTANCE + ": " + data);
		String iID = "";

		JsonFactory factory = new JsonFactory();
		JsonParser parser = factory.createParser(data);

		while (!parser.isClosed()) {
			JsonToken token = parser.nextToken();
			if (token == null) {
				break;
			}
			
			// If there was a populated hID
			if (JsonToken.FIELD_NAME.equals(token) && "iID".equals(parser.getCurrentName())) {
				token = parser.nextToken();
				iID = parser.getValueAsString();
			} 

		}
		
		ResultSet resultSet = connection.databaseConnection
				.executeSQL("select * from instance where instanceID='" + iID + "';");
		
		if (!resultSet.next()) {
			connection.sendResponse(Command.ERROR, Message.INSTANCE_DOES_NOT_EXIST);
			Global.log(connection.clientNumber, Command.ERROR + ": " + Message.INSTANCE_DOES_NOT_EXIST);
			return;
		}
		
		Humon requestedInstance = new Humon();
		requestedInstance = requestedInstance.HumonInstance(resultSet);
		
		resultSet = connection.databaseConnection
				.executeSQL("select * from humon where humonID='" + requestedInstance.gethID() + "';");
		
		if (!resultSet.next()) {
			connection.sendResponse(Command.ERROR, Message.INSTANCE_DOES_NOT_EXIST);
			Global.log(connection.clientNumber, Command.ERROR + ": " + Message.INSTANCE_DOES_NOT_EXIST);
			return;
		}
		
		Humon requestedHumon = new Humon(resultSet);
		
		// Combine moves onto the object
		requestedInstance.setMoves(requestedHumon.getMoves());
		
		System.out.println(requestedInstance.toJson(new ObjectMapper()));

		connection.sendResponse(Command.GET_INSTANCE, requestedInstance.toJson(new ObjectMapper()));
	}

	static void getHumon(ServerConnection connection, String data) throws JsonParseException, IOException, SQLException {
		Global.log(connection.clientNumber, Command.GET_HUMON + ": " + data);
		int hID = 0;

		JsonFactory factory = new JsonFactory();
		JsonParser parser = factory.createParser(data);

		while (!parser.isClosed()) {
			JsonToken token = parser.nextToken();
			if (token == null) {
				break;
			}
			
			// If there was a populated hID
			if (JsonToken.FIELD_NAME.equals(token) && "hID".equals(parser.getCurrentName())) {
				token = parser.nextToken();
				hID = parser.getValueAsInt(-1);
			} 
			// Else if it was a iID
			else if (JsonToken.FIELD_NAME.equals(token) && "iID".equals(parser.getCurrentName())) {
				// Do a lookup to get hID
				token = parser.nextToken();
				String iID = parser.getValueAsString();
				ResultSet resultSet = connection.databaseConnection
						.executeSQL("select humonID from instance where instanceID='" + iID + "';");
				
				if (!resultSet.next()) {
					connection.sendResponse(Command.ERROR, Message.INSTANCE_DOES_NOT_EXIST);
					Global.log(connection.clientNumber, Command.ERROR + ": " + Message.INSTANCE_DOES_NOT_EXIST);
					return;
				}
				
				hID = resultSet.getInt(1);
			}
		}

		if (hID <= 0) {
			connection.error(Message.MALFORMED_DATA_PACKET);
			Global.log(connection.clientNumber, Command.ERROR + ": " + Message.MALFORMED_DATA_PACKET);
			return;
		}
		
		ResultSet resultSet = connection.databaseConnection
				.executeSQL("select * from humon where humonID='" + hID + "';");

		if (!resultSet.next()) {
			connection.sendResponse(Command.ERROR, Message.HUMON_DOES_NOT_EXIST);
			Global.log(connection.clientNumber, Command.ERROR + ": " + Message.HUMON_DOES_NOT_EXIST);
			return;
		}
		
		Humon requested = new Humon(resultSet);
		
		resultSet = connection.databaseConnection
				.executeSQL("select * from image where imageid='" + hID + "';");
		
		if (!resultSet.next()) {
			connection.sendResponse(Command.ERROR, Message.HUMON_DOES_NOT_EXIST);
			Global.log(connection.clientNumber, Command.ERROR + ": " + Message.SERVER_ERROR_RETRY);
			return;
		}
		
		// Sed the image back on the Humon object.
		requested.setImage(resultSet.getString(2));
		
		connection.sendResponse(Command.GET_HUMON, requested.toJson(new ObjectMapper()));
		
	}
	
	// May choose to deprecate method / command. Just pass data when user call getHumon
	static void getImage(ServerConnection connection, String data) {
		// TODO Auto-generated method stub
		// Need to know what the client will send...
		Global.log(connection.clientNumber, Command.GET_IMAGE + ": " + data);
		connection.sendResponse(Command.ERROR, Message.COMMAND_NOT_SUPPORTED);
	}
	
	static void battleStart(ServerConnection connection, String data) {
		// TODO Auto-generated method stub
		// Need to know what the client will send...
		Global.log(connection.clientNumber, Command.GET_IMAGE + ": " + data);
		connection.sendResponse(Command.ERROR, Message.COMMAND_NOT_SUPPORTED);
	}
	
	static void battleAction(ServerConnection connection, String data) {
		// TODO Auto-generated method stub
		// Need to know what the client will send...
		Global.log(connection.clientNumber, Command.GET_IMAGE + ": " + data);
		connection.sendResponse(Command.ERROR, Message.COMMAND_NOT_SUPPORTED);
	}
	
	static void battleEnd(ServerConnection connection, String data) {
		// TODO Auto-generated method stub
		// Need to know what the client will send...
		Global.log(connection.clientNumber, Command.GET_IMAGE + ": " + data);
		connection.sendResponse(Command.ERROR, Message.COMMAND_NOT_SUPPORTED);
	}

}
