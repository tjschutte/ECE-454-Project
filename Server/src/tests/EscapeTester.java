package tests;

import org.junit.Test;

import utilities.SQLHelper;

public class EscapeTester {

	@Test
	public void test() {
		String testString = "This is a test, it's pretty dumb don't 'cha think";
		testString = SQLHelper.sqlString(testString);
		System.out.println(testString);
		System.out.println("This is a test, it\\'s pretty dumb don\\'t \\'cha think");
		assert(testString.equals("This is a test, it\\'s pretty dumb don\\'t \\'cha think"));
	}

}
