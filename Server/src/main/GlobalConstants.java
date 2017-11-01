package main;

import java.sql.PreparedStatement;

public class GlobalConstants {

	public static final long UPDATE_TIME = 					300000000000l;
	
	public static final String DATABASE_NAME = 				"jdbc:mysql://localhost:3306/";
	public static final String TABLE_NAME = 				"humon_test";
	public static final String DATABASE_USER_NAME = 		"ece454";
	public static final String DATABASE_USER_PASSWORD = 	"zYFqzVgW3t2Y";
	public static final String DEFAULT_CONNECTIONS = 		"5";
	
	public static final int MAX_EMAIL_LENGTH = 50;
	public static final int MAX_NAME_LENGTH = 50;
	public static final int MAX_DESCRIPTION_LENGTH = 500;
	public static final int MAX_INSTANCEID_LENGTH = 50;
	
	// Table columns
	public static final String USERS_TABLE_COLUMNS = "(email, password, party, encountered_humons, friends, hcount)";
	
	public static final String IMAGE_TABLE_COLUMNS = "(imageid, image)";	
	
	public static final String HUMON_TABLE_COLUMNS = "(name, description, health, attack, defense, speed, luck, moves)";
	
	public static final String INSTANCE_TABLE_COLUMNS = "(instanceID, humonID, level, expereince, health, user)";
	
}
