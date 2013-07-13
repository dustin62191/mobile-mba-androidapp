/*
2013 Measuring Broadband America Program
Mobile Measurement Android Application
Copyright (C) 2012  SamKnows Ltd.

The FCC Measuring Broadband America (MBA) Program's Mobile Measurement Effort developed in cooperation with SamKnows Ltd. and diverse stakeholders employs an client-server based anonymized data collection approach to gather broadband performance data in an open and transparent manner with the highest commitment to protecting participants privacy.  All data collected is thoroughly analyzed and processed prior to public release to ensure that subscribers’ privacy interests are protected.

Data related to the radio characteristics of the handset, information about the handset type and operating system (OS) version, the GPS coordinates available from the handset at the time each test is run, the date and time of the observation, and the results of active test results are recorded on the handset in JSON(JavaScript Object Notation) nested data elements within flat files.  These JSON files are then transmitted to storage servers at periodic intervals after the completion of active test measurements.

This Android application source code is made available under the GNU GPL2 for testing purposes only and intended for participants in the SamKnows/FCC Measuring Broadband American program.  It is not intended for general release and this repository may be disabled at any time.


This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/


package com.samknows.measurement.util;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * It is not thread safe!
 * @author ymyronovych
 *
 */
public class TimeUtils {
	public static final String dateLogFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	public static String logString(long time){
		SimpleDateFormat sdf = new SimpleDateFormat(dateLogFormat);
		return sdf.format(new Date(time));
	}
	
	private static Calendar cleanToDay(Calendar c) {
		c.set(GregorianCalendar.HOUR_OF_DAY, 0);
		c.set(GregorianCalendar.MINUTE, 0);
		c.set(GregorianCalendar.SECOND, 0);
		c.set(GregorianCalendar.MILLISECOND, 0);
		return c;
	}
	
	private static Calendar cleanToMonth(Calendar c) {
		c = cleanToDay(c);
		c.set(GregorianCalendar.DAY_OF_MONTH, 0);
		return c;
	}
	
	public static long getStartDayTime() {
		return cleanToDay(GregorianCalendar.getInstance()).getTimeInMillis();
	}
	
	public static long getStartMonthTime() {
		return cleanToMonth(GregorianCalendar.getInstance()).getTimeInMillis();
	}
	
	public static long getStartDayTime(long millis) {
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTimeInMillis(millis);
		return cleanToDay(calendar).getTimeInMillis();
	}
	
	public static long getStartNextDayTime() {
		Calendar c = cleanToDay(GregorianCalendar.getInstance());
		c.add(GregorianCalendar.DAY_OF_YEAR, 1);
		return c.getTimeInMillis();
	}

	public static long getStartNextDayTime(long millis) {
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTimeInMillis(millis);
		calendar.add(GregorianCalendar.DAY_OF_YEAR, 1);
		cleanToDay(calendar);
		return calendar.getTimeInMillis();
	}
	
	public static long daysToMillis(long days) {
		return days * 24 * 60 * 60 * 1000;
	}
	
	public static long hoursToMillis(long hours) {
		return hours * 60 * 60 * 1000;
	}
	
	public static long minutesToMillis(long minutes) {
		return minutes * 60 * 1000;
	}
	
	public static int millisToHours(long millis){
		return (int) millis/(1000*3600);
	}
	
	public static long getPreviousDayInMonth(int i){
		Calendar c = GregorianCalendar.getInstance();
		long now = c.getTimeInMillis();
		c = cleanToDay(c);
		c = setDay(c,i);
		//use the previous month
		if(now <= c.getTimeInMillis()){
			c.add(GregorianCalendar.MONTH, -1);
			c = setDay(c,i);
		}
		return c.getTimeInMillis();
	}
	
	public static Calendar setDay(Calendar c, int day){
		int actualDay = Math.min(day, c.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
		c.set(GregorianCalendar.DAY_OF_MONTH, actualDay);
		return c;
	}
	
	public static String getDayOfMonthSuffix(int n){
		String ret = n+"";
		if(n>=11 && n <=13){
			ret += "th";
		}else{
			switch(n % 10){
				case 1: ret += "st"; break;
				case 2: ret += "nd"; break;
				case 3: ret += "rd"; break;
				default: ret += "th";
			}
		}
		return ret;
	}
}
