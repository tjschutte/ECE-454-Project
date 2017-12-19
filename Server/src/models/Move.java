package models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import utilities.SQLHelper;

public class Move {
	
	public enum Effect {
        PARALYZED, CONFUSED, SLEPT, POISONED, EMBARRASSED
    }

    private int id;
    private String name;
    private boolean selfCast;
    private int dmg;
    private Effect effect;
    private boolean hasEffect;
    private String description;

    /**
     * Default constructor for Jackson
     */
    public Move() {
    	
    }

    public Move(int id, String name, boolean selfCast, int dmg, Effect effect, boolean hasEffect, String description) {
        this.id = id;
        this.name = name;
        this.selfCast = selfCast;
        this.dmg = dmg;
        this.effect = effect;
        this.hasEffect = hasEffect;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return SQLHelper.sqlString(name);
    }

    public boolean getSelfCast() {
        return selfCast;
    }

    public boolean isSelfCast() {
        return selfCast;
    }

    public int getDmg() {
        return dmg;
    }

    public Effect getEffect() {
        return effect;
    }

    public boolean isHasEffect() {
        return hasEffect;
    }

    public String getDescription() {
        return SQLHelper.sqlString(description);
    }
    
    @Override
    public String toString() {
    	try {
			return this.toJson(new ObjectMapper());
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return "";
    }

    public String toJson(ObjectMapper mapper) throws JsonProcessingException{
        return mapper.writeValueAsString(this);
    }

}
