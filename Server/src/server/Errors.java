package server;

public class Errors {
	
	/**
	 * Error Strings. Errors will be issued as follows
	 * ERROR:<number> - description
	 */
	
	public static final String EMAIL_ALREADY_IN_USE = 	"001 - Email is already in use.";
	public static final String BAD_CREDENTIALS = 		"002 - Incorrect email or password.";
	public static final String MALFORMED_DATA_PACKET =	"003 - Data packet received was formatted incorrectly";
	public static final String SERVER_ERROR_RETRY = 	"004 - Server encountered an error, try request again";

}