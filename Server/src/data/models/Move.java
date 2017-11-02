package data.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        return name;
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
        return description;
    }

    @Override
    public String toString() {
        return name;
    }

    public String toJson(ObjectMapper mapper) throws JsonProcessingException{
        return mapper.writeValueAsString(this);
    }

}
