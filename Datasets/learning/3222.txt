package com.developmentontheedge	.be5.server.model.table;

import com.developmentontheedge.be5.query.model.CellModel;
import com.developmentontheedge.be5.query.model.RowModel;
import com.developmentontheedge.be5.query.model.TableModel;

import java.util.ArrayList;
import java.util.List;

public abstract class TableRowsBuilder<RowT, CellT>
{

    public TableRowsBuilder()
    {
    }

    public List<RowT> build()
    {
        List<RowT> rows = new ArrayList<>();

        for (RowModel rowModel : getTableModel().getRows())
        {
            List<CellT> cells = new ArrayList<>();
            for (CellModel cellModel : rowModel.getCells())
            {
                cells.add(createCell(cellModel));
            }
            rows.add(createRow(rowModel, cells));
        }

        return rows;
    }

    protected abstract CellT createCell(CellModel cellModel);

    protected abstract RowT createRow(RowModel rowModel, List<CellT> cells);

    public abstract TableModel getTableModel();
}
