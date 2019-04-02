package com.developmentontheedge.be5.database;

import com.developmentontheedge.be5.database.impl.ConnectionServiceImpl;
import com.developmentontheedge.be5.database.impl.DataSourceServiceImpl;
import com.developmentontheedge.be5.database.impl.DbServiceImpl;
import com.developmentontheedge.be5.database.impl.SqlHelper;
import com.developmentontheedge.be5.database.impl.TransactionInterceptor;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;


public class DatabaseModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind (DataSourceService.class).to(DataSourceServiceImpl.class).in(Scopes.SINGLETON);
        bind(ConnectionService.class).to(ConnectionServiceImpl.class).in(Scopes.SINGLETON);
        bind(DbService.class).to(DbServiceImpl.class).in(Scopes.SINGLETON);
        bind(SqlHelper.class).in(Scopes.SINGLETON);

        TransactionInterceptor transactionInterceptor = new TransactionInterceptor();
        requestInjection(transactionInterceptor);

        bindInterceptor(annotatedWith(Transactional.class), any(), transactionInterceptor);
        bindInterceptor(any(), annotatedWith(Transactional.class), transactionInterceptor);
    }
}
