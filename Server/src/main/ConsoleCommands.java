package main;

import org.apache.commons.cli.Options;

public class ConsoleCommands {

	// create Options object
	private static Options options;
	
	public static String SERVER_PROMPT = "Humon Server: ";
	
	public static final String ONLINE = "online"; // check what users are online
	public static final String SEARCH = "search"; // check if a user exists
	public static final String GET = "get"; // get a user stats
	public static final String ACCOUNT = "lookup"; // lookup a users information in the DB
	public static final String RESET = "reset"; // reset a users password
	
	public static final String EXIT = "exit";
	public static final String HELP = "help";

	public static Options getConsoleOptions() {
		options = new Options();
		options.addOption(ONLINE, false, "Display online users");
		options.addOption(EXIT, false, "Shutdown the server.");
		options.addOption(SEARCH, true, "Search if <user> exists.");
		options.addOption(GET, true, "Get the <user> history for this session");
		options.addOption(ACCOUNT, true, "Get the <user> information from the database");
		options.addOption(RESET, true, "Reset the password for <user> to 'password'");
		
		options.addOption(HELP, false, "Display this message.");
		
		return options;
	}
}
