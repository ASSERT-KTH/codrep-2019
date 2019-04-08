package com.developmentontheedge.sql.format;

import com.developmentontheedge.sql.model.AstBeSqlSubQuery;
import one.util.streamex.StreamEx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class BasicQueryContext implements QueryContext
{
    private final Map<String, List<String>> parameters;
    private final Map<String, Object> sessionVariables;
    private final String userName;
    private final List<String> roles;
    private final QueryResolver queryResolver;
    private final Map<String, AstBeSqlSubQuery> subQueries = new HashMap<>();

    @FunctionalInterface
    public interface QueryResolver
    {
        public String resolve(String entityName, String queryName);
    }

    private BasicQueryContext(Map<String, List<String>> parameters, Map<String, Object> sessionVariables, String userName,
                              List<String> roles, QueryResolver queryResolver)
    {
        this.parameters = parameters;
        this.sessionVariables = sessionVariables;
        this.userName = userName;
        this.roles = roles;
        this.queryResolver = queryResolver;
    }

    @Override
    public String getUserName()
    {
        return userName;
    }

    @Override
    public List<String> getListParameter(String name)
    {
        return parameters.get(name);
    }

    @Override
    public String getParameter(String name)
    {
        if (parameters.get(name) == null)
            return null;
        if (parameters.get(name).size() != 1)
            throw new IllegalStateException(name + " contains more than one value");
        else
            return parameters.get(name).get(0);
    }

    @Override
    public Map<String, String> asMap()
    {
        return StreamEx.ofKeys(parameters).toMap(this::getParameter);
    }

    @Override
    public Object getSessionVariable(String name)
    {
        return sessionVariables.get(name);
    }

    @Override
    public StreamEx<String> roles()
    {
        return StreamEx.of(roles);
    }

    @Override
    public String getDictionaryValue(String tagName, String name, Map<String, String> parameters)
    {
        return "";
    }

    @Override
    public String resolveQuery(String entity, String name)
    {
        
return queryResolver.resolve(entity, name);
    }

    public static class Builder
    {
        private final Map<String, List<String>> parameters = new HashMap<>();
        private final Map<String, Object> sessionVariables = new HashMap<>();
        private String userName;
        private final List<String> roles = new ArrayList<>();
        private QueryResolver queryResolver = (entity, query) -> null;

        public Builder()
        {
        }

        public Builder roles(String... roles)
        {
            this.roles.addAll(Arrays.asList(roles));
            return this;
        }

        public Builder parameter(String name, String value)
        {
            if (parameters.containsKey(name))
                parameters.get(name).add(value);
            else
            {
                List<String> list = new ArrayList<String>();
                list.add(value);
                parameters.put(name, list);
            }
            return this;
        }

        public Builder sessionVar(String name, Object value)
        {
            sessionVar(name, value, null);
            return this;
        }

        public Builder sessionVar(String name, Object value, String className)
        {
            sessionVariables.put(name, SqlTypeUtils.parseValue(value, className));

            return this;
        }

        public Builder userName(String name)
        {
            this.userName = name;
            return this;
        }

        public Builder queryResolver(QueryResolver queryResolver)
        {
            this.queryResolver = Objects.requireNonNull(queryResolver);
            return this;
        }

        public QueryContext build()
        {
            return new BasicQueryContext(parameters, sessionVariables, userName, roles.isEmpty() ? Collections.singletonList("Guest")
                    : roles, queryResolver);
        }
    }

    @Override
    public Map<String, AstBeSqlSubQuery> getSubQueries()
    {
        return subQueries;
    }
}
