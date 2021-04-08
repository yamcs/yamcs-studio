package com.windhoverlabs.studio.time;

import java.time.Instant;
import java.util.Calendar;
import java.util.concurrent.TimeUnit; 

public class CFETime {

	/*
	/* Refer to airliner/core/base/cfe/fsw/src/time/cfe_time_api.c                                                                      
	/* CFE_TIME_Sub2MicroSecs() -- convert sub-seconds to micro-seconds        
	/*                                                                         
	*/

	 public static long  CFE_TIME_Sub2MicroSecs(long SubSeconds)
	{
	    long MicroSeconds;
		
	    /* 0xffffdf00 subseconds = 999999 microseconds, so anything greater 
	     * than that we set to 999999 microseconds, so it doesn't get to
	     * a million microseconds */
	    
		if (SubSeconds > 0xffffdf00)
		{
				MicroSeconds = 999999;
		}
	    else
	    {
	        /*
	        **  Convert a 1/2^32 clock tick count to a microseconds count
	        **
	        **  Conversion factor is  ( ( 2 ** -32 ) / ( 10 ** -6 ) ).
	        **
	        **  Logic is as follows:
	        **    x * ( ( 2 ** -32 ) / ( 10 ** -6 ) )
	        **  = x * ( ( 10 ** 6  ) / (  2 ** 32 ) )
	        **  = x * ( ( 5 ** 6 ) ( 2 ** 6 ) / ( 2 ** 26 ) ( 2 ** 6) )
	        **  = x * ( ( 5 ** 6 ) / ( 2 ** 26 ) )
	        **  = x * ( ( 5 ** 3 ) ( 5 ** 3 ) / ( 2 ** 7 ) ( 2 ** 7 ) (2 ** 12) )
	        **
	        **  C code equivalent:
	        **  = ( ( ( ( ( x >> 7) * 125) >> 7) * 125) >> 12 )
	        */   

	    	MicroSeconds = (((((SubSeconds >> 7) * 125) >> 7) * 125) >> 12);
	    

	        /* if the Subseconds % 0x4000000 != 0 then we will need to
	         * add 1 to the result. the & is a faster way of doing the % */  
		    if ((SubSeconds & 0x3ffffff) != 0)
	    	{
		    	MicroSeconds++;
	    	}
	    
	        /* In the Micro2SubSecs conversion, we added an extra anomaly
	         * to get the subseconds to bump up against the end point,
	         * 0xFFFFF000. This must be accounted for here. Since we bumped
	         * at the half way mark, we must "unbump" at the same mark 
	         */
	        if (MicroSeconds > 500000)
	        {
	            MicroSeconds --;
	        }
	        
	    } /* end else */
	    
	    return(MicroSeconds);

	} /* End of CFE_TIME_Sub2MicroSecs() */

	public static long secondsToMilliseconds(long seconds) 
	{
		return seconds * 1000;
	}
	
	public static long microSecondsToMilliseconds(long microSeconds) 
	{
		return microSeconds / 1000;
	}
	
	public static long yearToMilliseconds(long years) 
	{
		return years * 31557600000L;
	}
	
	/**
	 * Return relative since the Epoch. In java this is the Unix Epoch: 1970-01-01T00:00:00Z. Very useful
	 * for representing CFE relative time such as MET time.
	 * @param seconds
	 * @param subSeconds
	 * @return
	 */
	 public static Instant getRelativeTime( long seconds, long subSeconds) 
	{
		long subMicroSecs = CFE_TIME_Sub2MicroSecs(subSeconds);
			
		//Convert time to milliseconds since that is what the Time API supports
		long secondsMilliseconds = secondsToMilliseconds(seconds);
		long subMilliSecs = microSecondsToMilliseconds(subMicroSecs);
		
		return Instant.ofEpochMilli(secondsMilliseconds + subMilliSecs);
	}
 
	 /**
	  * Return relative since the Epoch. In java this is the Unix Epoch: 1970-01-01T00:00:00Z. Very useful
	  * for representing CFE relative time such as MET time.
	  * @param seconds
	  * @param subSeconds
	  * @return
	  */
	  public static Instant getTimeSinceEpoch(long seconds, long subSeconds, long epochYear, long epochDay, long epochHour, long epochMinute, long epochSecond) 
	{
	 	long subMicroSecs = CFE_TIME_Sub2MicroSecs(subSeconds);
	 		
	 	//Convert time to milliseconds since that is what the Time API supports
	 	long secondsMilliseconds = secondsToMilliseconds(seconds);
	 	long subMilliSecs = microSecondsToMilliseconds(subMicroSecs);

//		long timestamp = bornDate.getTime();
		Calendar javaEpochCalendar = Calendar.getInstance();
		javaEpochCalendar.setTimeInMillis(0);
//return cal.get(Calendar.YEAR);
		
		System.out.println("cal-->" + javaEpochCalendar.get(Calendar.DAY_OF_YEAR));
		
		long epochYearDelta =  epochYear -  javaEpochCalendar.get(Calendar.YEAR);
		long epochYearDeltaInMilliseconds = javaEpochCalendar.get(Calendar.YEAR);
		
		if (epochYearDelta>0) 
		{
			
			epochYearDeltaInMilliseconds = yearToMilliseconds(epochYearDelta);
		}
		
		long epochDayDeltaInMilliseconds = (int)TimeUnit.DAYS.toMillis(javaEpochCalendar.get(Calendar.DAY_OF_YEAR - 1) -  (epochDay-1));
		long epochHourDeltaInMilliseconds = (int)TimeUnit.HOURS.toMillis(javaEpochCalendar.get(Calendar.HOUR_OF_DAY) -  (epochHour));
		long epochMinuteDeltaInMilliseconds = (int)TimeUnit.MINUTES.toMillis(javaEpochCalendar.get(Calendar.MINUTE) -  (epochMinute));

		
		Instant javaEpoch = Instant.ofEpochMilli(0);
	 	Instant newEpoch = Instant.ofEpochMilli(epochYearDeltaInMilliseconds 
	 											+ epochDayDeltaInMilliseconds
	 											+ epochHourDeltaInMilliseconds
	 											+ epochMinuteDeltaInMilliseconds);
	 	//TODO:Test in Studio.
	 	System.out.println("newEpoch-->" + newEpoch);
//	 	javaEpoch.until(javaEpoch, null)
	 	
//	 	epochYearSeconds =   31557600
	 	
	 	return Instant.ofEpochMilli(secondsMilliseconds + subMilliSecs);
	}

}
