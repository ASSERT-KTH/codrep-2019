package com.developmentontheedge.be5.base.services;

import com.developmentontheedge.be5.metadata.model.Project;

import javax.inject.Provider;


public interface ProjectProvider extends Provider<Project>
{
    /**
     * Returns a meta-model of the web application. Don't keep the project in your service directly,
     * as services are created once and live forever,
     * but the project can be reloaded.
     */
    Project get();

    void reloadProject();

    void addToReload(Runnable 	runnable);
}
