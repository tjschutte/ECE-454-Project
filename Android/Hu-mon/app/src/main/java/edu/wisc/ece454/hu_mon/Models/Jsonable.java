package edu.wisc.ece454.hu_mon.Models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Objects that will be transferable to the sever need to extend this class.
 */
public abstract class Jsonable {

    /**
     * Turns object into a JSON representation using public fields, or private fields with
     * getter methods.
     * @return JSON String representation of the object.
     * @throws JsonProcessingException
     */
    public abstract String toJson(ObjectMapper mapper) throws JsonProcessingException;

}
