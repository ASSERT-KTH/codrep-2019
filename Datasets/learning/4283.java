package com.developmentontheedge.be5.server.services.impl;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.util.FilterUtil;
import com.developmentontheedge.be5.base.util.HashUrl;
import com.developmentontheedge.be5.base.util.LayoutUtils;
import com.developmentontheedge.be5.database.Transactional;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.model.ColumnModel;
import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.query.services.TableModelService;
import com.developmentontheedge.be5.server.model.DocumentPlugin;
import com.developmentontheedge.be5.server.model.TablePresentation;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.server.model.table.InitialRow;
import com.developmentontheedge.be5.server.model.table.InitialRowsBuilder;
import com.developmentontheedge.be5.server.model.table.MoreRows;
import com.developmentontheedge.be5.server.model.table.MoreRowsBuilder;
import com.developmentontheedge.be5.server.services.DocumentGenerator;
import com.developmentontheedge.be5.server.services.events.LogBe5Event;
import com.developmentontheedge.be5.web.Session;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.base.FrontendConstants.SEARCH_PARAM;
import static com.developmentontheedge.be5.base.FrontendConstants.SEARCH_PRESETS_PARAM;
import static com.developmentontheedge.be5.base.FrontendConstants.TABLE_ACTION;
import static com.developmentontheedge.be5.base.FrontendConstants.TABLE_MORE_ACTION;
import static com.developmentontheedge.be5.query.TableConstants.CLEAN_NAV;
import static com.developmentontheedge.be5.query.TableConstants.LIMIT;
import static com.developmentontheedge.be5.query.TableConstants.OFFSET;
import static com.developmentontheedge.be5.query.TableConstants.ORDER_COLUMN;
import static com.developmentontheedge.be5.query.TableConstants.ORDER_DIR;
import static com.developmentontheedge.be5.server.RestApiConstants.SELF_LINK;


public class DocumentGeneratorImpl implements DocumentGenerator
{
    private static final Logger log = Logger.getLogger(DocumentGeneratorImpl.class.getName());

    private static final String QUERY_POSITIONS = "QUERY_POSITIONS";
    private static final String QUERY_FILTER = "QUERY_FILTER";
    private static final List<String> positionsParamNames = Arrays.asList(ORDER_COLUMN, ORDER_DIR, OFFSET, LIMIT);

    private final UserAwareMeta userAwareMeta;
    private final TableModelService tableModelService;
    private final Provider<Session> session;

    private final Map<String, DocumentPlugin> documentPlugins = new HashMap<>();

    @Inject
    public DocumentGeneratorImpl(UserAwareMeta userAwareMeta, TableModelService tableModelService,
                                 Provider<Session> session)
    {
        this.userAwareMeta = userAwareMeta;
        this.tableModelService = tableModelService;
        this.session = session;
    }

    @Override
    public JsonApiModel getDocument(String entityName, String 
queryName, Map<String, Object> parameters)
    {
        Query query = userAwareMeta.getQuery(entityName, queryName);
        return getDocument(query, parameters);
    }

    @Override
    @LogBe5Event
    @Transactional
    public JsonApiModel getDocument(Query query, Map<String, Object> parameters)
    {
        Map<String, Object> params = processQueryParams(query, parameters);
        TablePresentation data = getTablePresentation(query, params);
        HashUrl url = new HashUrl(TABLE_ACTION, query.getEntity().getName(), query.getName())
                .named(FilterUtil.getOperationParamsWithoutFilter(params));

        List<ResourceData> included = new ArrayList<>();
        try
        {
            documentPlugins.forEach((k, v) -> {
                ResourceData resourceData = v.addData(query, params);
                if (resourceData != null) included.add(resourceData);
            });
        }
        catch (RuntimeException e)
        {
            throw Be5Exception.internalInQuery(query, e);
        }

        return JsonApiModel.data(
                new ResourceData(TABLE_ACTION, data, Collections.singletonMap(SELF_LINK, url.toString())),
                included.toArray(new ResourceData[0]),
                null
        );
    }

    @Override
    public TablePresentation getTablePresentation(Query query, Map<String, Object> parameters)
    {
        return getTablePresentation(query, parameters, tableModelService.getTableModel(query, parameters));
    }

    private TablePresentation getTablePresentation(Query query, Map<String, Object> parameters, TableModel tableModel)
    {
        List<ColumnModel> columns = tableModel.getColumns();
        List<InitialRow> rows = new InitialRowsBuilder(tableModel).build();
        Long totalNumberOfRows = tableModel.getTotalNumberOfRows();

        String entityName = query.getEntity().getName();
        String queryName = query.getName();
        String localizedEntityTitle = userAwareMeta.getLocalizedEntityTitle(query.getEntity());
        String localizedQueryTitle = userAwareMeta.getLocalizedQueryTitle(entityName, queryName);
        String title = localizedEntityTitle + ": " + localizedQueryTitle;

        return new TablePresentation(
                title, entityName, queryName, tableModel.isSelectable(),
                columns, rows,
                tableModel.orderColumn, tableModel.orderDir, tableModel.offset, tableModel.limit,
                parameters, totalNumberOfRows, tableModel.isHasAggregate(),
                LayoutUtils.getLayoutObject(query));
    }

