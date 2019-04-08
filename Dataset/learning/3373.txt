package com.developmentontheedge.be5.server.services;

import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.util.FilterUtil;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.impl.utils.QueryUtils;
import com.developmentontheedge.be5.query.services.QueriesService;
import com.developmentontheedge.be5.server.model.DocumentPlugin;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.sql.model.AstBeParameterTag;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class FilterInfoPlugin implements DocumentPlugin
{
    private final QueriesService queries;
    private final Meta meta;
    private final UserAwareMeta userAwareMeta;
    protected static final String DOCUMENT_FILTER_INFO_PLUGIN = "filterInfo";

    @Inject
    public FilterInfoPlugin(QueriesService queries, Meta meta, UserAwareMeta userAwareMeta,
                            DocumentGenerator documentGenerator)
    {
        this.queries = queries;
        this.meta = meta;
        this.userAwareMeta = userAwareMeta;
        documentGenerator.addDocumentPlugin(DOCUMENT_FILTER_INFO_PLUGIN, this);
    }

    @Override
    public ResourceData addData(Query query, Map<String, Object> parameters)
    {
        FilterInfo filterInfo = new FilterInfo(getOperationParamsInfo(query, parameters));
        return new ResourceData(DOCUMENT_FILTER_INFO_PLUGIN, filterInfo, null);
    }

    protected List<FilterItem> getOperationParamsInfo(Query query, Map<String, Object> parameters)
    {
        Map<String, Object> params = FilterUtil.getOperationParamsWithoutFilter(parameters);
        List<FilterItem> result = new ArrayList<>();
        String mainEntityName = query.getEntity().getName();
        if (params.containsKey("entity") && params.containsKey("entityID"))
        {
            String entity = (String) params.remove("entity");
            String entityID = (String) params.remove("entityID");
            ColumnDef column = meta.getColumn(entity, meta.getEntity(entity).getPrimaryKey());

            String[][] tags = queries.getTagsFromSelectionView(column.getTableFrom(),
                    Collections.singletonMap(column.getName(), entityID));
            String entityTitle = userAwareMeta.getLocalizedEntityTitle(column.getTableFrom());
            if (tags.length > 0) result.add(new FilterItem(entityTitle, tags[0][1]));
        }
        params.forEach((k, v) -> {
            ColumnDef column = meta.getColumn(mainEntityName, k);
            if (column != null)
            {
                result.add(getValueTitle(column, mainEntityName, k, v));
                return;
            }

            if (query.getType() != QueryType.GROOVY && query.getType() != QueryType.JAVA)
            {
                AstStart ast = SqlQuery.parse(query.getFinalQuery());
                Optional<AstBeParameterTag> usedParam = ast.tree()
                        .select(AstBeParameterTag.class)
                        .filter(x -> x.getName().equals(k))
                        .findFirst();

                if (usedParam.isPresent())
                {
                    ColumnDef column2 = QueryUtils.getColumnDef(ast, usedParam.get(), mainEntityName, meta);
                    if (column2 != null)
                    {
                        result.add(getValueTitle(column2, mainEntityName, k, v));
                        return;
                    }
                }
            }

            String valueTitle = userAwareMeta.getColumnTitle(mainEntityName, query.getName(), v + "");
            result.add(new FilterItem(k, valueTitle));
        });
        return result;
    }

    protected FilterItem getValueTitle(ColumnDef column, String mainEntityName, String k, Object v)
    {
        String columnTitle = userAwareMeta.getColumnTitle(column.getTableFrom(), k);
        if (meta.getEntity(column.getTableFrom()).getPrimaryKey().equals(column.getName()))
        {
            String[][] tags = queries.getTagsFromSelectionView(column.getTableFrom(),
                    Collections.singletonMap(meta.getEntity(column.getTableFrom()).getPrimaryKey(), v));
            String idColumnTitle = mainEntityName.equalsIgnoreCase(column.getTableFrom()) ? null : columnTitle;
            if (tags.length > 0) return new FilterItem(idColumnTitle, tags[0][1]);
        }
        if (column.getTableTo() != null)
        {
            String[][] tags = queries.getTagsFromSelectionView(column. getTableTo(),
                    Collections.singletonMap(meta.getEntity(column.getTableTo()).getPrimaryKey(), v));
            if (tags.length > 0) return new FilterItem(columnTitle, tags[0][1]);
        }
        return new FilterItem(columnTitle, v + "");
    }

    public static class FilterItem
    {
        private String key;
        private String value;

        public FilterItem(String key, String value)
        {
            this.key = key;
            this.value = value;
        }

        public String getKey()
        {
            return key;
        }

        public String getValue()
        {
            return value;
        }
    }

    public static class FilterInfo
    {
        private List<FilterItem> operationParamsInfo;

        public FilterInfo(List<FilterItem> operationParamsInfo)
        {
            this.operationParamsInfo = operationParamsInfo;
        }

        public List<FilterItem> getOperationParamsInfo()
        {
            return operationParamsInfo;
        }
    }

}
