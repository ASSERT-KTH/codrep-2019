package com.developmentontheedge.be5.base.services;

import com.github.benmanes.caffeine.cache.Cache;

import java.util.Map;


public interface Be5Caches
{
    void registerCache(String name, Cache cache);

    <K, V> Cache<K, V> createCache(String name);

    Map<String, Cache> 
getCaches();

    Cache getCache(String name);

    void clearAll();

    int getCacheSize(String name);
}
