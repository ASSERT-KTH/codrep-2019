package com.developmentontheedge.sql.format.dbms;

public enum Dbms
{
    DB2("db2"),
    MYSQL("mysql"),
    ORACLE("oracle"),
    SQLSERVER("sqlserver"),
    POSTGRESQL("postgres"),
    H2("h2");

    private String name;

    Dbms(String name)
    {
        this.name = name;
    	}

    public String getName()
    {
        return name;
    }

}
