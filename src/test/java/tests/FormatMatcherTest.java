package tests;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatMatcherTest {

	static Pattern pattern;
	static Matcher matcher;
	
	public static String format(String input, String regex) {
		pattern = Pattern.compile(regex);
	    matcher = pattern.matcher("");
	    //for each input string
	    matcher.reset(input);
	    return matcher.replaceAll("$1");
	}
	public static String format2(String input, String initialFormat, String format) throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat(format);
		SimpleDateFormat parser = new SimpleDateFormat(initialFormat);
		return df.format(parser.parse(input));
	}
	
	public static void main(String[] args) throws ParseException {
//	    System.out.println(format("04040404. Subject, Uni Name, Location 2", "(\\d++)\\.([^,]++),\\s*+([^,]++),\\s*+(.*+)"));
//	    System.out.println(format("00112233", "d"));
	    // esta mierda de arriba no funciona
	    
	    // match2
	    System.out.println(format2("151228", "yyMMdd", "yyyy-MM-dd"));
	    
	    //match 3
	    System.out.println(format2("151229000343", "yyMMddhhmmss", "yyyy-MM-dd hh:mm:ss"));
	    
	    System.out.println(format2("00034345", "hhmmssSS", "HH:mm:ss"));
	}
}
