package main;

public class GlobalConstants {

	public static final long UPDATE_TIME = 					300000000000l;
	
	public static final String DATABASE_NAME = 				"jdbc:mysql://localhost:3306/";
	public static final String TABLE_NAME = 				"humon-test";
	public static final String DATABASE_USER_NAME = 		"ece454";
	public static final String DATABASE_USER_PASSWORD = 	"zYFqzVgW3t2Y";
	public static final String DEFAULT_CONNECTIONS = 		"5";
	
	// Users Table
	public static final String USERS_TABLE_COLUMNS = "(email, password, party, encountered_humons, friends, hcount)";
	
}
