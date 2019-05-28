package com.developmentontheedge.be5.server.util;

import com.developmentontheedge.be5.base.util.HashUrl;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.server.model.Action;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.developmentontheedge.be5.metadata.DatabaseConstants.ALL_RECORDS_VIEW;


public final class ActionUtils
{
    public static Action toAction(Query query)
    {
        if (isExternalRef(query))
        {
            return Action.open(query.getQuery());
        }
        else if (isViewBlob(query))
        {
            return Action.open(new HashUrl( "api", "download", query.getEntity().getName(), query.getName()).toString());
        }
        else if (isAction(query))
        {
            return Action.call(query.getQuery());
        }
        else if (isStaticPage(query))
        {
            String url = query.getQuery();
            if (url.startsWith("static/"))
            {
                url = url.replaceFirst("static/", "");
            }
            return Action.call(new HashUrl("static", url));
        }
        else
        {
            if (query.getType() == QueryType.STATIC)
            {
                //move to static LegacyUrlParser
                //mspReceiverCategories.redir
                if (query.getQuery().contains(".redir")
                        || query.getQuery().startsWith("q?")
                        || query.getQuery().startsWith("o?"))
                {
                    //ArrayList<String> positional = new ArrayList<>();
                    String entityName = "_";
                    Map<String, String> params;

                    if (query.getQuery().contains(".redir"))
                    {
                        String[] parts = query.getQuery().split(".redir");
                        if (parts.length > 1)
                        {
                            String[] paramsVal = parts[1].replace("?", "").split("&");

                            params = getParams(paramsVal);
                        }
                        else
                        {
                            params = new HashMap<>();
                        }
                        entityName = parts[0];
                    }
                    else if (query.getQuery().startsWith("q?"))
                    {
                        params = getParams(query.getQuery().replaceFirst("q\\?", "").split("&"));
                    }
                    else if (query.getQuery().startsWith("o?"))
                    {
                        params = getParams(query.getQuery().replaceFirst("o\\?", "").split("&"));
                    }
                    else
                    {
                        params = new HashMap<>();
                    }

                    if (params.get("_t_") != null)
                    {
                        entityName = params.remove("_t_");
                    }

                    String qn = params.remove("_qn_");
                    if (qn == null) qn = ALL_RECORDS_VIEW;

                    HashUrl hashUrl;
                    if (params.get("_on_") != null)
                    {
                        hashUrl = new HashUrl("form", entityName, qn, params.remove("_on_"));
                    }
                    else
                    {
                        hashUrl = new HashUrl("table", entityName, qn);
                    }

                    return Action.call(hashUrl.named(params));
                }

                return Action.call(new HashUrl("servlet").named("path", query.getQuery()));
            }

            return Action.call(new HashUrl("table", query.getEntity().getName(), query.getName()));
        }
    }

    private static Map<String, String> getParams(String[] values)
    {
        Map<String, String> params = new HashMap<>();

        for (String s : values)
        {
            String[] split = s.split("=");
            params.put(split[0], split[1].replace("+", " "));
        }
        return params;
    }

    public static Action toAction(String query, Operation operation)
    {
        String entityName = operation.getEntity().getName();
        HashUrl hashUrl = new HashUrl("form", entityName, query, operation.getName());

        return Action.call(hashUrl);
    }

    public static boolean isStaticPage(Query query)
    {
        return query.getType() == QueryType.STATIC && (query.getQuery().endsWith(".be") || query.getQuery().startsWith("static/"));
    }

    private static final Pattern ACTION_PATTERN = Pattern.compile("^\\w+$");

    private static boolean isExternalRef(Query query)
    {
        return query.getType() == QueryType.STATIC && (query.getQuery().startsWith("http://") || query.getQuery().startsWith("https://"));
    }

    private static boolean isAction(Query query)
    {
        return query.getType() == QueryType.STATIC && ACTION_PATTERN.matcher(query.getQuery()).matches();
    }

    private static boolean isViewBlob(Query query)
    {
        return query.getType() == QueryType.STATIC && (query.getQuery() != null && query.getQuery().startsWith("viewBlob?"));
    }

}
