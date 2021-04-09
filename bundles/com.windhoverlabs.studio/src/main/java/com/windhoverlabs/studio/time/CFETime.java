package com.windhoverlabs.studio.time;

import java.time.Instant;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit; 

public class CFETime {

	private static long MILLIS_IN_DAY = 86400000;
	
	private static long CFE_TIME_EPOCH_YEAR = 1980;
	private static long CFE_TIME_EPOCH_DAY = 1;
	private static long CFE_TIME_EPOCH_HOUR = 0;
	private static long CFE_TIME_EPOCH_MINUTE = 0;
	private static long CFE_TIME_EPOCH_SECOND = 0;

	
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
	
	public static long daysToMilliseconds(long years) 
	{
		return  years * MILLIS_IN_DAY;
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
	  * Return relative since the Epoch provided. Very useful
	  * for representing times like GPS time.
	  * @param seconds
	  * @param subSeconds
	  * @return
	  * @note Please note the use of Java.Instant here. This is the modern approach of handling time
	  * in the JVM. 
	  */
	  public static Instant getTimeSinceEpoch(long seconds, long subSeconds, long epochYear, long epochDay, long epochHour, long epochMinute, long epochSecond) 
	{
	 	long subMicroSecs = CFE_TIME_Sub2MicroSecs(subSeconds);
	 		
	 	//Convert time to milliseconds since that is what the Time API supports
	 	long secondsMilliseconds = secondsToMilliseconds(seconds);
	 	long subMilliSecs = microSecondsToMilliseconds(subMicroSecs);

	 	// Start an epoch that has the UNIX epoch as a value
		Instant javaEpoch = Instant.EPOCH;
				
		long epochYearDeltaInMilliseconds = 0;
		
		//Have to count for leap years
		for(int i = javaEpoch.atZone(ZoneOffset.UTC).getYear() ;i<epochYear;i++) 
		{
			epochYearDeltaInMilliseconds += daysToMilliseconds(Year.of(i).length()) ;
		}
		
		//Get the rest of the deltas relative to our UNIX Epoch 
		long dayDelta = javaEpoch.atZone(ZoneOffset.UTC).getDayOfYear() - (epochDay);
		long hourDelta = javaEpoch.atZone(ZoneOffset.UTC).getHour() -  (epochHour);
		long minuteDelta = javaEpoch.atZone(ZoneOffset.UTC).getMinute() -  (epochMinute);
		long secondDelta = javaEpoch.atZone(ZoneOffset.UTC).getSecond() -  (epochSecond);

		//Convert our deltas to milliseconds
		long epochDayDeltaInMilliseconds = TimeUnit.DAYS.toMillis(dayDelta);
		long epochHourDeltaInMilliseconds = TimeUnit.HOURS.toMillis(hourDelta);
		long epochMinuteDeltaInMilliseconds = TimeUnit.MINUTES.toMillis(minuteDelta);
		long epochSecondDeltaInMilliseconds = TimeUnit.SECONDS.toMillis(secondDelta);
			
		
		//Get our new time with our own custom awesome EPOCH
		Instant newEpoch  = Instant.ofEpochMilli(epochYearDeltaInMilliseconds 
												+ epochDayDeltaInMilliseconds
												+ epochHourDeltaInMilliseconds
												+ epochMinuteDeltaInMilliseconds
												+ epochSecondDeltaInMilliseconds
												+ secondsMilliseconds
												+ subMilliSecs);
		
	 	return newEpoch;
	}
	  
	 /**
	  * Return relative since the CFS EPOCH. At the time of writing the CFS Epoch
	  * is described as the following:
	  *  CFE_TIME_EPOCH_YEAR: 1980
         CFE_TIME_EPOCH_DAY: 1
         CFE_TIME_EPOCH_HOUR 0
         CFE_TIME_EPOCH_MINUTE:0
         CFE_TIME_EPOCH_SECOND: 0
	  * @param seconds
	  * @param subSeconds
	  * @return
	  * @note Please note the use of Java.Instant here. This is the modern approach of handling time
	  * in the JVM. 
	  */
	  public static Instant getTimeSinceCFSEpoch(long seconds, long subSeconds) 
	  {
		return  getTimeSinceEpoch(seconds, subSeconds, CFE_TIME_EPOCH_YEAR, CFE_TIME_EPOCH_DAY, CFE_TIME_EPOCH_HOUR, CFE_TIME_EPOCH_MINUTE, CFE_TIME_EPOCH_SECOND);
	  }

}
