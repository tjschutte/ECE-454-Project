package edu.wisc.ece454.hu_mon.Models;

import android.graphics.Bitmap;

import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Humon {

    ObjectMapper mapper;
    private String name; 			// humon name
    private String description;     // Description about how kewl your humon is
    private Bitmap image;			// image of humon
    private int hID;     			// hID will map a humon to is details in storage (picture, name, moves, etc)
    private String uID; 			// uID will map a humon instance to a user
    private String iID;				// iID will map a humon to an instance. iID will be a concatination of hID, uID and a 3 digit count
    private int hp; 				// the health of a humon. All humons start with 100 hp.
    private ArrayList<String> moves; // list of moves a humon can perform

    public Humon(ObjectMapper mapper, String name, String description, Bitmap image, int hID, String uID, int hp, String iID, ArrayList<String> moves) {
        this.mapper = mapper;
        this.name = name;	// CANNOT BE NULL
        this.description = description;
        this.image = image; // CANNOT BE NULL
        this.hID = hID; 	// CANNONT BE NULL
        this.uID = uID; 	// CAN BE NULL
        this.hp = hp; 		// CAN BE NULL
        this.iID = iID;		// CAN BE NULL
        this.moves = moves; // CANNOT BE NULL
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

    public Bitmap getImage() {
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

    public int getHp() {
        return hp;
    }

    public ArrayList<String> getMoves() {
        return moves;
    }

    /**
     * Process the object into a JSON representation.  Will only expose public fields, or private
     * fields with getters.
     *
     * @return JSON string
     * @throws JsonProcessingException
     */
    public String toJson() throws JsonProcessingException {
        return mapper.writeValueAsString(this);
    }

    public String toString() {
        return "To-Do: Implement Humon.toString()";
    }
}
