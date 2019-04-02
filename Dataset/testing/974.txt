package com.developmentontheedge.be5.base.services.impl;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.exceptions.ErrorTitles;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.base.util.MoreStrings;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.QuerySettings;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;


public class UserAwareMetaImpl implements UserAwareMeta
{
    /**
     * The prefix constant for localized message.
     * <br/>This "{{{".
     */
    private static final String LOC_MSG_PREFIX = "{{{";

    /**
     * The postfix constant for localized message.
     * <br/>This "}}}".
     */
    private static final String LOC_MSG_POSTFIX = "}}}";

    private static final Pattern MESSAGE_PATTERN = MoreStrings.variablePattern(LOC_MSG_PREFIX, LOC_MSG_POSTFIX);

    private CompiledLocalizations localizations;

    private final Meta meta;
    private final ProjectProvider projectProvider;
    private final UserInfoProvider userInfoProvider;

    @Inject
    public UserAwareMetaImpl(Meta meta, ProjectProvider projectProvider, UserInfoProvider userInfoProvider)
    {
        this.meta = meta;
        this.projectProvider = projectProvider;
        this.userInfoProvider = userInfoProvider;

        projectProvider.addToReload(this::compileLocalizations);
        compileLocalizations();
    }

//    @Override
//    public void configure(String config)
//    {
//        compileLocalizations();
//    }

    @Override
    public void compileLocalizations()
    {
        localizations = CompiledLocalizations.from(projectProvider.get());
    }

    //todo localize entity, query, operation names
    @Override
    public String getLocalizedBe5ErrorMessage(Be5Exception e)
    {
        return ErrorTitles.formatTitle(
                getLocalizedExceptionMessage(ErrorTitles.getTitle(e.getCode())),
                e.getParameters()
        );
    }

    @Override
    public String getLocalizedEntityTitle(Entity entity)
    {
        Optional<String> localization = localizations.getEntityTitle(getLanguage(), entity.getName());

        if (!localization.isPresent())
        {
            if (!Strings.isNullOrEmpty(entity.getDisplayName()))
            {
                return entity.getDisplayName();
            }
            return entity.getName();
        }

        return localization.get();
    }

    @Override
    public String getLocalizedEntityTitle(String entity)
    {
        return getLocalizedEntityTitle(meta.getEntity(entity));
    }

    @Override
    public String getLocalizedQueryTitle(String entity, String query)
    {
        return localizations.getQueryTitle(getLanguage(), entity, query);
    }

    @Override
    public String getLocalizedOperationTitle(Operation operation)
    {
        return localizations.getOperationTitle(getLanguage(),
                operation.getEntity().getName(), operation.getName());
    }

    @Override
    public String getLocalizedOperationTitle(String entity, String name)
    {
        return localizations.getOperationTitle(getLanguage(), entity, name);
    }

    public String getLocalizedOperationField(String entityName, String operationName, String name)
    {
        return localizations.getFieldTitle(getLanguage(), entityName, operationName, name)
                .orElseGet(() -> getColumnTitle(entityName, name));
    }

    @Override
    public String getLocalizedCell(String content, String entity, String query)
    {
        String localized = MoreStrings.substituteVariables(content, MESSAGE_PATTERN, (message) ->
                localizations.get(getLanguage(), entity, query, message).orElse(message)
        );

        if (localized.startsWith("{{{") && localized.endsWith("}}}"))
        {
            String clearContent = localized.substring(3, localized.length() - 3);
            return localizations.get(getLanguage(), entity, query, clearContent)
                    .orElse(clearContent);
        }

        return localized;
    }

    @Override
    public String getLocalizedValidationMessage(String message)
    {
        return localizations.get(getLanguage(), "messages.l10n", "validation", message).orElse(message);
    }

    @Override
    public String getLocalizedExceptionMessage(String message)
    {
        return localizations.get(getLanguage(), "messages.l10n", "exception", message).orElse(message);
    }

    @Override
    public String getLocalizedInfoMessage(String message)
    {
        return localizations.get(getLanguage(), "messages.l10n", "info", message).orElse(message);
    }

    @Override
    public QuerySettings getQuerySettings(Query query)
    {
        List<String> currentRoles = userInfoProvider.get().getCurrentRoles();
        for (QuerySettings settings : query.getQuerySettings())
        {
            Set<String> roles = settings.getRoles().getFinalRoles();
            for (String role : currentRoles)
            {
                if (roles.contains(role))
                {
                    return settings;
                }
            }
        }
        return new QuerySettings(query);
    }

    @Override
    public Operation getOperation(String entityName, String name)
    {
        Operation operation = meta.getOperation(entityName, name);
        if (!meta.hasAccess(operation.getRoles(), userInfoProvider.get().getCurrentRoles()))
            throw Be5Exception.accessDeniedToOperation(entityName, name);

        return operation;
    }

    @Override
    public boolean hasAccessToOperation(String entityName, String queryName, String name)
    {
        Operation operation = meta.getOperation(entityName, queryName, name);
        return meta.hasAccess(operation.getRoles(), userInfoProvider.get().getCurrentRoles());
    }

    @Override
    public Operation getOperation(String entityName, String queryName, String name)
    {
        Operation operation = meta.getOperation(entityName, queryName, name);
        if (!meta.hasAccess(operation.getRoles(), userInfoProvider.get().getCurrentRoles()))
            throw Be5Exception.accessDeniedToOperation(entityName, name);

        return operation;
    }

    @Override
    public Query getQuery(String entityName, String queryName)
    {
        Query query = meta.getQuery(entityName, queryName);
        if (!meta.hasAccess(query.getRoles(), userInfoProvider.get().getCurrentRoles()))
            throw Be5Exception.accessDeniedToQuery(entityName, queryName);
        return query;
    }

    @Override
    public String getColumnTitle(String entityName, String queryName, String columnName)
    {
        return localizations.get(getLanguage(), entityName, queryName, columnName).orElse(columnName);
    }

    public String getColumnTitle(String entityName, String columnName)
    {
        ImmutableList<String> defaultQueries = ImmutableList.of("All records");
        for (String queryName : defaultQueries)
        {
            Optional<String> columnTitle = localizations.get(getLanguage (), entityName, queryName, columnName);
            if (columnTitle.isPresent()) return columnTitle.get();
        }
        return columnName;
    }

    @Override
    public String getFieldTitle(String entityName, String operationName, String queryName, String name)
    {
        return localizations.getFieldTitle(getLanguage(), entityName, operationName, queryName, name).orElse(name);
    }

    public CompiledLocalizations getLocalizations()
    {
        return localizations;
    }

    private String getLanguage()
    {
        return meta.getLocale(userInfoProvider.get().getLocale()).getLanguage();
    }

    @Override
    public String getStaticPageContent(String name)
    {
        String pageContent = projectProvider.get().getStaticPageContent(getLanguage(), name);
        if (pageContent == null)
            throw Be5Exception.notFound("static/" + name);

        return pageContent;
    }
}
