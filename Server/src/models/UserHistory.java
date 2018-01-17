package models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import server.Command;

public class UserHistory {
	
	private User user; // Who
	private ArrayList<String> actions;
	private String lastAction; // the last action they took
	private Date lastActionAt;
	private Date loggedInAt;

	/***
	 * Keep track of the user while they are logged in.
	 * @param user
	 */
	public UserHistory(User user) {
	 this.user = user;
	 loggedInAt = Calendar.getInstance().getTime();
	 lastActionAt = Calendar.getInstance().getTime();
	 lastAction = Command.LOGIN;
	 actions = new ArrayList<>();
	 actions.add(Command.LOGIN);
	}
	
	public void doAction(String action) {
		lastAction = action;
		lastActionAt = Calendar.getInstance().getTime();
		actions.add(action);
	}
	
	public String getLastAction() {
		return lastAction;
	}
	
	public ArrayList<String> getActionHistory(){
		return actions;
	}
	
	public Date lastActionAt() {
		return lastActionAt;
	}
	
	public Date loggedInAt() {
		return loggedInAt;
	}
	
	public User who() {
		return user;
	}

}
