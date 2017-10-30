package edu.wisc.ece454.hu_mon.Models;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Humon extends Jsonable {

    private String name; 			// humon name
    private String description;     // Description about how kewl your humon is
    private Bitmap image;			// image of humon
    private int level;              // The current level of the humon-instance
    private int xp;                 // current amount of xp
    private int hID;     			// hID will map a humon to is details in storage (picture, name, moves, etc)
    private String uID; 			// uID will map a humon instance to a user
    private String iID;				// iID will map a humon to an instance. iID will be a concatination of hID, uID and a 3 digit count
    private ArrayList<Move> moves;  // list of moves a humon can perform
    private int health; 		    // the health of a humon. All humons start with 100 hp.
    private int luck;               // How lucky your humon is
    private int attack;             // How much bonehurtingjuice
    private int speed;              // GOTTA GO FAST
    private int defense;            // how much alcohol your humon can drink

    // Moves will be a combination of <Name, id> to m ap to template moves.

    public Humon(String name, String description, Bitmap image, int level, int xp, int hID, String uID,
                 String iID, ArrayList<Move> moves, int health, int luck, int attack, int speed, int defense) {
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
        this.luck = luck;
        this.attack = attack;
        this.speed = speed;
        this.defense = defense;
    }

    public Humon(ObjectMapper mapper, String json) {
        System.out.println("This is not yet supported");
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        return Base64.encodeToString(bytes.toByteArray(), Base64.DEFAULT);
    }

    @JsonIgnore
    public Bitmap getBitmap() {
        return image;
    }

    public int gethID() {
        return hID;
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

    public String toString() {
        return "To-Do: Implement Humon.toString()";
    }
}
