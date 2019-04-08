package com.developmentontheedge.be5.server.operations;

import com.developmentontheedge.be5.base.util.Utils;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.TableReference;
import com.developmentontheedge.be5.operation.model.OperationResult;
import com.developmentontheedge.be5.operation.model.TransactionalOperation;
import com.developmentontheedge.be5.server.operations.support.OperationSupport;

import java.util.List;
import java.util.Map;

import static com.developmentontheedge.be5.metadata.model. EntityType.COLLECTION;
import static com.developmentontheedge.be5.metadata.model.EntityType.GENERIC_COLLECTION;


public class DeleteOperation extends OperationSupport implements TransactionalOperation
{
    protected StringBuilder out = new StringBuilder();

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        return null;
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        int updateCount = database.getEntity(getInfo().getEntityName()).remove(context.getRecords());

        out.append("" + updateCount + " " + ("records were deleted from") + " <i>" + getInfo().getEntityName() + "</i><br />");

        List<TableReference> collectionRefs = meta.getTableReferences(COLLECTION);

        for (TableReference reference : collectionRefs)
        {
            if (getInfo().getEntityName().equals(reference.getTableTo()) && getInfo().getEntity().getPrimaryKey().equalsIgnoreCase(reference.getColumnsTo()))
            {
                int updateCount1 = database.getEntity(reference.getTableFrom())
                        .removeWhereColumnIn(reference.getColumnsFrom(), context.getRecords());

                if (updateCount1 > 0)
                {
                    //todo localizedMessage
                    out.append("" + updateCount1 +
                            " " + ("records were deleted from the collection") + " <i>" + reference.getTableFrom() + "</i><br />");
                }
            }
        }

        if (!GENERIC_COLLECTION.equals(getInfo().getEntity().getType()))
        {
            List<TableReference> genericCollectionRefs = meta.getTableReferences(GENERIC_COLLECTION);

            for (TableReference reference : genericCollectionRefs)
            {
                if (reference.getColumnsTo() == null)
                {
                    int updateCount1 = database.getEntity(reference.getTableFrom())
                            .removeWhereColumnIn(reference.getColumnsFrom(),
                                    Utils.addPrefix(getInfo().getEntityName() + ".", context.getRecords()));

                    if (updateCount1 > 0)
                    {
                        out.append("" + updateCount1 +
                                " " + ("records were deleted from the generic collection") + " <i>" + reference.getTableFrom() + "</i><br />");
                    }
                }
            }
        }

        if (userInfo.getCurrentRoles().contains(RoleType.ROLE_SYSTEM_DEVELOPER))
        {
            setResult(OperationResult.finished(out.toString()));
        }
        else
        {
            setResult(OperationResult.finished());
        }
    }
}
