package top.mrxiaom.doomsdayessentials.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TimeUtil {

	public static final TimeUtil ZERO = new TimeUtil(0, 0, 0, 0, 0, 0);
	int year;
	int month;
	int day;
	int hour;
	int minute;
	int second;

	private TimeUtil(int year, int month, int day, int hour, int minute, int second) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.hour = hour;
		this.minute = minute;
		this.second = second;
	}

	public static TimeUtil of(int year, int month, int day) {
		return of(year, month, day, 0, 0, 0);
	}

	public static TimeUtil of(int year, int month, int day, int hour, int minute, int second) {
		return new TimeUtil(year, month, day, hour, minute, second);
	}

	public static String getDateString() {
		return LocalDate.now().toString();
	}
	public static String getDateTimeString() {
		return LocalDateTime.now().toString();
	}

	public static String getChineseTimeDay(long longTime) {
		return getChineseTimeDay(longTime, "", "");
	}

	public static String getChineseTimeDay(long longTime, String s, String e) {
		String result = "";
		int second = (int) ((longTime / 1000) % 60);
		int minute = (int) ((longTime / 1000 / 60) % 60);
		int hour = (int) ((longTime / 1000 / 60 / 60) % 24);
		int day = (int) ((longTime / 1000 / 60 / 60 / 24));

		if (day > 0)
			result += s + day + e + "天 ";
		if (hour > 0)
			result += s + hour + e + "时";
		if (minute > 0)
			result += s + minute + e + "分";
		if (second > 0)
			result += s + second + e + "秒";

		return result;
	}

	public static String getChineseTimeBetweenNow(LocalDateTime one) {
		return getChineseTimeBetween(LocalDateTime.now(), one);
	}

	public static String getChineseTimeBetween(LocalDateTime one, LocalDateTime two) {
		return getChineseTimeBetween(one, two, "", "");
	}

	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}

	public int getDay() {
		return day;
	}

	public int getHour() {
		return hour;
	}

	public int getMinute() {
		return minute;
	}

	public int getSecond() {
		return second;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}

	public void setSecond(int second) {
		this.second = second;
	}

	public static TimeUtil between(LocalDateTime one, LocalDateTime two) {
		if (one.isEqual(two))
			return TimeUtil.ZERO;
		LocalDateTime timeBefore = one.isBefore(two) ? one : two;
		LocalDateTime timeAfter = one.isAfter(two) ? one : two;

		LocalDateTime between = LocalDateTime.from(timeBefore);
		long years = between.until(timeAfter, ChronoUnit.YEARS);
		between = between.plusYears(years);
		long months = between.until(timeAfter, ChronoUnit.MONTHS);
		between = between.plusMonths(months);
		long days = between.until(timeAfter, ChronoUnit.DAYS);
		between = between.plusDays(days);
		long hours = between.until(timeAfter, ChronoUnit.HOURS);
		between = between.plusHours(hours);
		long minutes = between.until(timeAfter, ChronoUnit.MINUTES);
		between = between.plusMinutes(minutes);
		long seconds = between.until(timeAfter, ChronoUnit.SECONDS);

		return new TimeUtil((int) years, (int) months, (int) days, (int) hours, (int) minutes, (int) seconds);
	}

	public static String getChineseTimeBetween(LocalDateTime one, LocalDateTime two, String s, String e) {
		TimeUtil target = TimeUtil.between(one, two);
		int second = target.getSecond();
		int minute = target.getMinute();
		int hour = target.getHour();
		int day = target.getDay();
		int month = target.getMonth();
		int year = target.getYear();
		String result = "";
		if (year > 0)
			result += s + year + e + "年";
		if (month > 0)
			result += s + month + e + "月";
		if (day > 0)
			result += s + day + e + "日 ";
		if (hour > 0)
			result += s + hour + e + "时";
		if (minute > 0)
			result += s + minute + e + "分";
		if (second > 0)
			result += s + second + e + "秒";
		return result;
	}

	@Deprecated
	public static String getChineseTime_Old_Old(long longTime, String s, String e) {
		String result = "";

		int second = (int) ((longTime / 1000) % 60);
		int minute = (int) ((longTime / 1000 / 60) % 60);
		int hour = (int) ((longTime / 1000 / 60 / 60) % 24);
		int day = (int) ((longTime / 1000 / 60 / 60 / 24) % 30);
		int month = (int) ((longTime / 1000 / 60 / 60 / 24 / 30) % 12);
		int year = (int) ((longTime / 1000 / 60 / 60 / 24 / 30 / 12));

		if (year > 0)
			result += s + year + e + "年";
		if (month > 0)
			result += s + month + e + "月";
		if (day > 0)
			result += s + day + e + "日 ";
		if (hour > 0)
			result += s + hour + e + "时";
		if (minute > 0)
			result += s + minute + e + "分";
		if (second > 0)
			result += s + second + e + "秒";
		return result;
	}

	@Deprecated
	public static String getChineseTime_Old(long longTime, String s, String e) {
		String result = "";

		int second = (int) ((longTime / 1E+03) % 60);
		int minute = (int) ((longTime / 6E+04) % 60);
		int hour = (int) ((longTime / 3.6E+06) % 24);
		int day = (int) ((longTime / 8.64E+07) % 30);
		int month = (int) ((longTime / 2.592E+08) % 12);
		int year = (int) ((longTime / 3.1104E+010));

		if (year > 0)
			result += s + year + e + "年";
		if (month > 0)
			result += s + month + e + "月";
		if (day > 0)
			result += s + day + e + "日 ";
		if (hour > 0)
			result += s + hour + e + "时";
		if (minute > 0)
			result += s + minute + e + "分";
		if (second > 0)
			result += s + second + e + "秒";
		return result;
	}

	public static String getChineseTime(LocalDateTime time, String s, String e) {
		String result = "";
		int year = time.getYear();
		int month = time.getMonthValue();
		int day = time.getDayOfMonth();
		int hour = time.getHour();
		int minute = time.getMinute();
		int second = time.getSecond();
		if (year > 0)
			result += s + year + e + "年";
		if (month > 0)
			result += s + month + e + "月";
		if (day > 0)
			result += s + day + e + "日 ";
		if (hour > 0)
			result += s + hour + e + "时";
		if (minute > 0)
			result += s + minute + e + "分";
		if (second > 0)
			result += s + second + e + "秒";
		return result;
	}

	public static LocalDateTime addDay(LocalDateTime time, LocalDateTime nullTime, int day) {
		if (time == null)
			Util.logger.warning("[DEBUG] 时间获取失败");
		LocalDateTime r = LocalDateTime.from(time == null ? nullTime : time);
		return r.plusDays(day);
	}

	public static LocalDateTime addDay(LocalDateTime time, int day) {
		return addDay(time, LocalDateTime.now(), day);
	}

	public static LocalDateTime addDay(int day) {
		return addDay(LocalDateTime.now(), day);
	}

	public static String getChineseTime(LocalDateTime time) {
		return getChineseTime(time, "", "");
	}
}
