package com.developmentontheedge.be5.server.services;

import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.server.model.DocumentPlugin;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.server.util.ParseRequestUtils;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;

import static com.developmentontheedge.be5.base.FrontendConstants.TOP_FORM;


public class DocumentFormPlugin implements DocumentPlugin
{
    private final FormGenerator formGenerator;
    private final UserAwareMeta userAwareMeta;

    @Inject
    public DocumentFormPlugin(FormGenerator formGenerator, UserAwareMeta userAwareMeta,
                              DocumentGenerator documentGenerator)
    {
        this.formGenerator = formGenerator;
        this.userAwareMeta = userAwareMeta;
        documentGenerator.addDocumentPlugin("topForm", this);
    }

    @Override
    public ResourceData addData(Query query, Map<String, Object> parameters)
    {
        String topForm = (String) ParseRequestUtils.getValuesFromJson(query.getLayout()).get(TOP_FORM);
        if (topForm != null)
        {
            if (userAwareMeta.hasAccessToOperation(query.getEntity().getName(), query.getName(), topForm))            {
                ResourceData operationResourceData = formGenerator.generate(query.getEntity().getName(), query.getName(), topForm, parameters, Collections.emptyMap());
                operationResourceData.setId("topForm");

                return operationResourceData;
            }
        }

        return null;
    }

}
