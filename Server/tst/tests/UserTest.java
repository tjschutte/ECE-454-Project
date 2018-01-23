package tests;
import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.User;

public class UserTest {

	@Test
	public void test() {
		User test = new User("Email@email.com", "myPassword", 0, "", false, false);
        try {
        String testJSON = test.toJson(new ObjectMapper());
        
        User test2 = new ObjectMapper().readValue(testJSON, User.class);
        String testJSON2 = test2.toJson(new ObjectMapper());
        
        assert(testJSON.equals(testJSON2));
        
        } catch (Exception e) {
        	fail("Something went wrong with Jackson");
        }
	}
	
	@Test
	public void testAddAndGetParty() {
		User test = new User("Email@email.com", "myPassword", 0, "", false, false);
		//assert(test.getParty() == null);
		test.addPartyMember("SomeHumon");
		assert(test.getParty() != null);
	}
	
	@Test
	public void testAddAndGetFriends() {
		User test = new User("Email@email.com", "myPassword", 0, "", false, false);
		//assert(test.getFriends() == null);
		test.addPartyMember("SomeHumon");
		assert(test.getFriends() != null);
	}
	
	@Test
	public void testAddAndGetEncounteredHumons() {
		User test = new User("Email@email.com", "myPassword", 0, "", false, false);
		//assert(test.getEncounteredHumons() == null);
		test.addPartyMember("SomeHumon");
		assert(test.getEncounteredHumons() != null);
	}
	
	@Test
	public void testArrayCleaner() throws JsonParseException, JsonMappingException, IOException {
		User test = new User("Email@email.com", "myPassword", null, null, "\"[[test_email2\" , \"gggg\"]", null, 0, "", false, false);
		System.out.println(test.toJson(new ObjectMapper()));
		assert(test != null);
	}

}
