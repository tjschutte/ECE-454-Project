package utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.codec.binary.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;

import main.Global;
import models.Humon;
import models.User;

public class Database {
	private Connector connection;
	
	private String testHumon1 = "{\"attack\":4,\"defense\":5,\"description\":\"it's a test description\",\"hID\":0,\"health\":3,\"iID\":\"\",\"image\":\"\",\"imagePath\":\"\",\"level\":1,\"luck\":7,\"moves\":[{\"description\":\"Wattup\",\"dmg\":10,\"effect\":null,\"hasEffect\":false,\"id\":0,\"name\":\"Test Move A\",\"selfCast\":false},{\"description\":\"Wattup\",\"dmg\":10,\"effect\":null,\"hasEffect\":false,\"id\":1,\"name\":\"Test Move B\",\"selfCast\":false},{\"description\":\"Wattup\",\"dmg\":10,\"effect\":null,\"hasEffect\":false,\"id\":8,\"name\":\"Test Move I\",\"selfCast\":false},{\"description\":\"Wattup\",\"dmg\":10,\"effect\":null,\"hasEffect\":false,\"id\":9,\"name\":\"Test Move J\",\"selfCast\":false}],\"name\":\"Test Humon 1\",\"speed\":6,\"uID\":\"a\",\"xp\":0}\r\n";
	private String testHumon2 = "{\"attack\":4,\"defense\":5,\"description\":\"it's a test description\",\"hID\":0,\"health\":3,\"iID\":\"\",\"image\":\"\",\"imagePath\":\"\",\"level\":1,\"luck\":7,\"moves\":[{\"description\":\"Wattup\",\"dmg\":10,\"effect\":null,\"hasEffect\":false,\"id\":0,\"name\":\"Test Move A\",\"selfCast\":false},{\"description\":\"Wattup\",\"dmg\":10,\"effect\":null,\"hasEffect\":false,\"id\":1,\"name\":\"Test Move B\",\"selfCast\":false},{\"description\":\"Wattup\",\"dmg\":10,\"effect\":null,\"hasEffect\":false,\"id\":8,\"name\":\"Test Move I\",\"selfCast\":false},{\"description\":\"Wattup\",\"dmg\":10,\"effect\":null,\"hasEffect\":false,\"id\":9,\"name\":\"Test Move J\",\"selfCast\":false}],\"name\":\"Test Humon 2\",\"speed\":6,\"uID\":\"a\",\"xp\":0}\r\n";

	/**
	 * Object whose sole purpose is to connect to the database and make sure all needed tables are
	 * set up for the server.  Will create a connection, create each of the needed tables, and
	 * then close the connection.
	 * @param cleanTables - Delete all tables to have a clean database <Not - implemented>
	 * @param testData - Populate tables with test data <Not - implemented>
	 * @throws SQLException
	 */
	public Database(boolean cleanTables, boolean testData) throws SQLException {
		this.connection = new Connector(Global.DATABASE_NAME, Global.TABLE_NAME, 
				Global.DATABASE_USER_NAME, Global.DATABASE_USER_PASSWORD, Global.DEFAULT_CONNECTIONS);
		
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
		try {
			String image = "";
		
			File file =  new File("GooglePlay.png");
		
			FileInputStream fileInputStreamReader = new FileInputStream(file);
			byte[] bytes = new byte[(int)file.length()];
			fileInputStreamReader.read(bytes);
			image = new String(Base64.encodeBase64(bytes), "UTF-8");
			
			PreparedStatement ps = connection.prepareStatement("insert into image (imageid, image) values ('1', '" + image + "');");
			ps.executeUpdate();
			ps = connection.prepareStatement("insert into image (imageid, image) values ('2', '" + image + "');");
			ps.executeUpdate();
			
			User user = new User("user1@testemail.com", "Password", 0, "", false);
			ps = connection.prepareStatement("insert into users " + Global.USERS_TABLE_COLUMNS + " values " + user.toSqlValueString() + ";");
			ps.executeUpdate();
			
			user = new User("user2@testemail.com", "Password", 0, "", false);
			ps = connection.prepareStatement("insert into users " + Global.USERS_TABLE_COLUMNS + " values " + user.toSqlValueString() + ";");
			ps.executeUpdate();
			
			Humon humon = new ObjectMapper().readValue(testHumon1, Humon.class);
			ps = connection.prepareStatement("insert into humon " + Global.HUMON_TABLE_COLUMNS + " values " + humon.toSqlHumonValueString(user));
			ps.executeUpdate();
			
			humon = new ObjectMapper().readValue(testHumon2, Humon.class);
			ps = connection.prepareStatement("insert into humon " + Global.HUMON_TABLE_COLUMNS + " values " + humon.toSqlHumonValueString(user));
			ps.executeUpdate();
			
			
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
		
		
		
		
	}

	private void createUserTable() throws SQLException {
		PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS users (" + 
				"userid int auto_increment," + 
				"email varchar(" + Global.MAX_EMAIL_LENGTH + ") unique not null," + 
				"password text not null," +
				"party blob," + 
				"encountered_humons blob," + 
				"friends blob," + 
				"hcount int," +
				"deviceToken text," + 
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
				+ "name varchar(" + Global.MAX_NAME_LENGTH + "),"
				+ "description varchar(" + Global.MAX_DESCRIPTION_LENGTH + "),"
				+ "health int,"
				+ "attack int,"
				+ "defense int,"
				+ "speed int,"
				+ "luck int,"
				+ "moves blob,"
				+ "created_by varchar(" + Global.MAX_EMAIL_LENGTH + "),"
				+ "PRIMARY KEY (humonID)"
			+ ");");
		ps.executeUpdate();
	}
	
	private void createInstanceTable() throws SQLException {
		PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS instance ("
				+ "instanceID varchar(" + Global.MAX_INSTANCEID_LENGTH + "),"
				+ "humonID int,"
				+ "level int,"
				+ "experience int,"
				+ "health int,"
				+ "user varchar(" + Global.MAX_NAME_LENGTH + "),"
				+ "PRIMARY KEY (instanceID)"
			+ ");");
		ps.executeUpdate();
	}

}
