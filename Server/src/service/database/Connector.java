package service.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class Connector {

	private static final String DATABASE_DRIVER = "com.mysql.cj.jdbc.Driver";
	
    private Connection connection;
    private Properties properties;
    
    private String url;
    private String database;
    private String username;
    private String password;
    private String pool;
    
    /**
     *  Create an instance of the Connection Object. Does not connect to the database!
     * @param url
     * @param table
     * @param username
     * @param password
     */
    public Connector(String url, String database, String username, String password, String pool) {
    	this.url = url;
    	this.database = database;
    	this.username = username;
    	this.password = password;
    	this.pool = pool;
    }
    
    /**
     * Setup properties object for connection to database
     * @return
     */
    private Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            properties.setProperty("user", this.username);
            properties.setProperty("password", this.password);
            properties.setProperty("MaxPooledStatements", this.pool);
        }
        return properties;
    }
	
	/**
	 * Connects to the Database.  Call before executing queries etc
	 */
	public void startConnection() {
	    if (connection == null) {
	    	System.out.println("Attempting to connect to database....");
	        try {
	            Class.forName(DATABASE_DRIVER);
	            connection = DriverManager.getConnection(this.url + this.database, getProperties());
	            System.out.println("Connected to databse!");
	        } catch (ClassNotFoundException | SQLException e) {
	            e.printStackTrace();
	        }
	    }
	}
	
	/**
	 * Disconnect from the Database. Call before Exiting.
	 */
	public void disconnect() {
	    if (connection != null) {
	        try {
	            connection.close();
	            connection = null;
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	}
	
	/**
	 * Executes a query on the connectors database.
	 * @param sql - query to execute
	 * @return ResultSet object that contains result rows.
	 */
	public ResultSet executeSQL(String sql) {
		if (connection == null) {
			startConnection();
		}
		try {
			PreparedStatement statement = connection.prepareStatement(sql);
			ResultSet rs = statement.executeQuery();
			return rs;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return connection.prepareStatement(sql);
	}
}
