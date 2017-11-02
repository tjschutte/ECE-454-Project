package service.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import main.GlobalConstants;

public class Database {
	private Connector connection;
	
	/**
	 * Object whose sole purpose is to connect to the database and make sure all needed tables are
	 * set up for the server.  Will create a connection, create each of the needed tables, and
	 * then close the connection.
	 * @param cleanTables - Delete all tables to have a clean database <Not - implemented>
	 * @param testData - Populate tables with test data <Not - implemented>
	 * @throws SQLException
	 */
	public Database(boolean cleanTables, boolean testData) throws SQLException {
		this.connection = new Connector(GlobalConstants.DATABASE_NAME, GlobalConstants.TABLE_NAME, 
				GlobalConstants.DATABASE_USER_NAME, GlobalConstants.DATABASE_USER_PASSWORD, GlobalConstants.DEFAULT_CONNECTIONS);
		
		connection.startConnection();
		
		if (cleanTables) {
			System.out.println("Dropping exsisting tables...");
			dropAllTablesIfExist();
		}
		
		System.out.println("Setting up database tables...");
		
		createUserTable();
		createImagesTable();
		createHumonsTable();
		createInstanceTable();
		
		if (testData) {
			System.out.println("Populating test data...");
			createTestData();
		}
		connection.disconnect();
	}
	
	/**
	 * Method that will drop any existing tables on the database
	 * @throws SQLException
	 */
	private void dropAllTablesIfExist() throws SQLException {
		PreparedStatement ps = connection.prepareStatement(""
				+ "DROP TABLE IF EXISTS users;");
		ps.executeUpdate();
		ps = connection.prepareStatement(""
				+ "DROP TABLE IF EXISTS image;");
		ps.executeUpdate();
		ps = connection.prepareStatement(""
				+ "DROP TABLE IF EXISTS humon;");
		ps.executeUpdate();
		ps = connection.prepareStatement(""
				+ "DROP TABLE IF EXISTS instance;");
		ps.executeUpdate();
	}
	
	private void createTestData() {
		// TODO Auto-generated method stub
	}

	private void createUserTable() throws SQLException {
		PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS users (" + 
				"userid int auto_increment," + 
				"email varchar(" + GlobalConstants.MAX_EMAIL_LENGTH + ") unique not null," + 
				"password text not null," +
				"party blob," + 
				"encountered_humons blob," + 
				"friends blob," + 
				"hcount int," +  
				"PRIMARY KEY (userid)" + 
			");");
		ps.executeUpdate();
	}
	
	private void createImagesTable() throws SQLException {
		PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS image ("
				+ "imageid int auto_increment,"
				+ "image mediumblob,"
				+ "PRIMARY KEY (imageid)"
			+ ");");
		ps.executeUpdate();
	}
	
	private void createHumonsTable() throws SQLException {
		PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS humon ("
				+ "humonID int auto_increment,"
				+ "name varchar(" + GlobalConstants.MAX_NAME_LENGTH + "),"
				+ "description varchar(" + GlobalConstants.MAX_DESCRIPTION_LENGTH + "),"
				+ "health int,"
				+ "attack int,"
				+ "defense int,"
				+ "speed int,"
				+ "luck int,"
				+ "moves blob,"
				+ "created_by varchar(" + GlobalConstants.MAX_EMAIL_LENGTH + "),"
				+ "PRIMARY KEY (humonID)"
			+ ");");
		ps.executeUpdate();
	}
	
	private void createInstanceTable() throws SQLException {
		PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS instance ("
				+ "instanceID varchar(" + GlobalConstants.MAX_INSTANCEID_LENGTH + "),"
				+ "humonID int,"
				+ "level int,"
				+ "experience int,"
				+ "health int,"
				+ "user varchar(" + GlobalConstants.MAX_NAME_LENGTH + "),"
				+ "PRIMARY KEY (instanceID)"
			+ ");");
		ps.executeUpdate();
	}

}
