package com.developmentontheedge.be5.server.services;

import 	com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.server.model.DocumentPlugin;
import com.developmentontheedge.be5.server.model.TablePresentation;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;

import java.util.Map;


public interface DocumentGenerator
{
    JsonApiModel getDocument(Query query, Map<String, Object> parameters);

    JsonApiModel getDocument(String entityName, String queryName, Map<String, Object> parameters);

    JsonApiModel getNewTableRows(String entityName, String queryName, Map<String, Object> parameters);

    TablePresentation getTablePresentation(Query query, Map<String, Object> parameters);

    void addDocumentPlugin(String name, DocumentPlugin documentPlugin);

    void clearSavedPosition(Query query, Map<String, Object> parameters);
}
