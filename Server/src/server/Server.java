package server;

import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

//import java.sql.*; // Coming soon to a Database connector near you!

import data.models.*;

public class Server {

	public static void main(String[] args) {
		
		System.out.println("╔═══════════════════════════════════╗");
		System.out.println("║      ECE 454 Hú-mon Server!       ║");
		System.out.println("║ Some other information goes here! ║");
		System.out.println("║ Some other information goes here! ║");
		System.out.println("║ Some other information goes here! ║");
		System.out.println("╚═══════════════════════════════════╝");
		
		// Mapper for converting from POJO to JSON
		ObjectMapper mapper = new ObjectMapper();

		ArrayList<Humon> temp = new ArrayList<Humon>();
		temp.add(new Humon(mapper, "test1"));
		temp.add(new Humon(mapper, "test2"));
		temp.add(new Humon(mapper, "test3"));
		temp.add(new Humon(mapper, "test4"));
		
		User user = new User(mapper, "Tom", "myPasshash", temp);
		
		try {
			System.out.println(user.toJson());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

	}

}
