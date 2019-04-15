package com.developmentontheedge.be5.base.util;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class DateUtils
{
    private static final DateFormatSymbols dateFormatSymbolsRussianCases = getDateFormatSymbolsRussianCases();
    private static final SimpleDateFormat dateFormatRussianCases = getDateFormatRussianCases();
    private static final SimpleDateFormat dateFormatRussianCasesQuotes = getDateFormatRussianCasesQuotes();

    public static final String[][] MONTHS_TAGS_RU = {
            {"1", "январь"},
            {"2", "февраль"},
            {"3", "март"},
            {"4", "апрель"},
            {"5", "май"},
            {"6", "июнь"},
            {"7", "июль"},
            {"8", "август"},
            {"9", "сентябрь"},
            {"10", "октябрь"},
            {"11", "ноябрь"},
            {"12", "декабрь"}};


    public static java.util.Date convert(Object val)
    {
        return (java.util.Date) Utils.changeType(val, java.sql.Date.class);
    }

    public static java.sql.Timestamp convertToTimestamp(Object val)
    {
        return (java.sql.Timestamp) Utils.changeType(val, java.sql.Timestamp.class);
    }

    public static Date toDate(java.sql.Timestamp timestamp)
    {
        return new java.sql.Date(timestamp.getTime());
    }

    public static Date toDate(Calendar cal)
    {
        return new java.sql.Date(cal.getTime().getTime());
    }

    public static Date toDate(Date date)
    {
        if (date == null)
            return null;
        return new java.sql.Date(date.getTime());
    }

    public static Calendar toCalendar(Calendar cal)
    {
        return cal;
    }

    public static Calendar toCalendar(java.sql.Timestamp timestamp)
    {
        Calendar cal = Calendar.getInstance();
        if (timestamp != null)
        {
            cal.setTimeInMillis(timestamp.getTime());
        }
        return cal;
    }

    public static Calendar toCalendar(Date date)
    {
        Calendar cal = Calendar.getInstance();
        if (date != null)
        {
            cal.setTime(date);
        }
        return cal;
    }

    public static MonthlyDate toMonthlyDate(Calendar cal)
    {
        return new MonthlyDate(new java.sql.Date(cal.getTime().getTime()));
    }

    public static MonthlyDate toMonthlyDate(Date date)
    {
        return new MonthlyDate(date);
    }

    public static Date prevMonthBegin()
    {
        return prevMonthBegin((Date) null);
    }

    public static Date prevMonthBegin(Date date)
    {
        return prevMonthBegin(toCalendar(date));
    }

    public static Date prevMonthBegin(Calendar cal)
    {
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, -1);
        return toDate(cal);
    }

    public static Date prevMonthEnd()
    {
        return prevMonthEnd((Date) null);
    }

    public static Date prevMonthEnd(Date date)
    {
        return prevMonthEnd(toCalendar(date));
    }

    public static Date prevMonthEnd(Calendar cal)
    {
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return toDate(cal);
    }

    public static Date nextMonthBegin()
    {
        return nextMonthBegin((Date) null);
    }

    public static Date nextMonthBegin(Date date)
    {
        return nextMonthBegin(toCalendar(date));
    }

    public static Date nextMonthBegin(Calendar cal)
    {
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, 1);
        return toDate(cal);
    }

    public static Date nextMonthEnd()
    {
        return nextMonthEnd((Date) null);
    }

    public static Date nextMonthEnd(Date date)
    {
        return nextMonthEnd(toCalendar(date));
    }

    public static Date nextMonthEnd(Calendar cal)
    {
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, 2);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return toDate(cal);
    }

    public static Date curMonthBegin()
    {
        return curMonthBegin((Date) null);
    }

    public static Date curMonthBegin(Date date)
    {
        return curMonthBegin(toCalendar(date));
    }

    public static Date curMonthBegin(Calendar cal)
    {
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return toDate(cal);
    }

    public static Date curMonthEnd()
    {
        return curMonthEnd((Date) null);
    }

    public static Date curMonthEnd(Date date)
    {
        return curMonthEnd(toCalendar(date));
    }

    public static Date curMonthEnd(Calendar cal)
    {
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return toDate(cal);
    }

    public static Date prevDay()
    {
        return prevDay((Date) null);
    }

    public static Date prevDay(Date date)
    {
        return prevDay(toCalendar(date));
    }

    public static Date prevDay(Calendar cal)
    {
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return toDate(cal);
    }

    public static Date curDay()
    {
        return toDate(toCalendar((Date) null));
    }


    public static Date nextDay()
    {
        return nextDay((Date) null);
    }

    public static Date nextDay(Date date)
    {
        return nextDay(toCalendar(date));
    }

    public static Date nextDay(Calendar cal)
    {
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return toDate(cal);
    }

    public static Date addDays(int nDays)
    {
        return addDays((Date) null, nDays);
    }

    public static Date addDays(Date date, int nDays)
    {
        Calendar cal = Calendar.getInstance();
        if (date != null)
        {
            cal.setTime(date);
        }
        cal.add(Calendar.DAY_OF_MONTH, nDays);
        return toDate(cal);
    }

    public static Date addMonths(int nMonths)
    {
        return addMonths((Date) null, nMonths);
    }

    public static Date addMonths(Date date, int nMonths)
    {
        Calendar cal = Calendar.getInstance();
        if (date != null)
        {
            cal.setTime(date);
        }
        cal.add(Calendar.MONTH, nMonths);
        return toDate(cal);
    }

    public static Date curWeekMonday()
    {
        return curWeekMonday((Date) null);
    }

    public static Date curWeekMonday(Date date)
    {
        return curWeekMonday(toCalendar(date));
    }

    public static Date curWeekMonday(Calendar cal)
    {
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return toDate(cal);
    }

    public static Date curWeekFriday()
    {
        return curWeekFriday((Date) null);
    }

    public static Date curWeekFriday(Date date)
    {
        return curWeekFriday(toCalendar(date));
    }

    public static Date curWeekFriday(Calendar cal)
    {
        cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        return toDate(cal);
    }

    public static int curYear()
    {
        return curYear((Date) null);
    }

    public static int curYear(Date date)
    {
        Calendar cal = Calendar.getInstance();
        if (date != null)
        {
            cal.setTime(date);
        }
        return cal.get(Calendar.YEAR);
    }

    public static int curYear(Calendar cal)
    {
        return cal.get(Calendar.YEAR);
    }

    public static int getYear(Date date)
    {
        return curYear(date);
    }

    public static int curMonth()
    {
        return getMonth((Date) null);
    }

    public static int getMonth(Date date)
    {
        Calendar cal = Calendar.getInstance();
        if (date != null)
        {
            cal.setTime(date);
        }
        return cal.get(Calendar.MONTH) + 1;
    }

    public static int getMonth(Calendar cal)
    {
        return cal.get(Calendar.MONTH) + 1;
    }

    public static int getDay(Date date)
    {
        Calendar cal = Calendar.getInstance();
        if (date != null)
        {
            cal.setTime(date);
        }
        return getDay(cal);
    }

    public static int getDay(Calendar cal)
    {
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public static int getHours(Calendar cal)
    {
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public static int getHours(Date date)
    {
        Calendar cal = Calendar.getInstance();
        if (date != null)
        {
            cal.setTime(date);
        }
        return getHours(cal);
    }

    public static int getMinutes(Calendar cal)
    {
        return cal.get(Calendar.MINUTE);
    }

    public static int getMinutes(Date date)
    {
        Calendar cal = Calendar.getInstance();
        if (date != null)
        {
            cal.setTime(date);
        }
        return getMinutes(cal);
    }

    public static boolean isBetween(Date date, Date from, Date to)
    {
        if (from == null)
        {
            return false;
        }
        if (to == null)
        {
            return !from.after(date);
        }
        if (to.before(from))
        {
            Date tmp = to;
            to = from;
            from = tmp;
        }
        return !from.after(date) && date.before(to);

    }

    public static boolean isSameDay(Date date1, Date date2)
    {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }

    public static boolean isTimeBefore(String time)
    {
        return isTimeBefore((Date) null, time);
    }

    public static boolean isTimeBefore(Date date, String time)
    {
        return isTimeBefore(toCalendar(date), time);
    }

    public static boolean isTimeBefore(Calendar cal, String time)
    {
        String[] timeParts = time.split(":");
        int h = Integer.parseInt(timeParts[0]);
        int m = Integer.parseInt(timeParts[1]);
        int s = Integer.parseInt(timeParts.length > 2 ? timeParts[2] : "0");
        return cal.get(Calendar.HOUR_OF_DAY) < h ||
                cal.get(Calendar.HOUR_OF_DAY) == h && cal.get(Calendar.MINUTE) < m ||
                cal.get(Calendar.HOUR_OF_DAY) == h && cal.get(Calendar.MINUTE) == m || cal.get(Calendar.SECOND) < s;
    }

    public static boolean isTimeAfter(String time)
    {
        return isTimeAfter((Date) null, time);
    }

    public static boolean isTimeAfter(Date date, String time)
    {
        return isTimeAfter(toCalendar(date), time);
    }

    public static boolean isTimeAfter(Calendar cal, String time)
    {
        String[] timeParts = time.split(":");
        int h = Integer.parseInt(timeParts[0]);
        int m = Integer.parseInt(timeParts[1]);
        int s = Integer.parseInt(timeParts.length > 2 ? timeParts[2] : "0");
        return cal.get(Calendar.HOUR_OF_DAY) > h ||
                cal.get(Calendar.HOUR_OF_DAY) == h && cal.get(Calendar.MINUTE) > m ||
                cal.get(Calendar.HOUR_OF_DAY) == h && cal.get(Calendar.MINUTE) == m || cal.get(Calendar.SECOND) > s;
    }

    public static Date makeDate(int year, int month, int day)
    {
        return toDate(makeCalendar(year, month, day));
    }

    public static Calendar makeCalendar(int year, int month, int day)
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    public static int getMonthDiff(Date from, Date to)
    {
        return getMonthDiff(toCalendar(from), toCalendar(to));
    }

    public static int getMonthDiff(Calendar from, Calendar to)
    {
        return to.get(Calendar.YEAR) * 12 + to.get(Calendar.MONTH) - (from.get(Calendar.YEAR) * 12 + from.get(Calendar.MONTH));
    }

    public static int getFullMonthDiff(Date from, Date to)
    {
        return getFullMonthDiff(toCalendar(from), toCalendar(to));
    }

    public static int getFullMonthDiff(Calendar from, Calendar to)
    {
        if (from.get(Calendar.DAY_OF_MONTH) > to.get(Calendar.DAY_OF_MONTH))
        {
            return getMonthDiff(from, to) - 1;
        }
        return getMonthDiff(from, to);
    }

    public static int getDaysDiff(Date from, Date to)
    {
        final long difference = to.getTime() - from.getTime();
        return (int) (difference / (24 * 60 * 60 * 1000));
    }

    public static int daysInMonth(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.set(getYear(date), getMonth(date) - 1, 1);
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static String monthNameRU(int month)
    {
        //String []months = { "", "январь", "февраль", "март", "апрель", "май", "июнь", "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь" };
        String[] months = {"", "\u044f\u043d\u0432\u0430\u0440\u044c", "\u0444\u0435\u0432\u0440\u0430\u043b\u044c", "\u043c\u0430\u0440\u0442", "\u0430\u043f\u0440\u0435\u043b\u044c", "\u043c\u0430\u0439", "\u0438\u044e\u043d\u044c", "\u0438\u044e\u043b\u044c", "\u0430\u0432\u0433\u0443\u0441\u0442", "\u0441\u0435\u043d\u0442\u044f\u0431\u0440\u044c", "\u043e\u043a\u0442\u044f\u0431\u0440\u044c", "\u043d\u043e\u044f\u0431\u0440\u044c", "\u0434\u0435\u043a\u0430\u0431\u0440\u044c"};
        return month > 0 && month < 13 ? months[month] : null;
    }

    private static DateFormatSymbols getDateFormatSymbolsRussianCases()
    {
        //String[] months = {"января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря"};
        String[] months = {"\u044f\u043d\u0432\u0430\u0440\u044f", "\u0444\u0435\u0432\u0440\u0430\u043b\u044f", "\u043c\u0430\u0440\u0442\u0430", "\u0430\u043f\u0440\u0435\u043b\u044f", "\u043c\u0430\u044f", "\u0438\u044e\u043d\u044f", "\u0438\u044e\u043b\u044f", "\u0430\u0432\u0433\u0443\u0441\u0442\u0430", "\u0441\u0435\u043d\u0442\u044f\u0431\u0440\u044f", "\u043e\u043a\u0442\u044f\u0431\u0440\u044f", "\u043d\u043e\u044f\u0431\u0440\u044f", "\u0434\u0435\u043a\u0430\u0431\u0440\u044f"};
        DateFormatSymbols dfs = new DateFormatSymbols(new Locale("ru"));
        dfs.setMonths(months);
        return dfs;
    }

    private static SimpleDateFormat getDateFormatRussianCases()
    {
        // "d MMMM yyyy 'г.'"
        return new SimpleDateFormat("d MMMM yyyy '\u0433.'", dateFormatSymbolsRussianCases);
    }

    private static SimpleDateFormat getDateFormatRussianCasesQuotes()
    {
        // "\"d\" MMMM yyyy 'г.'"
        return new SimpleDateFormat("\"d\" MMMM yyyy '\u0433.'", dateFormatSymbolsRussianCases);
    }

    public static String formatRussianCases(Date date)
    {
        return dateFormatRussianCases.format(date);
    }

    public static String formatRussianCasesQuotes(Date date)
    {
        return dateFormatRussianCasesQuotes.format(date);
    }

    public static String formatRussianCasesPattern(Date date, String pattern)
    {
        return new SimpleDateFormat(pattern, dateFormatSymbolsRussianCases).format(date);
    }


    public static String toAnsiDateTime(java.sql.Timestamp ts)
    {
        if (ts == null)
        {
            
return null;
        }

        String dstr = "" + ts;
        return dstr.substring(0, 19);
    }

    public static Date parse(String format, String dateStr)
    {
        String[] formatParts = format.split("/");
        String[] parts = dateStr.split("/");
        if (parts.length == 1)
        {
            formatParts = format.split("-");
            parts = dateStr.split("-");
        }
        if (parts.length == 1)
        {
            return null;
        }

        int year = 0, month = 0, day = 0;
        for (int i = 0; i < formatParts.length; i++)
        {
            if ("YYYY".equalsIgnoreCase(formatParts[i]))
            {
                year = Integer.parseInt(parts[i]);
            }
            else if ("MM".equalsIgnoreCase(formatParts[i]))
            {
                month = Integer.parseInt(parts[i]);
            }
            else if ("DD".equalsIgnoreCase(formatParts[i]))
            {
                day = Integer.parseInt(parts[i]);
            }
        }

        return makeDate(year, month, day);
    }

    public static boolean isIntersect(Date activeFrom1, Date activeTo1, Date activeFrom2, Date activeTo2)
    {
        if (null != activeTo1 && null != activeTo2 && (!activeFrom2.before(activeTo1) || !activeTo2.after(activeFrom1)))
        {
            return false;
        }
        else if (null != activeTo1 && null == activeTo2 && (activeFrom2.after(activeTo1)))
        {
            return false;
        }
        else if (null == activeTo1 && null != activeTo2 && (activeFrom1.after(activeTo2)))
        {
            return false;
        }
        return true;
    }

    public static boolean isActualNotNull(Date beginDate, Date endDate, Date curDate)
    {
        return isSameDay(curDate, beginDate) || curDate.after(beginDate) && curDate.before(endDate);
    }

    public static boolean isActual(Date beginDate, Date endDate, Date curDate)
    {
        return isSameDay(curDate, beginDate) ||
               curDate.after(beginDate) && (endDate == null || curDate.before(endDate) || isSameDay(curDate, endDate));
    }

    /**
     * Returns current system time, as {@link java.sql.Timestamp Timestamp}.
     *
     * @return
     */
    public static java.sql.Timestamp currentTimestamp()
    {
        return new java.sql.Timestamp(System.currentTimeMillis());
    }

    /**
     * Returns current system time, as {@link java.sql.Date Date}.
     *
     * @return
     */
    public static java.sql.Date currentDate()
    {
        return new java.sql.Date(System.currentTimeMillis());
    }

    public static <T extends Date> T max(T a, T b)
    {
        return a == null ? b : (b == null ? a : (a.before(b) ? b : a));
    }
}
