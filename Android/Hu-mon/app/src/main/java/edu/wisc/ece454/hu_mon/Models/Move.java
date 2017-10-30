package edu.wisc.ece454.hu_mon.Models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Move extends Jsonable {

    public enum Effect {
        PARALYZED, CONFUSED, SLEPT, POISONED, EMBARRASSED
    }

    private int id;
    private String name;
    private boolean selfCast;
    private int dmg;
    private Effect effect;
    private String description;


    public Move() {

    }

    public String toJson(ObjectMapper mapper) throws JsonProcessingException{
        return mapper.writeValueAsString(this);
    }

}
