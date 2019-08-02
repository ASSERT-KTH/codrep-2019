package com.developmentontheedge.be5.server.operations.extenders;

import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.base.BeModelCollection;
import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.operation.model.OperationResult;
import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.be5.server.operations.support.OperationExtenderSupport;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * OperationExtender suitable to check whether records supplied for the operation appear in the
 * special view. First "AllowedRecordsFor"+operation name is checked (for example, "AllowedRecordsForEdit"),
 * then simply "AllowedRecords". Thus you can make common view for all operations and then create special view
 * for some specific operations which should be filtered in different way.
 * If some of records absent in the given view, operation invocation will be stopped.
 *
 * @author lan
 */
public class CheckRecordsExtender extends OperationExtenderSupport
{
    private static final Logger log = Logger.getLogger(CheckRecordsExtender.class.getName());
    public static final String ALLOWED_RECORDS_VIEW = "AllowedRecords";
    public static final String ALLOWED_RECORDS_VIEW_PREFIX = "AllowedRecordsFor";

    private String message;

    @Override
    public Object postGetParameters(Operation op, Object parameters, Map<String, Object> presetValues) throws Exception
    {
        boolean skip = skip(op);
        if (skip)
        {
            op.setResult(OperationResult.error(message));
        }
        return parameters;
    }

    @Override
    public boolean skipInvoke(Operation op, Object parameters)
    {
        boolean skip = skip(op);
        if (skip)
        {
            op.setResult(OperationResult.error(message));
        }
        return skip;
    }

    private boolean skip(Operation op)
    {
        Entity entity = op.getInfo().getEntity();
        BeModelCollection<Query> entityQueries = entity.getQueries();
        Query query;
        query = entityQueries.get(ALLOWED_RECORDS_VIEW_PREFIX + op.getInfo().getName());
        if (query == null)
        {
            query = entityQueries.get(ALLOWED_RECORDS_VIEW);
        }
        if (query == 
null)
        {
            message = "Checked query not found for entity: " + op.getInfo().getEntityName();
            return true;
        }

        try
        {
            List<Object> records = Arrays.asList(op.getContext().getRecords());
            Set<Object> disabledRecords = new HashSet<>(records);

            List<QRec> dps = queries.readAsRecordsFromQuery(query,
                    Collections.singletonMap(op.getInfo().getPrimaryKey(), records));

            for (QRec row : dps)
            {
                disabledRecords.remove(row.getValue("ID"));
            }
            if (disabledRecords.size() > 0)
            {
                if (userInfoProvider.isAdmin())
                {
                    message = "Cannot execute operation " + op.getInfo().getName() + ": the following records are not found or not accessible: " + disabledRecords;
                }
                else
                {
                    message = userAwareMeta.getLocalizedExceptionMessage("Access to these records is denied.");
                }
                return true;
            }
        }
        catch (Throwable e)
        {
            message = "Cannot execute operation " + op.getInfo().getName() + ": " + e.getMessage();
            log.log(Level.SEVERE, message, e);
            return true;
        }

        return false;
    }
}
