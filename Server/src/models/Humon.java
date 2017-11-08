package models;

import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import utilities.SQLHelper;

public class Humon {
	
	private String name; 			// humon name
    private String description;     // Description about how kewl your humon is
    private String image;			// image of humon
    private int level;              // The current level of the humon-instance
    private int xp;                 // current amount of xp
    private int hID;     			// hID will map a humon to is details in storage (picture, name, moves, etc)
    private String uID; 			// uID will map a humon instance to a user
    private String iID;				// iID will map a humon to an instance. iID will be a concatination of hID, uID and a 3 digit count
    private ArrayList<Move> moves;  // list of moves a humon can perform
    private int health; 		    // the health of a humon.
    private int hp;					// The current hp of the humon
    private int luck;               // How lucky your humon is
    private int attack;             // How much bonehurtingjuice
    private int speed;              // GOTTA GO FAST
    private int defense;            // how much alcohol your humon can drink
    private String imagePath;		// server does nothing with this.

    // Moves will be a combination of <Name, id> to m ap to template moves.

    /**
     * Default constructor for Jackson
     */
    public Humon() {
    	// Need this default constructor for Jackson to be able to pase JSON back into the object for us.
    }
    
    public Humon(String name, String description, String image, int level, int xp, int hID, String uID,
                 String iID, ArrayList<Move> moves, int health, int hp, int luck, int attack, int speed, int defense) {
        this.name = name;
        this.description = description;
        this.image = image;
        this.level = level;
        this.xp = xp;
        this.hID = hID;
        this.uID = uID;
        this.iID = iID;
        this.moves = moves;
        this.health = health;
        this.hp = hp;
        this.luck = luck;
        this.attack = attack;
        this.speed = speed;
        this.defense = defense;
    }

    public String getName() {
        return name;
    }
    
    public String getImagePath() {
    	return ""; // The server never uses this value, so only even return a dumby value.
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public int gethID() {
        return hID;
    }
    
    public void sethID(int hID) {
    	this.hID = hID;
    }

    public String getuID() {
        return uID;
    }

    public String getiID() {
        return iID;
    }

    public int getHealth() {
        return health;
    }
    
    public int getHp() {
    	return hp;
    }

    public ArrayList<Move> getMoves() {
        return moves;
    }

    public int getLevel() {
        return level;
    }

    public int getXp() {
        return xp;
    }

    public int getLuck() {
        return luck;
    }

    public int getAttack() {
        return attack;
    }

    public int getSpeed() {
        return speed;
    }

    public int getDefense() {
        return defense;
    }
	
	/**
	 * Process the object into a JSON representation.  Will only expose public fields, or private
	 * fields with getters.
	 * 
	 * @return JSON string
	 * @throws JsonProcessingException
	 */
	public String toJson(ObjectMapper mapper) throws JsonProcessingException {
		return mapper.writeValueAsString(this);
	}
	
	public String toSqlHumonValueString(User user) {
		// (name, description, health, attack, defense, speed, luck, moves)
		String obj = "(";
		obj += "'" + SQLHelper.sqlString(name) + "',";
		obj += "'" + SQLHelper.sqlString(description) + "',";
		obj += "'" + health + "',";
		obj += "'" + attack + "',";
		obj += "'" + defense + "',";
		obj += "'" + speed + "',";
		obj += "'" + luck + "',";
		obj += "'" + moves + "',";
		obj += "'" + SQLHelper.sqlString(user.getEmail()) + "')";
		return obj;
	}
	
	public String toSqlInstanceValueString(User user) {
		// (instanceID, humonID, level, expereince, health, hp, attack, defence, speed, luck, user)
		String obj = "(";
		obj += "'" + iID + "',";
		obj += "'" + hID + "',";
		obj += "'" + level + "',";
		obj += "'" + xp + "',";
		obj += "'" + health + "',";
		obj += "'" + attack + "',";
		obj += "'" + defense + "',";
		obj += "'" + speed + "',";
		obj += "'" + luck + "',";
		obj += "'" + SQLHelper.sqlString(user.getEmail()) + "')";
		return obj;
	}

}
