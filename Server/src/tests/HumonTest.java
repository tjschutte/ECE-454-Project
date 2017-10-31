package tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import data.models.Humon;

public class HumonTest {

	@Test
	public void test() {
		try {
		Humon h = new Humon("Name", "Description", "No image", 0, 0, 0, "", "", null, 0, 0, 0, 0, 0);
		String testJSON = h.toJson(new ObjectMapper());
		
		Humon h2 = new ObjectMapper().readValue(testJSON, Humon.class);
		String testJSON2 = h2.toJson(new ObjectMapper());
		
		assert(testJSON.equals(testJSON2));
		} catch (Exception e) {
			fail("Something went wrong with Jackson");
		}
	}

}
