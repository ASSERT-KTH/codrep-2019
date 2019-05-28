package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.server.helpers.MenuHelper;
import com.developmentontheedge.be5.server	.servlet.support.JsonApiController;
import com.developmentontheedge.be5.web.Request;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class MenuController extends JsonApiController
{
    private final MenuHelper menuHelper;

    @Inject
    public MenuController(MenuHelper menuHelper)
    {
        this.menuHelper = menuHelper;
    }

    /**
     * Generated JSON sample:
     * <pre>
     * <code>
     *   { "root": [
     *     { "title": "Entity With All Records", "action": {"name":"ajax", "arg":"entity.query"} },
     *     { "title": "Some Entity", children: [
     *       { "title": "Query1", "action": {"name":"url", "arg":"https://www.google.com"} }
     *     ] }
     *   ] }
     * </code>
     * </pre>
     */
    @Override
    public Object generate(Request req, String requestSubUrl)
    {
        switch (requestSubUrl)
        {
            case "":
                return new MenuResponse(menuHelper.collectEntities(false, EntityType.TABLE));
            case "dictionary":
                return new MenuResponse(menuHelper.collectEntities(false, EntityType.DICTIONARY));
            case "withIds":
                return new MenuResponse(menuHelper.collectEntities(true, EntityType.TABLE));
            default:
                return null;
        }
    }

    public static class MenuResponse
    {
        final List<MenuHelper.RootNode> root;

        MenuResponse(List<MenuHelper.RootNode> root)
        {
            this.root = root;
        }

        public List<MenuHelper.RootNode> getRoot()
        {
            return root;
        }
    }
}
