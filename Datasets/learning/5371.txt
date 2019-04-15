package com.developmentontheedge.be5.base.services.impl;

import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.model.EntityLocalizations;
import com.developmentontheedge.be5.metadata.model.EntityLocalizations.LocalizationRow;
import com.developmentontheedge.be5.metadata.model.LanguageLocalizations;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.Project;
import com.google.common.base.Function;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class CompiledLocalizations
{

    public static CompiledLocalizations from(Project project)
    {
        // language -> entity name -> entity localizations
        Table<String, String, CompiledEntityLocalizations> all = HashBasedTable.create();

        for (Module module : project.getModulesAndApplication())
        {
            collectLocalizations(module, all);
        }

        return new CompiledLocalizations(all);
    }

    private static void collectLocalizations(Module module, Table<String, String, CompiledEntityLocalizations> all)
    {
        for (LanguageLocalizations languageLocalizations : module.getLocalizations() )
        {
            collectLocalizations(languageLocalizations, all);
        }
    }

    private static void collectLocalizations(LanguageLocalizations localizations, Table<String, String, CompiledEntityLocalizations> all)
    {
        String language = localizations.getName();

        for (EntityLocalizations entityLocalizations : localizations)
        {
            collectLocalizations(language, entityLocalizations, all);
        }
    }

    private static void collectLocalizations(String language, EntityLocalizations localizations, Table<String, String, CompiledEntityLocalizations> all)
    {
        String entityName = localizations.getName();
        Table<String, String, String> targetEntityLocalizations = toTable(localizations);
        CompiledEntityLocalizations createdInThisLoopLocalizations = all.get(language, entityName);

        if (createdInThisLoopLocalizations == null)
        {
            all.put(language, entityName, new CompiledEntityLocalizations(targetEntityLocalizations));
        }
        else
        {
            createdInThisLoopLocalizations.entityLocalizations.putAll(targetEntityLocalizations);
        }
    }

    private static Table<String, String, String> toTable(EntityLocalizations entityLocalizations)
    {
        Table<String, String, String> targetEntityLocalizations = HashBasedTable.create();

        for (LocalizationRow row : entityLocalizations.getRawRows()) // XXX this can be incorrect: getRows()?
        {
            targetEntityLocalizations.put(row.getTopic(), row.getKey(), row.getValue());
        }

        return targetEntityLocalizations;
    }

    private static class CompiledEntityLocalizations
    {
        // topic -> key -> value
        final Table<String, String, String> entityLocalizations;

        public CompiledEntityLocalizations(Table<String, String, String> entityLocalizations)
        {
            this.entityLocalizations = entityLocalizations;
        }

        public static Function<CompiledEntityLocalizations, String> fnGetFirstByTopic(final String topic)
        {
            return entityLocalizations -> entityLocalizations.getFirstByTopic(topic);
        }

        public static Function<CompiledEntityLocalizations, String> fnGetByTopicAndKey(final String topic, final String key)
        {
            return entityLocalizations -> entityLocalizations.getByTopicAndKey(topic, key);
        }

        String getByTopicAndKey(String topic, String key)
        {
            return entityLocalizations.get(topic, key);
        }

        String getFirstByTopic(String topic)
        {
            Map<String, String> pairs = entityLocalizations.row(topic);
            return Iterables.getFirst(pairs.values(), null);
        }
    }

    // language -> entity -> CompiledEntityLocalizations
    private final Table<String, String, CompiledEntityLocalizations> all;

    public CompiledLocalizations(Table<String, String, CompiledEntityLocalizations> all)
    {
        this.all = all;
    }

    public Optional<String> getEntityTitle(String language, final String entityName)
    {
        checkNotNull(language);
        checkNotNull(entityName);
        return findLocalization(language, entityName,
                CompiledEntityLocalizations.fnGetFirstByTopic(DatabaseConstants.L10N_TOPIC_DISPLAY_NAME));
    }

    public String getOperationTitle(String language, String entityName, final String name)
    {
        checkNotNull(language);
        checkNotNull(entityName);
        checkNotNull(name);
        Optional<String> localization = findLocalization(language, entityName,
                CompiledEntityLocalizations.fnGetByTopicAndKey(DatabaseConstants.L10N_TOPIC_OPERATION_NAME, name));

        return localization.orElseGet(() -> findLocalization(language, "default",
                CompiledEntityLocalizations.fnGetByTopicAndKey(DatabaseConstants.L10N_TOPIC_OPERATION_NAME, name))
                .orElse(name));
    }

    public String getQueryTitle(String language, String entityName, final String queryName)
    {
        checkNotNull(language);
        checkNotNull(entityName);
        checkNotNull(queryName);
        Optional<String> localization = findLocalization(language, entityName,
                CompiledEntityLocalizations.fnGetByTopicAndKey(DatabaseConstants.L10N_TOPIC_VIEW_NAME, queryName));

        return localization.orElseGet(() -> findLocalization(language, "index",
                CompiledEntityLocalizations.fnGetByTopicAndKey(DatabaseConstants.L10N_TOPIC_VIEW_NAME, queryName))
                .orElse(queryName));
    }

    public Optional<String> getFieldTitle(String language, String entityName, String operationName, String queryName, String name)
    {
        Optional<String> title = get(language, entityName, operationName, name);

        if (!title.isPresent())
        {
            title = get(language, entityName, queryName, name);
        }

        return title;
    }

    public Optional<String> getFieldTitle(String language, String entityName, String operationName, String name)
    {
        return get(language, entityName, operationName, name);
    }

    public Optional<String> get(String language, String entityName, String queryName, String content)
    {
        checkNotNull(language);
        checkNotNull(entityName);
        checkNotNull(queryName);
        checkNotNull(content);

        Optional<String> localization = findLocalization(language, entityName,
                CompiledEntityLocalizations.fnGetByTopicAndKey(queryName, content));

        if (!localization.isPresent())
        {
            localization = findLocalization(language, entityName,
                    CompiledEntityLocalizations.fnGetByTopicAndKey("Insert", content));
        }

        if (!localization.isPresent())
        {
            localization = findLocalization(language, "query.jsp",
                    CompiledEntityLocalizations.fnGetByTopicAndKey("page", content));
        }

        if (!localization.isPresent())
        {
            localization = findLocalization(language, "index.jsp",
                    CompiledEntityLocalizations.fnGetByTopicAndKey("page", content));
        }

        if (!localization.isPresent())
        {
            localization = findLocalization(language, "operation.jsp",
                    CompiledEntityLocalizations.fnGetByTopicAndKey("page", content));
        }

//use for send to frontend
//        if(!localization.isPresent())
//        {
//            localization = findLocalization(language, "index",
//                    CompiledEntityLocalizations.fnGetByTopicAndKey("page", content));
//        }

        return localization;
    }

    private Optional<String> findLocalization(String language, String entityName, Function<CompiledEntityLocalizations, String> continuation)
    {
        CompiledEntityLocalizations entityLocalizations = all.get(language.toLowerCase(Locale.US), entityName);

        if (entityLocalizations == null)
        {
            return Optional.empty();
        }

        return Optional.ofNullable(continuation.apply(entityLocalizations));
    }

}
