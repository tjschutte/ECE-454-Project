package utilities;

public class SQLHelper {

	public static String sqlString(String string) {
		return string.replaceAll("(?<!\\\\)'", "\\\\'");
	}
}
