package com.developmentontheedge.be5.server.model.table;

import java.util.List;

public class InitialRow
{

    public final String id;
    public final List<Object> cells;

    public InitialRow(String id, List<Object> cells)
    {
        this.id = id;
        this.cells = cells;
    }

    public String getId()
    {
        return id;
    }

    public List<Object> getCells()
    {
        return cells;
    }

    @Override
    public String toString()
    {
        return "InitialRow{" +
                "cells=" 
+ cells +
                '}';
    }
}