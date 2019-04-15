/* $Id: MonthlyDate.java,v 1.24 2013/11/12 11:17:49 mixa Exp $ */

package com.developmentontheedge.be5.base.util;

import java.util.Calendar;

/**
 * Date specified to the month
 */
public class MonthlyDate extends java.sql.Date
{
    final Calendar calendar = Calendar.getInstance();

    public static final MonthlyDate MAX_VALUE = new MonthlyDate(2099, 12);
    public static final MonthlyDate MIN_VALUE = new MonthlyDate(1900, 1);

    public MonthlyDate()
    {
        super(System.currentTimeMillis());
        calendar.setTime(this);
    }

    // expects "YYYY[-/.]MM" or "YYYYMM"
    public MonthlyDate(String dateStr, String mask)
    {
        super(getTime(
                Integer.parseInt(dateStr.substring(0, 4)),
                mask.length() == 6 ? Integer.parseInt(dateStr.substring(4, 6)) : Integer.parseInt(dateStr.substring(5, 7))
                )
        );
        calendar.setTime(this);
    }

    public MonthlyDate(int year, int month)
    {
        super(getTime(year, month));
        calendar.setTime(this);
    }

    public MonthlyDate(java.util.Date date)
    {
        super(getTime(date));
        calendar.setTime(this);
    }

    public int getNumDays()
    {
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public int getCurrentDay()
    {
        calendar.setTime(this);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public int getCurrentMonth()
    {
        calendar.setTime(this);
        return calendar.get(Calendar.MONTH) + 1;
    }

    public int getCurrentYear()
    {
        calendar.setTime(this);
        return calendar.get(Calendar.YEAR);
    }

    private static long getTime(int year, int month)
    {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);

        //время сбрасывается в 0, для исключения проблем со сравнениями
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis();
    }

    private static long getTime(java.util.Date date)
    {
        if (date == null)
        {
            return MAX_VALUE.getTime();
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        //время сбрасывается в 0, для исключения проблем со сравнениями
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public String getDateStr(boolean quote)
    {
        String str = this.toString();
        return quote ? "'" + str + "'" : str;
    }

    public boolean lessOrEqual(java.sql.Date date)
    {
        return this.compareTo(date) <= 0;
    }

    public MonthlyDate getNextYear()
    {
        calendar.setTime(this);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.YEAR, 1);
        return new MonthlyDate(calendar.getTime());
    }

    public MonthlyDate increment()
    {
        return increment(1);
    }

    public MonthlyDate increment(int n)
    {
        calendar.setTime(this);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, n);
        return new MonthlyDate(calendar.getTime());
    }

    public MonthlyDate decrement()
    {
        return decrement(1);
    }

    public MonthlyDate decrement(int n)
    {
        calendar.setTime(this);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, -n);
        return new MonthlyDate(calendar.getTime());
    }

    public MonthlyDate getNextMonth()
    {
        return increment();
    }

    public MonthlyDate getPrevMonth()
    {
        return decrement();
    }

    public boolean lessThan(MonthlyDate MonthlyDate)
    {
        return this.compareTo(MonthlyDate) < 0;
    }

    public boolean lessThanOrEqual(MonthlyDate MonthlyDate)
    {
        return this.compareTo(MonthlyDate) <= 0;
    }

    public static MonthlyDate min(MonthlyDate a, MonthlyDate b)
    {
        return b.compareTo(a) < 0 ? b : a;
    }

    public static MonthlyDate max(MonthlyDate a, MonthlyDate b)
    {
        return a.compareTo(b) < 0 ? b : a;
    }

    public MonthlyDate getFirstDay()
    {
        calendar.setTime(this);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return new MonthlyDate(calendar.getTime());
    }

    public MonthlyDate getFirstDayOfYear()
    {
        calendar.setTime(this);
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        return new MonthlyDate(calendar.getTime());
    }

    public static MonthlyDate max(MonthlyDate a, MonthlyDate b, MonthlyDate c)
    {
        return max(a, MonthlyDate.max(b, c));
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MonthlyDate that = (MonthlyDate) o;

        return calendar != null ? calendar.equals(that.calendar) : that.calendar == null;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        
result = 31 * result + (calendar != null ? calendar.hashCode() : 0);
        return result;
    }
}
