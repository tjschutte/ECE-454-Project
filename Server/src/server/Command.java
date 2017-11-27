package server;

public class Command {
	/**
	 * Static definitions of valid commands the server supports
	 * More commands should always be added here.
	 */
	
	public static final String LOGIN = 					"LOGIN"; // Log user in
	public static final String LOGOUT = 				"LOGOUT"; // Log user out
	public static final String REGISTER = 				"REGISTER"; // Register new user
	public static final String ERROR = 					"ERROR"; // Error
	public static final String SUCCESS = 				"SUCCESS"; // Success
	public static final String SAVE_USER = 				"SAVE-USER"; // Issue a save of passed data
	public static final String GET_IMAGE = 				"GET-IMAGE"; // Get a Humons image data
	public static final String SET_IMAGE = 				"SET-IMAGE"; // TODO: Not currently used.
	public static final String GET_INSTANCE = 			"GET-INSTANCE"; // TODO: Get the data for a particular instance
	public static final String SAVE_INSTANCE =			"SAVE-INSTANCE"; // Save a new / update existing instance
	public static final String GET_HUMON = 				"GET-HUMON"; // TODO: Get a humon (base object)
	public static final String CREATE_HUMON = 			"CREATE-HUMON"; // create a new humon (base object)
	public static final String FRIEND_REQUEST =			"FRIEND-REQUEST"; // Send a friend request
	public static final String FRIEND_REQUEST_SUCCESS = "FRIEND-REQUEST-SUCCESS"; // Friend request was successful (user look up)
	public static final String FRIEND_ADDED = 			"FRIEND-ADDED"; // User added a friend from requests
	public static final String BATTLE_REQUEST = 		"BATTLE-REQUEST"; // Send a new battle request
	
	// Commands for when in battle
	// Used to start the battle
	public static final String BATTLE_START = 			"BATTLE-START"; // TODO: Not currently used.
	// Used to get the opposing players party UID's / HID's
	public static final String GET_PARTY = 				"GET-PARTY"; // Get the party of the opposing player
	// Used to do an action (attack)
	public static final String BATTLE_ACTION = 			"BATTLE-ACTION"; // TODO: Not currently used.
	// Used to signal the end of a battle (someone lost)
	public static final String BATTLE_END = 			"BATTLE-END"; // TODO: Not currently used.

}

