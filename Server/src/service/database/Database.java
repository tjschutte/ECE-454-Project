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
			dropAllTablesIfExist();
		}
		if (testData) {
			createTestData();
		}
		createUserTable();
		createImagesTable();
		createHumonsTable();
		createInstanceTable();
		
		connection.disconnect();
	}

	private void createUserTable() throws SQLException {
		PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS users (\r\n" + 
				"    userid int auto_increment,\r\n" + 
				"    email varchar(20) unique not null,\r\n" + 
				"    password text not null,\r\n" + 
				"    party blob, # list of instanceids\r\n" + 
				"    encountered_humons blob, # list of humonids\r\n" + 
				"    friends blob, # list of friends usernames / emails\r\n" + 
				"    hcount int, #number of humons this player has captured.  used in generating instance ids\r\n" + 
				"    \r\n" + 
				"    PRIMARY KEY (userid)\r\n" + 
				");");
		ps.executeUpdate();
	}
	
	private void dropAllTablesIfExist() {
		// TODO Auto-generated method stub
	}
	
	private void createTestData() {
		// TODO Auto-generated method stub
	}
	
	private void createImagesTable() {
		//connection.executeSQL("");
	}
	
	private void createHumonsTable() {
		//connection.executeSQL("");
	}
	
	private void createInstanceTable() {
		//connection.executeSQL("");
	}

}
