package com.developmentontheedge.be5.base.services;

import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Daemon;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityItem;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.RoleSet;
import com.developmentontheedge.be5.metadata.model.TableReference;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public interface Meta
{
    Project getProject();

    /**
     * ...sorted by localized title.
     */
    List<Entity> getOrderedEntities(String language);

    List<Entity> getOrderedEntities(EntityType entityType, String language);

    List<Entity> getEntities();

    List<Entity> getEntities(EntityType entityType);

    List<TableReference> getTableReferences(EntityType entityType);

//    Map<String, List<Entity>> getOrderedEntitiesByModules(String language);
//
//    Map<String, List<Entity>> getOrderedEntitiesByModules(EntityType entityType, String language);

    /**
     * Returns an entity with by its name.
     * Throws an exception if there's no such entity.
     */
    Entity getEntity(String entityName);

    Locale getLocale(Locale locale);

    List<String> getOperationNames(Entity entity);

    /**
     * Returns an operation by its name.
     * Throws an exception if there's no operation with this name.
     * Throws an exception if there's no query with this name or this query hasn't this operation.
     */
    Operation getOperation(String entityName, String queryName, String name);

    Operation getOperation(String entityName, String name);

    /**
     * Returns a list of all queries of the entity that a user with the given roles can run.
     * Returns an empty list if there's no such queries.
     */
    List<Query> getQueries(Entity entity, List<String> roles);

    /**
     * Checks if we can run an operation or a query having given roles.
     */
    boolean isAvailableFor(EntityItem entityItem, List<String> roles);

    /**
     * Returns a query.
     * Throws an exception if there's no such query.
     */
    Query getQuery(String entityName, String queryName);

    List<String> getQueryNames(Entity entity);

//    Optional<Entity> findEntity(String entityName);
//
//    Optional<Query> findQuery(String entityName, String queryName);
//
//    Optional<Query> findQuery(QueryLink link);

    Map<String, ColumnDef> getColumns(String entityName);

    Map<String, ColumnDef> getColumns(Entity entity);

    @Nullable
    ColumnDef getColumn(String entityName, String columnName);

    @Nullable
    ColumnDef getColumn(Entity entity, String columnName);

    default 
boolean columnExists(String entity, String column)
    {
        return getColumn(entity, column) == null;
    }

    default String getColumnDefaultValue(ColumnDef column)
    {
        if (column == null) return null;

        String defaultValue = column.getDefaultValue();
        if (defaultValue != null && defaultValue.startsWith("'") && defaultValue.endsWith("'"))
        {
            defaultValue = defaultValue.substring(1, defaultValue.length() - 1);
        }
        return defaultValue;
    }

    Class<?> getColumnType(ColumnDef columnDef);

    Class<?> getColumnType(Entity entity, String columnName);

    Class<?> getColumnType(String entityName, String columnName);
//
//    boolean isNumericColumn(String entityName, String columnName);
//
//    boolean isNumericColumn(Entity entity, String columnName);

    /**
     * Returns a localized title. Takes into consideration its display name.
     */
    String getTitle(Entity entity, String language);

    /**
     * Returns a localized title of a query. Takes into consideration its menu name.
     */
    String getTitle(Query query, String language);

    /**
     * Checks only the specified query, doesn't respolve redirects.
     */
    boolean isParametrizedTable(Query query);

    Set<String> getProjectRoles();

    List<Daemon> getDaemons();

    Query createQueryFromSql(String sql);

    boolean hasAccess(RoleSet roles, List<String> availableRoles);

    String getStaticPageContent(String language, String name);
}
