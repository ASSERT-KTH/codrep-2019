package com.developmentontheedge.sql.format.dbms;


public enum DateFormat
{
    FORMAT_DATE("'YYYY-MM-DD'", "'%Y-%m-%d'"),
    FORMAT_DATETIME("'YYYY-MM-DD HH24:MI:SS'", "'%Y-%m-%d %H:%i:%S'"),
    FORMAT_DATE_RUS("'DD.MM.YYYY'", "'%d.%m.%Y'"),
    FORMAT_DATE_RUS_SHORT("'DD.MM.YY'", "'%d.%m.%y'"),
    FORMAT_MONTHYEAR("'month YYYY'", "'%M %Y'"),
    FORMAT_FMDAYMONTH("'FMDD.MM'", "'%e.%c'"),
    FORMAT_DAYMONTH("'DD.MM'", "'%d.%m'"),
    FORMAT_HOURMINUTE("'HH24:MI'", "'%H:%i'"),
    FORMAT_YYYYMMDD("'YYYYMMDD'", "'%Y%m%d'"),
    FORMAT_YYYYMM("'YYYYMM'", "'%Y%m'"),
    FORMAT_DAY_OF_WEEK("'D'", "'%w'"),
    YEAR("'YYYY'", "'%Y'"),
    MONTH("'MM'", "'%m'"),
    DAY("'DD'", "'%d'");

    private final String formatMySQL;
    private final String formatOther;

    DateFormat(String formatOther, String formatMySQL)
    {
        this.formatMySQL = formatMySQL;
        this.formatOther = formatOther;
    }

    public static DateFormat byFunction(String name)
    {
        for (DateFormat f : values())
        {
            if (name.equalsIgnoreCase(f.name()))
                return f;
        }
        return null;
    }

    public String getFormatMySQL()
    {
        return formatMySQL;
    }

    public String getFormatOther()
    {
        return formatOther;
    }

    public static DateFormat byFormatString(String 	format)
    {
        for (DateFormat f : values())
        {
            if (f.formatMySQL.equals(format) || f.formatOther.equalsIgnoreCase(format))
                return f;
        }
        return null;
    }
}