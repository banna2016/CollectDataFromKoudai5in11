package main.java.echart.collect;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CollectUtil {

	public static String getNextDay(String day)
	  {
	    SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");
	    Calendar calendar = new GregorianCalendar();
	    String dateString = null;
	    try
	    {
	      Date date = formatter.parse(day);
	      calendar.setTime(date);
	      calendar.add(5, 1);
	      date = calendar.getTime();
	      dateString = formatter.format(date);
	    }
	    catch (ParseException e)
	    {
	      e.printStackTrace();
	    }
	    return dateString;
	  }
	
	public static String getNextIssueNumber(String issueNumber)
	  {
	    String nextIssueNumber = null;
	    String issueCode = issueNumber.substring(issueNumber.length() - 2, issueNumber.length());
	    if (issueCode.equals(App.lineCount))
	    {
	      nextIssueNumber = getNextDay(issueNumber.substring(0, 6)) + "01";
	    }
	    else
	    {
	      int codeInt = Integer.parseInt(issueCode) + 1;
	      if (codeInt < 10) {
	        nextIssueNumber = issueNumber.substring(0, issueNumber.length() - 2) + "0" + codeInt;
	      } else {
	        nextIssueNumber = issueNumber.substring(0, issueNumber.length() - 2) + codeInt;
	      }
	    }
	    return nextIssueNumber;
	  }
	
	 public static String translate(int temp)
	  {
	    String rtn = null;
	    if (temp < 10) {
	      rtn = temp+"";
	    } else if (temp == 10) {
	      rtn = "A";
	    } else if (temp == 11) {
	      rtn = "J";
	    } else if (temp == 12) {
	      rtn = "Q";
	    }
	    return rtn;
	  }
}
