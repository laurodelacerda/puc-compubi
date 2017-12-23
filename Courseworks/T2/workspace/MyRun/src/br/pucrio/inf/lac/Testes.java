package br.pucrio.inf.lac;

import java.util.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Testes {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		
		Long timestamp = 1513275130726L ;
		
		
		
		Date date = new Date((long)1513268645 * 1000);
		Date date2 = new Date((long)1513275130726L);
	
		LocalDateTime time = millsToLocalDateTime(timestamp);
		
		
		System.out.println(date2);
		System.out.println(time);
	}
	
	public static LocalDateTime millsToLocalDateTime(long millis) {
	    Instant instant = Instant.ofEpochMilli(millis);
	    LocalDateTime date = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
	    return date;
	}	
	
//	public class MillisToLocalDateTimeExample {
//		  public static void main(String[] args) {
//		      long m = System.currentTimeMillis();
//		      LocalDateTime d = millsToLocalDateTime(m);
//		      System.out.println(d);
//		  }
//
//		  public static LocalDateTime millsToLocalDateTime(long millis) {
//		      Instant instant = Instant.ofEpochMilli(millis);
//		      LocalDateTime date = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
//		      return date;
//		  }
//		}

}