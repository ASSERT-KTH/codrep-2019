package com.developmentontheedge.be5.server.helpers;

import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.server.model.Action;
import com.developmentontheedge.be5.server.util.ActionUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class MenuHelper
{
    private static final String INSERT_OPERATION = "Insert";

    private final UserAwareMeta userAwareMeta;
    private final Meta meta;
    private final UserInfoProvider userInfoProvider;

    @Inject
    public MenuHelper(UserAwareMeta userAwareMeta, Meta meta, UserInfoProvider userInfoProvider)
    {
        this.userAwareMeta = userAwareMeta;
        this.meta = meta;
        this.userInfoProvider = userInfoProvider;
    }

    public Action getDefaultAction()
    {
        List<RootNode> entities = collectEntities(false, false, EntityType.TABLE);

        for (RootNode rootNode : entities)
        {
            if (rootNode.action != null)
            {
                if (rootNode.isDefault) return rootNode.action;
            }
            else if (rootNode.children != null)
            {
                for (QueryNode node : rootNode.children)
                {
                    if (node.isDefault) return node.action;
                }
            }
        }

        entities = collectEntities(false, true, EntityType.TABLE);

        for (RootNode rootNode : entities)
        {
            if (rootNode.action != null)
            {
                return rootNode.action;
            }
            else if (rootNode.children != null)
            {
                for (QueryNode node : rootNode.children)
                {
                    return node.action;
                }
            }
        }

        return null;
    }

    /**
     * Adds all permitted queries to the root array.
     */
    public List<RootNode> collectEntities(boolean withIds, EntityType entityType)
    {
        return collectEntities(withIds, true, entityType);
    }

    private List<RootNode> collectEntities(boolean withIds, boolean withoutInvisible, EntityType entityType)
    {
        UserInfo userInfo = userInfoProvider.get();
        List<String> roles = userInfo.getCurrentRoles();
        String language = userInfo.getLanguage();
        List<RootNode> out = new ArrayList<>();

        for (Entity entity : meta.getOrderedEntities(entityType, language))
        {
            collectEntityContent(entity, language, roles, withIds, withoutInvisible, out);
        }

        return out;
    }

    private void collectEntityContent(Entity entity, String language, List<String> roles, boolean withIds,
                                      boolean withoutInvisible, List<RootNode> out)
    {
        List<Query> queries = meta.getQueries(entity, roles);
        if (withoutInvisible)
        {
            queries.removeIf(Query::isInvisible);
        }

        if (queries.isEmpty())
        {
            return;
        }

        String title = meta.getTitle(entity, language);
        List<OperationNode> operations = generateEntityOperations(entity, roles, withIds);

        if (operations.isEmpty())
        {
            operations = null;
        }

        if (queries.size() == 1)
        {
            // Query in the root, contains an action.
            Id id = null;
            Action action = ActionUtils.toAction(queries.get(0));
            boolean isDefault = queries.get(0).isDefaultView();

            if (withIds)
            {
                String queryTitle = getTitleOfRootQuery(queries, title, language, meta);
                id = new Id(entity.getName(), queryTitle);
            }

            out.add(RootNode.action(id, title, isDefault, action, operations));
        }
        else
        {
            // No query in the root, just inner queries.
            List<QueryNode> children = generateEntityQueries(queries, language, meta, withIds);
            Id id = new Id(entity.getName(), null);
            out.add(RootNode.container(id, title, children, operations));
        }
    }

    private List<QueryNode> generateEntityQueries(List<Query> permittedQueries, String language, Meta meta, boolean withIds)
    {
        List<OrderedQuery> queries = new ArrayList<>();

        for (Query permittedQuery : permittedQueries)
        {
            queries.add(new OrderedQuery(permittedQuery, meta.getTitle(permittedQuery, language)));
        }

        Collections.sort(queries);

        List<QueryNode> children = new ArrayList<>();

        for (OrderedQuery query : queries)
        {
            Query permittedQuery = query.query;
            Id id = null;

            if (withIds)
            {
                id = new Id(permittedQuery.getEntity().getName(), permittedQuery.getName());
            }

            children.add(new QueryNode(id, query.title, ActionUtils.toAction(permittedQuery), permittedQuery.isDefaultView()));
        }

        return children;
    }

    private List<OperationNode> generateEntityOperations(Entity entity, List<String> roles, boolean withIds)
    {
        List<OperationNode> operations = new ArrayList<>();
        Query allRecords = entity.getQueries().get(DatabaseConstants.ALL_RECORDS_VIEW);
        String insertOperationName = INSERT_OPERATION;

        if (allRecords != null && allRecords.getOperationNames().getFinalValues().contains(insertOperationName))
        {
            Operation insertOperation = entity.getOperations().get(insertOperationName);
            if (insertOperation != null && meta.isAvailableFor(insertOperation, roles))
            {
                String title = userAwareMeta.getLocalizedOperationTitle(entity.getName(), insertOperationName);
                Action action = ActionUtils.toAction(DatabaseConstants.ALL_RECORDS_VIEW, insertOperation);
                OperationId id = withIds ? new OperationId(entity.getName(), insertOperationName) : null;
                OperationNode operation = new OperationNode(id, title, action);
                operations.add(operation);
            }
        }

        return operations;
    }

//    /**
//     * If the entity contains only one query, that's named "All records" or as the entity itself.
//     */
//    private boolean canBeMovedToRoot(List<Query> queries, String entityTitle, String language, Meta meta)
//    {
//        return getTitleOfRootQuery(queries, entityTitle, language, meta) != null;
//    }

    private String getTitleOfRootQuery(List<Query> queries, String entityTitle, String language, Meta meta)
    {
        if (queries.size() != 1)
            return null;

        Query query = queries.get(0);

        if (query.getName().equals(DatabaseConstants.ALL_RECORDS_VIEW))
            return DatabaseConstants.ALL_RECORDS_VIEW;

        if (meta.getTitle(query, language).equals(entityTitle))
            return entityTitle;

        return null;
    }

    public static class RootNode
    {

        private final Id id;
        private final String title;
        private final boolean isDefault;
        private final Action action;
        private final List<QueryNode> children;
        private final List<OperationNode> operations;

        static 
RootNode action(Id id, String title, boolean isDefault, Action action, List<OperationNode> operations)
        {
            return new RootNode(id, title, isDefault, action, null, operations);
        }

        static RootNode container(Id id, String title, List<QueryNode> children, List<OperationNode> operations)
        {
            return new RootNode(id, title, false, null, children, operations);
        }

        private RootNode(Id id, String title, boolean isDefault, Action action, List<QueryNode> children, List<OperationNode> operations)
        {
            this.id = id;
            this.title = title;
            this.isDefault = isDefault;
            this.action = action;
            this.children = children;
            this.operations = operations;
        }

        public Id getId()
        {
            return id;
        }

        public String getTitle()
        {
            return title;
        }

        //@JsonbProperty("default")
        public boolean isDefault()
        {
            return isDefault;
        }

        public Action getAction()
        {
            return action;
        }

        public List<QueryNode> getChildren()
        {
            return children;
        }

        public List<OperationNode> getOperations()
        {
            return operations;
        }
    }

    public static class Id
    {

        final String entity;
        final String query;

        public Id(String entity, String query)
        {
            this.entity = entity;
            this.query = query;
        }

        public String getEntity()
        {
            return entity;
        }

        public String getQuery()
        {
            return query;
        }
    }

    public static class QueryNode
    {

        private final Id id;
        private final String title;
        private final Action action;
        private final boolean isDefault;

        public QueryNode(Id id, String title, Action action, boolean isDefault)
        {
            this.id = id;
            this.title = title;
            this.action = action;
            this.isDefault = isDefault;
        }

        public Id getId()
        {
            return id;
        }

        public String getTitle()
        {
            return title;
        }

        public Action getAction()
        {
            return action;
        }

        //@JsonbProperty("default")
        public boolean isDefault()
        {
            return isDefault;
        }
    }

    public static class OperationNode
    {

        final OperationId id;
        final String title;
        final Action action;

        OperationNode(OperationId id, String title, Action action)
        {
            this.id = id;
            this.title = title;
            this.action = action;
        }

        public OperationId getId()
        {
            return id;
        }

        public String getTitle()
        {
            return title;
        }

        public Action getAction()
        {
            return action;
        }
    }

    public static class OperationId
    {

        final String entity;
        final String operation;

        OperationId(String entity, String operation)
        {
            this.entity = entity;
            this.operation = operation;
        }

        public String getEntity()
        {
            return entity;
        }

        public String getOperation()
        {
            return operation;
        }
    }

    /**
     * Used to sort queries.
     *
     * @author asko
     */
    public static class OrderedQuery implements Comparable<OrderedQuery>
    {

        final Query query;
        final String title;

        public OrderedQuery(Query query, String title)
        {
            Objects.requireNonNull(query);
            Objects.requireNonNull(title);
            this.query = query;
            this.title = title;
        }

        @Override
        public int compareTo(OrderedQuery other)
        {
            Objects.requireNonNull(other);
            return title.compareTo(other.title);
        }

        public Query getQuery()
        {
            return query;
        }

        public String getTitle()
        {
            return title;
        }
    }

}
