package com.developmentontheedge.be5.base.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class SimpleCompositeMap extends AbstractMap
{
    protected Set set;

    List<Map> maps;

    protected Map me = new HashMap();

    public static Map valueMap(Object... values)
    {
        if (values == null)
        {
            return null;
        }
        LinkedHashMap map = new LinkedHashMap(values.length);
        for (int i = 0; i < values.length; i += 2)
        {
            map.put(values[i], values[i + 1]);
        }
        return map;
    }

    public static Map valueNotNullMap(Object... values)
    {
        if (values == null)
        {
            return null;
        }
        LinkedHashMap map = new LinkedHashMap(values.length / 2);
        for (int i = 0; i < values.length; i += 2)
        {
            if (values[i + 1] != null)
            {
                map.put(values[i], values[i + 1]);
            }
        }
        return map;
    }

    public SimpleCompositeMap(Map basis, Object... values)
    {
        this(basis, valueMap(values));
    }

    public SimpleCompositeMap(Map... varMaps)
    {
        maps = Arrays.asList(varMaps);

        set = new AbstractSet()
        {
            public int size()
            {
                int size = SimpleCompositeMap.this.me.size();
                for (int i = 0; i < SimpleCompositeMap.this.maps.size(); i++)
                {
                    size += SimpleCompositeMap.this.maps.get(i).size();
                }
                return size;
            }

            public Iterator iterator()
            {
                throw new RuntimeException("This class shouldn't be used via Iterator");
            }
        };
    }

    public Set entrySet()
    {
        return set;
    }

    @Override
    public boolean containsKey(Object key)
    {
        for (int i = 0; i <maps.size(); i++)
        {
            boolean contains = maps.get(i).containsKey(key);
            if (contains) return true;
        }
        return me.containsKey(key);
    }

    public Object get(Object key)
    {
        for (int i = 0; i < maps.size(); i++)
        {
            Object val = maps.get(i).get(key);
            if (val != null)
            {
                return val;
            }
        }
        return me.get(key);
    }

    public Object put(Object key, Object value)
    {
        return me.put(key, value);
    }
}