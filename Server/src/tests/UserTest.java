package tests;
import static org.junit.Assert.*;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import data.models.User;

public class UserTest {

	@Test
	public void test() {
		User test = new User("Email@email.com", "myPassword", 0, false);
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
		User test = new User("Email@email.com", "myPassword", 0, false);
		assert(test.getParty() == null);
		test.addPartyMember("SomeHumon");
		assert(test.getParty() != null);
	}
	
	@Test
	public void testAddAndGetFriends() {
		User test = new User("Email@email.com", "myPassword", 0, false);
		assert(test.getFriends() == null);
		test.addPartyMember("SomeHumon");
		assert(test.getFriends() != null);
	}
	
	@Test
	public void testAddAndGetEncounteredHumons() {
		User test = new User("Email@email.com", "myPassword", 0, false);
		assert(test.getEncounteredHumons() == null);
		test.addPartyMember("SomeHumon");
		assert(test.getEncounteredHumons() != null);
	}

}
