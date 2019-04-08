package com.developmentontheedge.be5.server.operations.support;

import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.operation.services.OperationsFactory;
import com.developmentontheedge.be5.operation.services.validation.Validator;
import com.developmentontheedge.be5.operation.support.BaseOperationExtenderSupport ;
import com.developmentontheedge.be5.query.services.QueriesService;
import com.developmentontheedge.be5.server.helpers.DpsHelper;

import javax.inject.Inject;


public abstract class OperationExtenderSupport extends BaseOperationExtenderSupport
{
    @Inject
    public Meta meta;
    @Inject
    public UserAwareMeta userAwareMeta;
    @Inject
    public DbService db;
    @Inject
    public DatabaseModel database;
    @Inject
    public DpsHelper dpsHelper;
    @Inject
    public Validator validator;
    @Inject
    public OperationsFactory operations;
    @Inject
    public QueriesService queries;
    @Inject
    public UserInfoProvider userInfoProvider;
}
