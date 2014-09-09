package com.stredm.flume.util;

public class TimeUtils {

	/**
	 * Function to convert milliseconds time to Timer Format
	 * Hours:Minutes:Seconds
	 * */
	public String milliSecondsToTimer(long milliseconds) {
		String finalTimerString = "";

		// Convert total duration into time
		int hours = (int) (milliseconds / (1000 * 60 * 60));
		int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
		int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
		// Add hours if there
		if (hours > 0) {
			finalTimerString = hours + ":";
		}

		// Prepending 0 to seconds if it is one digit
		String secondsString = "" + seconds;
		if (seconds < 10) {
			secondsString = "0" + seconds;
		}

		String minutesString = "" + minutes;
		if (minutes < 10) {
			minutesString = "0" + minutes;
		}

		finalTimerString = finalTimerString + minutesString + ":"
				+ secondsString;

		// return timer string
		return finalTimerString;
	}

	/**
	 * Function to get Progress percentage
	 * 
	 * @param currentDuration
	 * @param totalDuration
	 * */
	public int getProgressPercentage(long currentDuration, long totalDuration) {
		Double percentage = (double) 0;

		long currentSeconds = (int) (currentDuration / 1000);
		long totalSeconds = (int) (totalDuration / 1000);

		// calculating percentage
		percentage = (((double) currentSeconds) / totalSeconds) * 100;

		// return percentage
		return percentage.intValue();
	}

	/**
	 * Function to change progress to timer
	 * 
	 * @param progress
	 *            -
	 * @param totalDuration
	 *            returns current duration in milliseconds
	 * */
	public int progressToTimer(int progress, int totalDuration) {
		int currentDuration = 0;
		totalDuration = totalDuration / 1000;
		currentDuration = (int) ((((double) progress) / 100) * totalDuration);

		// return current duration in milliseconds
		return currentDuration * 1000;
	}

	public int timerToMilliSeconds(String time) {
		int msecs = 0;
		if (time.length() == 5) {
			int minutes = Integer.parseInt(time.substring(0, 2));
			int seconds = Integer.parseInt(time.substring(3, 5));
			int secs = minutes * 60 + seconds;
			msecs = secs * 1000;
		} else if (time.length() == 6) {
			int minutes = Integer.parseInt(time.substring(0, 3));
			int seconds = Integer.parseInt(time.substring(4, 6));
			int secs = minutes * 60 + seconds;
			msecs = secs * 1000;
		}

		return msecs;
	}
}