package com.developmentontheedge.be5.database.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

public class SqlUtils
{
    public static <T> T getSqlValue(Class<T> clazz, ResultSet rs, int idx)
    {
        try
        {
            Object object = rs.getObject(idx);
            if (object == null)
            {
                return null;
            }

            if (clazz == Double.class && object.getClass() == BigDecimal.class)
            {
                return (T) (Double) ((BigDecimal) object).doubleValue();
            }

            if (clazz == Short.class && object.getClass() == Integer.class)
            {
                return (T) (Short) ((Integer) object).shortValue();
            }

            if (clazz == Integer.class && object.getClass() == Long.class)
            {
                return (T) (Integer) ((Long) object).intValue();
            }

            if (clazz == Long.class)
            {
                return (T) longFromDbObject(object);
            }

            if (clazz == String.class)
            {
                return (T) stringFromDbObject(object);
            }

            return clazz.cast(object);
        }
        catch (Throwable e)
        {
            String name = "";
            try
            {
                name = rs.getMetaData().getColumnName(idx);
            }
            catch (SQLException ignore)
            {

            }

            throw new RuntimeException("for column: " + name, e);
        }
    }

    public static Long longFromDbObject(Object number)
    {
        if (number == null)
        {
            return null;
        }
        else if (number.getClass() == Long.class)
        {
            return (Long) number;
        }
        else if (number.getClass() == Integer.class)
        {
            return ((Integer) number).longValue();
        }
        else if (number.getClass() == BigInteger.class)
        {
            return ((BigInteger) number).longValue();
        }
        else
        {
            return Long.parseLong(number.toString());
        }
    }

    public static String stringFromDbObject(Object value)
    {
        try
        {
            if (value == null)
            {
                return null;
            }
            else if (value.getClass() == String.class)
            {
                return (String) value;
            }
            else if (value.getClass() == byte[].class)
            {
                return new String((byte[]) value, StandardCharsets.UTF_8);
            }
            else if (value instanceof Clob)
            {
                Clob clob = (Clob) value;
                return clob.getSubString(1, (int) clob.length());
            }
            return (String) value;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getTypeClass(int columnType)
    {
        switch (columnType)
        {
            case Types.BIGINT:
                return Long.class;
            case Types.INTEGER:
                return Integer.class;
            case Types.SMALLINT:
                return Short.class;
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.DECIMAL:
            case Types.REAL:
            case Types.NUMERIC:
                return Double.class;
            case Types	.BOOLEAN:
                return Boolean.class;
            case Types.DATE:
                return Date.class;
            case Types.TIME:
                return Time.class;
            case Types.TIMESTAMP:
                return Timestamp.class;
            case Types.CLOB:
                return Clob.class;
            case Types.BLOB:
                return Blob.class;
            case Types.BINARY:
                return byte[].class;
            default:
                return String.class;
        }
    }

    public static Class<?> getSimpleStringTypeClass(int columnType)
    {
        switch (columnType)
        {
            case Types.CLOB:
            case Types.BLOB:
            case Types.BINARY:
                return String.class;
            default:
                return getTypeClass(columnType);
        }
    }
}
