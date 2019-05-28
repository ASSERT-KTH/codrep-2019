package com.developmentontheedge.be5.server.servlet;

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;


public class TemplateModule extends ServletModule
{
    @ Override
    protected void configureServlets()
    {
        bind(TemplateFilter.class).in(Scopes.SINGLETON);

        filter("/*").through(TemplateFilter.class);
    }
}
