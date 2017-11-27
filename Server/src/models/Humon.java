package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import utilities.SQLHelper;

public class Humon {

	private String name; // humon name
	private String description; // Description about how kewl your humon is
	private String image; // image of humon
	private int level; // The current level of the humon-instance
	private int xp; // current amount of xp
	private int hID; // hID will map a humon to is details in storage (picture, name, moves, etc)
	private String uID; // uID will map a humon instance to a user
	private String iID; // iID will map a humon to an instance. iID will be a concatination of hID, uID
						// and a 3 digit count
	private ArrayList<Move> moves; // list of moves a humon can perform
	private int health; // the health of a humon.
	private int hp; // The current hp of the humon
	private int luck; // How lucky your humon is
	private int attack; // How much bonehurtingjuice
	private int speed; // GOTTA GO FAST
	private int defense; // how much alcohol your humon can drink
	private String imagePath; // server does nothing with this.

	// Moves will be a combination of <Name, id> to m ap to template moves.

	/**
	 * Default constructor for Jackson
	 */
	public Humon() {
		// Need this default constructor for Jackson to be able to pase JSON back into
		// the object for us.
	}

	public Humon(String name, String description, String image, int level, int xp, int hID, String uID, String iID,
			ArrayList<Move> moves, int health, int hp, int luck, int attack, int speed, int defense) {
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
		this.imagePath = "";
	}
	
	public Humon(ResultSet resultSet) throws SQLException {
		//  2        3         4        5       6       7      8     9       10
		//(name, description, health, attack, defense, speed, luck, moves, created_by)
		this(resultSet.getString(2), resultSet.getString(3), "", 0, 0, resultSet.getInt(1), "", "", null, resultSet.getInt(4), 0,
				resultSet.getInt(8), resultSet.getInt(5), resultSet.getInt(7), resultSet.getInt(6));
	}
	
	public Humon HumonInstance(ResultSet resultSet) throws SQLException {
		//     1         2      3      4         5         6     7     8        9      10     11    12
		//(instanceID, name, humonID, level, experience, health, hp, attack, defense, speed, luck, user)
		return new Humon(resultSet.getString(2), "", "", resultSet.getInt(4), resultSet.getInt(5), resultSet.getInt(3), resultSet.getString(12), resultSet.getString(1),
				null, resultSet.getInt(6), resultSet.getInt(7), resultSet.getInt(11), resultSet.getInt(8), resultSet.getInt(10), resultSet.getInt(9));
	}

	public String getName() {
		return name;
	}

	public String getImagePath() {
		return imagePath; // The server never uses this value, so only ever return a dumby value.
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
	 * Process the object into a JSON representation. Will only expose public
	 * fields, or private fields with getters.
	 * 
	 * @return JSON string
	 * @throws JsonProcessingException
	 */
	public String toJson(ObjectMapper mapper) throws JsonProcessingException {
		return mapper.writeValueAsString(this);
	}

	public String toSqlHumonValueString() {
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
		obj += "'" + SQLHelper.sqlString(uID) + "')";
		return obj;
	}

	public String toSqlInstanceValueString() {
		// (instanceID, name, humonID, level, experience, health, hp, attack, defense, speed, luck, user)
		String obj = "(";
		obj += "'" + iID + "',";
		obj += "'" + SQLHelper.sqlString(name) + "',";
		obj += "'" + hID + "',";
		obj += "'" + level + "',";
		obj += "'" + xp + "',";
		obj += "'" + health + "',";
		obj += "'" + hp + "',";
		obj += "'" + attack + "',";
		obj += "'" + defense + "',";
		obj += "'" + speed + "',";
		obj += "'" + luck + "',";
		obj += "'" + SQLHelper.sqlString(uID) + "')";
		return obj;
	}

	public String toSqlInstanceUpdateSyntax() {
		// (instanceID, name, humonID, level, experience, health, hp, attack, defense, speed, luck, user)
		String obj = "";
		obj += "instanceID = '" + iID + "',";
		obj += "name = '" + SQLHelper.sqlString(name) + "',";
		obj += "humonID = " + hID + ",";
		obj += "level = " + level + ",";
		obj += "experience = " + xp + ",";
		obj += "health = " + health + ",";
		obj += "hp = " + hp + ",";
		obj += "attack = " + attack + ",";
		obj += "defense = " + defense + ",";
		obj += "speed = " + speed + ",";
		obj += "luck = " + luck + ",";
		obj += "user = '" + SQLHelper.sqlString(uID) + "'";
		return obj;
	}

}
