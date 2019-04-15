package com.developmentontheedge.be5.base.services.impl;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.Configurable;
import com.google.gson.Gson;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class ConfigurationProvider
{
    private static final String CONFIG_FILE = "config.yaml";

    private final Map<Class<?>, Object> configurations;

    public ConfigurationProvider()
    {
        configurations = readConfigurations();
    }

    public <T> Object configure(T object)
    {
        if (object instanceof Configurable)
        {
            @SuppressWarnings("unchecked")
            Configurable<Object> configurable = (Configurable<Object>) object;
            return getConfiguration(object.getClass(), configurable.getConfigurationClass());
        }
        else
        {
            throw Be5Exception.internal("Class must implement Configurable: " + object.getClass().getCanonicalName());
        }
    }

    private Object getConfiguration(Class<?> clazz, Class<Object> configClass)
    {
        Object config = configurations.get(clazz);

        if (config == null)
        {
            return null;
        }

        //todo use json-b
        String componentConfigJson = new Gson().toJson(config);

        return new Gson().fromJson(componentConfigJson, configClass);
    }

    @SuppressWarnings("unchecked")
    private Map<Class<?>, Object> readConfigurations()
    {
        Map<Class<?>, Object> configurations = new HashMap<>();
//todo config be5.configPath
//        ServletContext ctx = ServletContexts.getServletContext();
//        Path projectSource = ConfigurationProvider.getPath( ctx, "be5.configPath" );
//
//        if (projectSource == null)
//        {
//            configurations = ImmutableMap.of("components", ImmutableMap.of());
//            return;
//        }

        try
        {
            //String text = Files.asCharSource(projectSource.resolve(CONFIG_FILE).toFile(), Charsets.UTF_8).read();
            //String text = Files.asCharSource(getClass().getClassLoader().getResources(CONFIG_FILE), Charsets.UTF_8).read();

            ArrayList<URL> urls = Collections.list(getClass().getClassLoader().getResources(CONFIG_FILE));

            if (urls.size() > 1) throw new RuntimeException("must be one config");

            if (urls.size() == 0)
            {
                return configurations;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(urls.get(0).openStream(), StandardCharsets.UTF_8)))
            {
                Map<String, Object> config = (Map<String, Object>) ((Map<String, Object>) new Yaml().load(reader)).get("config");
                if (config != null)
                {
                    for (Map.Entry<String, Object> entry : config.entrySet())
                    {
                        configurations.put(loadClass(entry.getKey()), entry.getValue());
                    }
                }
            }

            return configurations;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private Class< ?> loadClass(String path)
    {
        try
        {
            return Class.forName(path);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("ClassNotFoundException by path='" + path + "' in " + CONFIG_FILE, e);
        }
    }
}
