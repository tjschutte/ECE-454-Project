package main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Global {

	public static final long UPDATE_TIME = 					300000000000l;
	public static final int HUMON_SERVER_PORT = 			9898;
	public static final int HUMON_HTTP_PORT = 				80;
	
	public static final String DATABASE_NAME = 				"jdbc:mysql://localhost:3306/";
	public static final String TABLE_NAME = 				"humon_test?useSSL=false&max_allowed_packet=8388608";
	public static final String DATABASE_USER_NAME = 		"ece454";
	public static final String DATABASE_USER_PASSWORD = 	"zYFqzVgW3t2Y";
	public static final String DEFAULT_CONNECTIONS = 		"5";
	
	public static final int MAX_EMAIL_LENGTH = 50;
	public static final int MAX_NAME_LENGTH = 50;
	public static final int MAX_DESCRIPTION_LENGTH = 500;
	public static final int MAX_INSTANCEID_LENGTH = 50;
	
	// Table columns
	public static final String USERS_TABLE_COLUMNS = "(email, password, party, encountered_humons, friends, friendRequests, hcount, deviceToken)";
	
	public static final String IMAGE_TABLE_COLUMNS = "(imageid, image)";	
	
	public static final String HUMON_TABLE_COLUMNS = "(name, description, health, attack, defense, speed, luck, moves, created_by)";

	public static final String INSTANCE_TABLE_COLUMNS = "(instanceID, name, humonID, level, experience, health, hp, attack, defense, speed, luck, user)";
	
	
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	/**
	 * Logs a simple message. In this case we just write the message to the server
	 * applications standard output.
	 */
	public static void log(int clientNumber, String message) {
		System.out.println(
				"Client " + clientNumber + ": " + dateFormat.format(Calendar.getInstance().getTime()) + " " + message);
	}
	
}
