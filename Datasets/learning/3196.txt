package com.developmentontheedge.be5.server.model.table;

import com.developmentontheedge.be5.query.model.CellModel;

import java.util.List;

public class MoreRows
{
    private final int recordsTotal;
    private final int recordsFiltered;
    private final List<List<CellModel>> data; // rows

    public MoreRows(int recordsTotal, int recordsFiltered, List<List<CellModel>> data)
    {
        this.recordsTotal = recordsTotal
;
        this.recordsFiltered = recordsFiltered;
        this.data = data;
    }

    public int getRecordsTotal()
    {
        return recordsTotal;
    }

    public int getRecordsFiltered()
    {
        return recordsFiltered;
    }

    public List<List<CellModel>> getData()
    {
        return data;
    }
}
