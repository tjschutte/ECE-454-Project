package server;

public class Command {
	/**
	 * Static definitions of valid commands the server supports
	 * More commands should always be added here.
	 */
	
	public static final String LOGIN = 					"LOGIN";
	public static final String LOGOUT = 				"LOGOUT";
	public static final String REGISTER = 				"REGISTER";
	public static final String ERROR = 					"ERROR";
	public static final String SUCCESS = 				"SUCCESS";
	public static final String SAVE_USER = 				"SAVE-USER";
	public static final String GET_IMAGE = 				"GET-IMAGE";
	public static final String SET_IMAGE = 				"SET-IMAGE"; // Not currently used.
	public static final String GET_INSTANCE = 			"GET-INSTANCE";
	public static final String SAVE_INSTANCE =			"SAVE-INSTANCE";
	public static final String GET_HUMON = 				"GET-HUMON";
	public static final String CREATE_HUMON = 			"CREATE-HUMON";
	public static final String FRIEND_REQUEST =			"FRIEND-REQUEST";
	public static final String FRIEND_REQUEST_SUCCESS = "FRIEND-REQUEST-SUCCESS";
	public static final String FRIEND_ADDED = 			"FRIEND-ADDED";
	public static final String BATTLE_REQUEST = 		"BATTLE-REQUEST";
	
	// Commands for when in battle
	// Used to start the battle
	public static final String BATTLE_START = 			"BATTLE-START"; // Not currently used.
	// Used to get the opposing players party UID's / HID's
	public static final String GET_PARTY = 				"GET-PARTY"; // Not currently user.
	// Used to do an action (attack)
	public static final String BATTLE_ACTION = 			"BATTLE-ACTION"; // Not currently used.
	// Used to signal the end of a battle (someone lost)
	public static final String BATTLE_END = 			"BATTLE-END"; // Not currently used.

}

