package com.developmentontheedge.sql.format.dbms;

public class Context
{
    protected Dbms dbms;
    protected DbmsTransformer dbmsTransformer;

    public Context(Dbms dbms)
    {
        this.dbms = dbms;

        switch (dbms)
        {
            case DB2:
                dbmsTransformer = new DB2Transformer();
                break;
            case MYSQL:
                dbmsTransformer = new MySqlTransformer();
                break;
            case ORACLE:
                dbmsTransformer = new OracleTransformer();
                break;
            case POSTGRESQL:
                dbmsTransformer = new PostgreSqlTransformer();
                break;
            case SQLSERVER:
                dbmsTransformer = new SqlServerTransformer();
                break;
            case H2:
                dbmsTransformer = new H2SqlTransformer();
                break;
        }
    }

    public Dbms getDbms()
    {
        return dbms;
    }

    public DbmsTransformer getDbmsTransformer()
    {
        return dbmsTransformer;
    	}
}
