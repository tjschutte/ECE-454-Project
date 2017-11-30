package edu.wisc.ece454.hu_mon.Models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Random;

public class Humon extends Jsonable implements Parcelable{

    private String name; 			// humon name
    private String description;     // Description about how kewl your humon is
    private Bitmap image;			// image of humon
    private String imagePath;       // location of image file on phone
    private int level;              // The current level of the humon-instance
    private int xp;                 // current amount of xp
    private int hp;                 // current amount of hp
    private int hID;     			// hID will map a humon to is details in storage (picture, name, moves, etc)
    private String uID; 			// uID will map a humon instance to a user
    private String iID;				// iID will map a humon to an instance. iID will be a concatination of uID and a count <uID-count>
    private ArrayList<Move> moves;  // list of moves a humon can perform
    private int health; 		    // the health of a humon. All humons start with 100 hp.
    private int luck;               // How lucky your humon is
    private int attack;             // How much bonehurtingjuice
    private int speed;              // GOTTA GO FAST
    private int defense;            // how much alcohol your humon can drink

    // Moves will be a combination of <Name, id> to m ap to template moves.

    public Humon(String name, String description, Bitmap image, int level, int xp, int hID, String uID,
                 String iID, ArrayList<Move> moves, int health, int luck, int attack, int speed, int defense,
                 String imagePath, int hp) {
        this.name = name;
        this.description = description;
        this.image = image;
        this.imagePath = imagePath;
        this.level = level;
        this.xp = xp;
        this.hp = hp;
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

    public void setName(String name) {this.name = name;}

    public String getDescription() {
        return description;
    }

    public String getImage() {
        if(image != null) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, bytes);
            return Base64.encodeToString(bytes.toByteArray(), Base64.DEFAULT);
        }
        else {
            return "";
        }
    }

    public String getImagePath() {
        return imagePath;
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

    public void setIID(String iID) {
        this.iID = iID;
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

    public int getHp() { return hp;}

    public void setLevel(int level) {
        this.level = level;
    }

    public void setHp(int hp) {
        if(hp < 0) {
            hp = 0;
        }
        if(hp > health) {
            hp = health;
        }
        this.hp = hp;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void setLuck(int luck) {
        this.luck = luck;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    //Returns amount of experience points for next level up
    @JsonIgnore
    public int getMaxXp() {
        return level * 5;
    }

    public void addXp(int experience) {
        xp += experience;

        if(xp >= getMaxXp()) {
            xp -= getMaxXp();
            levelUp();
        }
    }

    public void levelUp() {
        //increment stats
        Random rng = new Random();
        int chance = rng.nextInt(10);
        if(chance < 2) {
            health += 20;
        }
        else {
            health += 10;
        }
        chance = rng.nextInt(10);
        if(chance < 2) {
            attack += 2;
        }
        else {
            attack++;
        }
        chance = rng.nextInt(10);
        if(chance < 2) {
            defense += 2;
        }
        else {
            defense++;
        }
        chance = rng.nextInt(10);
        if(chance < 2) {
            speed += 2;
        }
        else {
            speed++;
        }
        chance = rng.nextInt(10);
        if(chance < 2) {
            luck += 2;
        }
        else {
            luck++;
        }

        //Fully heal after level up
        hp = health;

        level++;

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

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
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

    //
    public String toString() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeValue(image);
        dest.writeString(imagePath);
        dest.writeInt(level);
        dest.writeInt(xp);
        dest.writeInt(hp);
        dest.writeInt(hID);
        dest.writeString(uID);
        dest.writeString(iID);
        dest.writeArray(moves.toArray());
        dest.writeInt(health);
        dest.writeInt(luck);
        dest.writeInt(attack);
        dest.writeInt(speed);
        dest.writeInt(defense);
    }

    public static final Parcelable.Creator<Humon> CREATOR =
            new Parcelable.Creator<Humon>() {

                @Override
                public Humon createFromParcel(Parcel source) {
                    return new Humon(source);
                }

                @Override
                public Humon[] newArray(int size) {
                    return new Humon[size];
                }
            };

    private Humon(Parcel in) {
        name = in.readString();
        description = in.readString();
        image = (Bitmap) in.readValue(null);
        imagePath = in.readString();
        level = in.readInt();
        xp = in.readInt();
        hp = in.readInt();
        hID = in.readInt();
        uID = in.readString();
        iID = in.readString();
        Object[] moveIn = in.readArray(Move.class.getClassLoader());
        moves = new ArrayList<Move>();
        for(int i = 0;  i < moveIn.length; i++) {
            moves.add((Move)moveIn[i]);
        }
        health = in.readInt();
        luck = in.readInt();
        attack = in.readInt();
        speed = in.readInt();
        defense = in.readInt();
    }
}
