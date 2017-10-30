package edu.wisc.ece454.hu_mon.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Move extends Jsonable implements Parcelable {

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeValue(selfCast);
        dest.writeInt(dmg);
        dest.writeValue(effect);
        dest.writeValue(hasEffect);
        dest.writeString(description);
    }

    public static final Parcelable.Creator<Move> CREATOR =
            new Parcelable.Creator<Move>() {

                @Override
                public Move createFromParcel(Parcel source) {
                    return new Move(source);
                }

                @Override
                public Move[] newArray(int size) {
                    return new Move[size];
                }
            };

    private Move(Parcel in) {
        id = in.readInt();
        name = in.readString();
        selfCast = (boolean) in.readValue(null);
        dmg = in.readInt();
        effect = (Effect) in.readValue(null);
        hasEffect = (boolean) in.readValue(null);
        description = in.readString();
    }

    public enum Effect {
        PARALYZED, CONFUSED, SLEPT, POISONED, EMBARASSED
    }

    private int id;
    private String name;
    private boolean selfCast;
    private int dmg;
    private Effect effect;
    private boolean hasEffect;
    private String description;


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
