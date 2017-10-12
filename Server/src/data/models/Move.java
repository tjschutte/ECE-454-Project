package data.models;

import org.w3c.dom.NameList;

public class Move {
	
	private String name;
	private String dmg;

	public Move(String name, String dmg) {
		this.name = name;
		this.dmg = dmg;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDmg() {
		return dmg;
	}

}
