package server;

public class Message {
	
	/**
	 * Error Strings. Errors will be issued as follows
	 * ERROR:<number> - description
	 */
	
	public static final String SERVER_SAVED_USER = 			"000 - Saved User progress!";
	public static final String EMAIL_ALREADY_IN_USE = 		"001 - Email is already in use.";
	public static final String BAD_CREDENTIALS = 			"002 - Incorrect email or password.";
	public static final String MALFORMED_DATA_PACKET =		"003 - Data packet received was formatted incorrectly.";
	public static final String SERVER_ERROR_RETRY = 		"004 - Server encountered an error, try request again.";
	public static final String BAD_COMMAND = 				"005 - Invalid or bad command was issued.";
	public static final String HUMON_CREATION_ERROR =		"006 - Error creating humon. Try again later.";
	public static final String DUPLICATE_HUMON = 			"007 - User attempted to create an already existing humon. Try creating a Humon with a different Name or Description";
	public static final String NOT_LOGGEDIN = 				"008 - Must be logged in to perform this action.";
	public static final String USER_DOES_NOT_EXIST =		"009 - User does not exist.";
	public static final String COULD_NOT_SAVE_ACCOUNT = 	"010 - Server was unable to save user account.";
	public static final String USERS_ALREADY_FRIENDS = 		"011 - Users are already friends.";
	public static final String REQUEST_ALREADY_PENDING = 	"012 - Friend request is already pending.";
	public static final String MUST_BE_FRIENDS_TODO = 		"013 - Users must be mutual friends in order to do that!";
	
	public static final String COMMAND_NOT_SUPPORTED = 		"666 - The command you issued was valid, but ther sever does nothing for it as this time.";
	
	/**
	 * Other user messages
	 */
	public static final String NEW_FRIEND_REQUEST_TITLE =	"New friend request!";
	public static final String NEW_FRIEND_REQUEST_BODY =	" has sent you a friend request!";
	public static final String FRIEND_REQUEST_SENT = 		"Friend request succesfully sent!";
	
	public static final String NEW_BATTLE_REQUEST_TITLE =	"New battle request!";
	public static final String NEW_BATTLE_REQUEST_BODY =	" has challenged you to a battle!";
	public static final String BATTLE_REQUEST_SENT = 		"Battle request succesfully sent!";

}
