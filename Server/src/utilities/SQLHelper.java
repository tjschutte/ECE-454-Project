package utilities;

public class SQLHelper {

	/**
	 * Takes in a string, and adds delimiters to single quotes
	 * @param string - the string to cleanup
	 * @return
	 */
	public static String sqlString(String string) {
		return string.replaceAll("(?<!\\\\)'", "\\\\'");
	}
}
