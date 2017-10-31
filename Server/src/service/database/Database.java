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
		//createHumonsTable();
		//createInstanceTable();
		
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
				"email varchar(20) unique not null," + 
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
		//PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS humon (");
		//ps.executeUpdate();
	}
	
	private void createInstanceTable() throws SQLException {
		//PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS instance (");
		//ps.executeUpdate();
	}

}