    @Override
    public JsonApiModel getNewTableRows(String entityName, String queryName, Map<String, Object> parameters)
    {
        Query query = userAwareMeta.getQuery(entityName, queryName);
        return getNewTableRows(query, parameters);
    }

    @LogBe5Event
    @Transactional
    JsonApiModel getNewTableRows(Query query, Map<String, Object> parameters)
    {
        String url = new HashUrl(TABLE_ACTION, query.getEntity().getName(), query.getName())
                .named(FilterUtil.getOperationParamsWithoutFilter(parameters)).toString();
        Map<String, String> links = Collections.singletonMap(SELF_LINK, url);


        Map<String, Object> params = processQueryParams(query, parameters);
        TableModel tableModel = tableModelService.getTableModel(query, params);

        return JsonApiModel.data(new ResourceData(TABLE_MORE_ACTION, new MoreRows(
                tableModel.getTotalNumberOfRows().intValue(),
                tableModel.getTotalNumberOfRows().intValue(),
                new MoreRowsBuilder(tableModel).build()
        ), links), null);
    }

    private Map<String, Object> processQueryParams(Query query, Map<String, Object> parameters)
    {
        Map<String, Object> params = new LinkedHashMap<>(parameters);
        if (parameters.containsKey(CLEAN_NAV))
        {
            params.remove(CLEAN_NAV);
            String queryKey = getQueryKey(query, parameters);
            getUserQueriesPositions().remove(queryKey);
            getUserQueriesFilterParams().remove(queryKey);
            return params;
        }
        else
        {
            addSavedPositionIfNotExist(query, params);
            addSavedFilterParamsIfNotExist(query, params);
            return params;
        }
    }

    @Override
    public void addDocumentPlugin(String name, DocumentPlugin documentPlugin)
    {
        documentPlugins.put(name, documentPlugin);
    }

    private Map<String, Map<String, Object>> getUserQueriesPositions()
    {
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> filterParams =
                (Map<String, Map<String, Object>>) session.get().get(QUERY_POSITIONS);
        if (filterParams == null)
        {
            filterParams = new HashMap<>();
            session.get().set(QUERY_POSITIONS, filterParams);
        }
        return filterParams;
    }

    private Map<String, Map<String, Object>> getUserQueriesFilterParams()
    {
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> filterParams =
                (Map<String, Map<String, Object>>) session.get().get(QUERY_FILTER);
        if (filterParams == null)
        {
            filterParams = new HashMap<>();
            session.get().set(QUERY_FILTER, filterParams);
        }
        return filterParams;
    }

    @Override
    public void clearSavedPosition(Query query, Map<String, Object> parameters)
    {
        getUserQueriesPositions().remove(getQueryKey(query, parameters));
    }

    private void addSavedPositionIfNotExist(Query query, Map<String, Object> parameters)
    {
        Map<String, Map<String, Object>> positions = getUserQueriesPositions();
        String queryKey = getQueryKey(query, parameters);
        if (parameters.containsKey(ORDER_COLUMN) || parameters.containsKey(ORDER_DIR) ||
                parameters.containsKey(OFFSET) || parameters.containsKey(LIMIT))
        {
            Map<String, Object> newPos = new HashMap<>();
            positionsParamNames.forEach(name -> {
                if (parameters.containsKey(name)) newPos.put(name, parameters.get(name));
            });
            positions.put(queryKey, newPos);
        }
        else
        {
            Map<String, Object> savedPosition = positions.get(queryKey);
            if (savedPosition != null) parameters.putAll(savedPosition);
        }
    }

    private void addSavedFilterParamsIfNotExist(Query query, Map<String, Object> parameters)
    {
        Map<String, Map<String, Object>> filterParams = getUserQueriesFilterParams();
        String queryKey = getQueryKey(query, parameters);
        if (parameters.containsKey(SEARCH_PARAM))
        {
            filterParams.put(queryKey, FilterUtil.getFilterParams(parameters));
        }
        else
        {
            if (filterParams.containsKey(queryKey))
            {
                String searchPresetParam = FilterUtil.getSearchPresetParam(parameters);
                if (searchPresetParam != null) parameters.put(SEARCH_PRESETS_PARAM, searchPresetParam);
                parameters.putAll(filterParams.get(queryKey));
                parameters.put(SEARCH_PARAM, "true");
            }
        }
    }

    private String getQueryKey(Query query, Map<String, Object> parameters)
    {
        return new HashUrl(query.getEntity().getName(), query.getName()).named(
                FilterUtil.getOperationParamsWithoutFilter(parameters)).toString();
    }
}
