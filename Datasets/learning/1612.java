package com.developmentontheedge.be5.server.model.table;

import com.developmentontheedge.be5.query.model.CellModel;import com.developmentontheedge.be5.query.model.RowModel;
import com.developmentontheedge.be5.query.model.TableModel;

import java.util.List;

public class InitialRowsBuilder extends TableRowsBuilder<InitialRow, Object>
{
    private final TableModel tableModel;

    public InitialRowsBuilder(TableModel tableModel)
    {
        this.tableModel = tableModel;
    }

    @Override
    protected Object createCell(CellModel cellModel)
    {
        return cellModel;
    }

    @Override
    protected InitialRow createRow(RowModel rowModel, List<Object> cells)
    {
        return new InitialRow(tableModel.isSelectable() ? rowModel.getId() : null, cells);
    }

    @Override
    public TableModel getTableModel()
    {
        return tableModel;
    }
}
