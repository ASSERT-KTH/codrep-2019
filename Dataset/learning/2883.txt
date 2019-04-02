package com.developmentontheedge.be5.base.services.impl;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.base.util.Utils;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Daemon;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityItem;
import com.developmentontheedge.be5.metadata.model.EntityLocalizations;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.LanguageLocalizations;
import com.developmentontheedge.be5.metadata.model.LocalizationElement;
import com.developmentontheedge.be5.metadata.model.Localizations;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.RoleSet;
import com.developmentontheedge.be5.metadata.model.SqlColumnType;
import com.developmentontheedge.be5.metadata.model.TableDef;
import com.developmentontheedge.be5.metadata.model.TableReference;
import com.developmentontheedge.be5.metadata.model.base.BeCaseInsensitiveCollection;
import com.developmentontheedge.be5.metadata.model.base.BeModelElement;
import com.developmentontheedge.be5.metadata.model.base.BeModelElementSupport;

import javax.inject.Inject;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class MetaImpl implements Meta
{
    /**
     * Predicates.
     */
    private static class Pr
    {

        private static final Predicate<LocalizationElement> TOPICS_CONTAIN_DISPLAY_NAME = topicsContain("displayName");
        private static final Predicate<LocalizationElement> TOPICS_CONTAIN_VIEW_NAME = Pr.topicsContain("viewName");

        private static Predicate<LocalizationElement> topicsContain(final String topic)
        {
            return l10n -> l10n.getTopics().contains(topic);
        }

        private static Predicate<LocalizationElement> keyIs(final String key)
        {
            return l10n -> l10n.getKey().equals(key);
        }

        private static Predicate<LocalizationElement> topicsContainDisplayName()
        {
            return TOPICS_CONTAIN_DISPLAY_NAME;
        }

        private static Predicate<LocalizationElement> topicsContainViewName()
        {
            return TOPICS_CONTAIN_VIEW_NAME;
        }

    } /* class Pr */

    private static final Pattern MENU_ITEM_PATTERN = Pattern.compile("<!--\\S+?-->");

    private final ProjectProvider projectProvider;

    @Inject
    public MetaImpl(ProjectProvider projectProvider)
    {
        this.projectProvider = projectProvider;
    }

    @Override
    public Entity getEntity(String entityName)
    {
        Entity entity = getProject().getEntity(entityName);
        if (entity == null)
        {
            throw Be5Exception.unknownEntity(entityName);
        }
        return entity;
    }

    @Override
    public boolean hasAccess(RoleSet roles, List<String> availableRoles)
    {
        Set<String> finalRoles = roles.getFinalRoles();
        for (String role : availableRoles)
        {
            if (role.equals(RoleType.ROLE_ADMINISTRATOR) || role.equals(RoleType.ROLE_SYSTEM_DEVELOPER)
                    || finalRoles.contains(role))
                return true;
        }
        return false;
    }

    @Override
    public List<Entity> getOrderedEntities(String language)
    {
        return getOrderedEntities(null, language);
    }

    @Override
    public List<Entity> getOrderedEntities(EntityType entityType, String language)
    {
        return getEntitiesStream()
                .filter(e -> entityType == null || e.getType() == entityType)
                .map(e -> new OrderedEntity(e, getTitle(e, language)))
                .sorted()
                .map(oe -> oe.entity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Entity> getEntities(EntityType entityType)
    {
        return getEntitiesStream()
                .filter(e -> e.getType() == entityType)
                .collect(Collectors.toList());
    }

    @Override
    public List<Entity> getEntities()
    {
        return getEntitiesStream().collect(Collectors.toList());
    }

    private Stream<Entity> getEntitiesStream()
    {
        return getProject().getEntityNames().stream()
                .map(name -> getProject().getEntity(name))
                .filter(BeModelElementSupport::isAvailable);
    }

    @Override
    public List<TableReference> getTableReferences(EntityType entityType)
    {
        return getEntities(entityType).stream()
                .flatMap(entity -> entity.getAllReferences().stream())
                .collect(Collectors.toList());
    }

//    @Override
//    public Map<String, List<Entity>> getOrderedEntitiesByModules(String language)
//    {
//        return getOrderedEntitiesByModules(null, language);
//    }
//
//    @Override
//    public Map<String, List<Entity>> getOrderedEntitiesByModules(EntityType entityType, String language)
//    {
//        HashMap<String, List<Entity>> result = new HashMap<>();
//
//        for (Module module : getProject().getModulesAndApplication())
//        {
//            List<OrderedEntity> entities = new ArrayList<>();
//            for (Entity entity : module.getEntities())
//            {
//                if (entityType == null || entity.getType() == entityType)
//                {
//                    entities.add(new OrderedEntity(entity, getTitle(entity, language)));
//                }
//            }
//            Collections.sort(entities);
//            result.put(module.getName(), entities.stream().map(e -> e.entity).collect(Collectors.toList()));
//        }
//
//        return result;
//    }

    @Override
    public String getTitle(Entity entity, String language)
    {
        String l10n = getLocalization(entity.getProject(), language, entity.getName(), Pr.topicsContainDisplayName());
        return !l10n.isEmpty() ? l10n : getDisplayName(entity);
    }

    private String getDisplayName(Entity entity)
    {
        String displayName = entity.getDisplayName();

        if (displayName != null && displayName.trim().length() > 0)
            return displayName;

        return entity.getName();
    }

    @Override
    public String getTitle(Query query, String language)
    {
        return removeHints(getBe3Title(query, language));
    }

    /**
     * Removes prefixes used to sort items.
     */
    private String removeHints(String title)
    {
        return MENU_ITEM_PATTERN.matcher(title).replaceAll("");
    }

    private String getBe3Title(Query query, String language)
    {
        Predicate<LocalizationElement> accept = Pr.topicsContainViewName().and(Pr.keyIs(query.getName()));
        String l10n = getLocalization(query.getProject(), language, query.getEntity().getName(), accept);
        return !l10n.isEmpty() ? l10n : getMenuName(query);
    }

    private String getLocalization(Project project, String language, String entity,
                                   Predicate<LocalizationElement> accept)
    {
        for (Module module : project.getModulesAndApplication())
        {
            Localizations localizations = module.getLocalizations();
            LanguageLocalizations languageLocalizations = localizations.get(language);

            if (languageLocalizations == null)
                continue;

            EntityLocalizations entityLocalizations = languageLocalizations.get(entity);

            if (entityLocalizations == null)
                continue;

            for (LocalizationElement element : entityLocalizations.elements())
                if (accept.test(element))
                    return element.getValue();
        }

        return "";
    }

    @Override
    public Locale getLocale(Locale locale)
    {
        List<String> languages = getProject().getApplication().getLocalizations().names().toList();

        if (locale == null || !languages.contains(locale.getLanguage()))
        {
            if (languages.size() > 0)
                return new Locale(languages.get(0));
            else
                return Locale.US;
        }
        else
        {
            return locale;
        }
    }

    private String getMenuName(Query query)
    {
        String menuName = query.getMenuName();

        if (menuName != null && menuName.trim().length() > 0)
            return menuName;

        return query.getName();
    }

    @Override
    public List<String> getOperationNames(Entity entity)
    {
        return entity.getOperations().stream()
                .map(BeModelElementSupport::getName).toList();
    }

    @Override
    public Operation getOperation(String entityName, String queryName, String name)
    {
        Operation operation = getProject().findOperation(entityName, queryName, name);
        if (operation == null)
        {
            if (getProject().findOperation(entityName, name) != null)
            {
                throw Be5Exception.operationNotAssignedToQuery(entityName, queryName, name);
            }
            else
            {
                throw Be5Exception.unknownOperation(entityName, name);
            }
        }
        return operation;
    }

    @Override
    public Operation getOperation(String entityName, String name)
    {
        Operation operation = getProject().findOperation(entityName, name);
        if (operation == null)
        {
            throw Be5Exception.unknownOperation(entityName, name);
        }
        return operation;
    }

    @Override
    public List<Query> getQueries(Entity entity, List<String> roles)
    {
        List<Query> permittedQueries = new ArrayList<>();

        for (Query query : entity.getQueries())
        {
            if (isAvailableFor(query, roles) && query.isAvailable())
            {
                permittedQueries.add(query);
            }
        }

        return permittedQueries;
    }

    @Override
    public boolean isAvailableFor(EntityItem entityItem, List<String> roles)
    {
        return roles.stream().anyMatch(entityItem.getRoles().getFinalRoles()::contains);
    }

    @Override
    public Query getQuery(String entityName, String queryName)
    {
        Query query = getEntity(entityName).getQueries().get(queryName);
        if (query == null)
        {
            throw Be5Exception.unknownQuery(entityName, queryName);
        }
        return query;
    }

    @Override
    public List<String> getQueryNames(Entity entity)
    {
        return entity.getQueries().stream()
                .map(BeModelElementSupport::getName).toList();
    }

    @Override
    public Map<String, ColumnDef> getColumns(String entityName)
    {
        Objects.requireNonNull(entityName);
        return getColumns(getEntity(entityName));
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public Map<String, ColumnDef> getColumns(Entity entity)
    {
        BeModelElement scheme = entity.getAvailableElement("Scheme");
        if (scheme == null) return new HashMap<>();

        if (scheme instanceof TableDef)
        {
            BeCaseInsensitiveCollection<ColumnDef> columns = (BeCaseInsensitiveCollection<ColumnDef>) ((TableDef) scheme).get("Columns");

            return StreamSupport.stream(columns.spliterator(), false).collect(
                    Utils.toLinkedMap(ColumnDef::getName, Function.identity())
            );
        }
        else
        {
            return Collections.emptyMap();
        }
    }

    @Override
    public ColumnDef getColumn(String entityName, String columnName)
    {
        Objects.requireNonNull(entityName);
        Objects.requireNonNull(columnName);
        return getColumns(entityName).get(columnName);
    }

    @Override
    public ColumnDef getColumn(Entity entity, String columnName)
    {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(columnName);
        return getColumns(entity).get(columnName);
    }

    @Override
    public Class<?> getColumnType(Entity entity, String columnName)
    {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(columnName);
        ColumnDef columnDef = getColumn(entity, columnName);
        if (columnDef == null)
        {
            throw Be5Exception.internal("Column '" + columnName + "' not found in '" + entity.getName() + "'");
        }
        return getColumnType(columnDef);
    }

    @Override
    public Class<?> getColumnType(String entityName, String columnName)
    {
        return getColumnType(getEntity(entityName), columnName);
    }

    @Override
    public Class<?> getColumnType(ColumnDef columnDef)
    {
        switch (columnDef.getType().getTypeName())
        {
            case SqlColumnType.TYPE_BIGINT:
            case SqlColumnType.TYPE_UBIGINT:
            case SqlColumnType.TYPE_KEY:
                return Long.class;
            case SqlColumnType.TYPE_INT:
            case SqlColumnType.TYPE_UINT:
                return Integer.class;
            case SqlColumnType.TYPE_SMALLINT:
                return Short.class;
            case SqlColumnType.TYPE_DECIMAL:
            case SqlColumnType.TYPE_CURRENCY:
                return Double.class;
            case SqlColumnType.TYPE_BOOL:
                return String.class;
            case SqlColumnType.TYPE_DATE:
                return Date.class;
            case SqlColumnType.TYPE_DATETIME:
            case SqlColumnType.TYPE_TIMESTAMP:
                return Timestamp.class;
            case SqlColumnType.TYPE_BLOB:
            case SqlColumnType.TYPE_MEDIUMBLOB:
                return byte[].class;
            default:
                return String.class;
        }
    }

    @Override
    public String getStaticPageContent(String language, String name)
    {
        return getProject().getStaticPageContent(language, name);
    }

    @Override
    public Project getProject()
    {
        return projectProvider.get();
    }

    @Override
    public boolean isParametrizedTable(Query query)
    {
        return !query.getParametrizingOperationName().isEmpty();
    }

    @Override
    public Set<String> getProjectRoles()
    {
        return getProject().getRoles();
    }

    @Override
    public List<Daemon> getDaemons()
    {
        return getProject().getAllDaemons();
    }

    @Override
    public Query createQueryFromSql(String sql)
    {
        Entity e = new Entity("be5DynamicQueries", getProject().getApplication(), EntityType.TABLE);
        e.setBesql(true);
        DataElementUtils.save(e);
        Query query = new Query("query", e);
        DataElementUtils.save(query);
        query.setQuery(sql)
;
        return query;
    }

    static class OrderedEntity implements Comparable<OrderedEntity>
    {
        public final Entity entity;
        public final int order;
        public final String title;

        OrderedEntity(Entity entity, String title)
        {
            Objects.requireNonNull(entity);
            Objects.requireNonNull(title);
            this.entity = entity;
            this.order = softParseInt(entity.getOrder(), Integer.MAX_VALUE);
            this.title = title;
        }

        @Override
        public int compareTo(OrderedEntity other)
        {
            Objects.requireNonNull(other);

            if (order != other.order)
                return Integer.compare(order, other.order);

            return title.compareTo(other.title);
        }

        private static int softParseInt(String order, int defaultValue)
        {
            if (order == null || order.trim().length() == 0)
            {
                return defaultValue;
            }

            try
            {
                return Integer.parseInt(order);
            }
            catch (NumberFormatException e)
            {
                return defaultValue;
            }
        }
    }

}
