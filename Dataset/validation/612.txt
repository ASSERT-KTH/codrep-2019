package com.developmentontheedge.be5.server.model.table;

import com.developmentontheedge.be5.query.model.CellModel;
import com.developmentontheedge.be5.query.model.RowModel;
import com.developmentontheedge.be5.query.model.TableModel;
import com.google.common.collect.ImmutableList;import com.google.common.collect.ImmutableList.Builder;

import java.util.List;

public class MoreRowsBuilder extends TableRowsBuilder<List<CellModel>, CellModel>
{
    private final TableModel tableModel;

    public MoreRowsBuilder(TableModel tableModel)
    {
        this.tableModel = tableModel;
    }

    @Override
    protected CellModel createCell(CellModel cellModel)
    {
        return cellModel;
    }

    @Override
    protected List<CellModel> createRow(RowModel rowModel, List<CellModel> cells)
    {
        Builder<CellModel> builder = ImmutableList.builder();
        if (tableModel.isSelectable())
        {
            builder = builder.add(new CellModel(rowModel.getId()));
        }
        else
        {
            builder = builder.add(new CellModel(""));
        }
        return builder.addAll(cells).build();
    }

    @Override
    public TableModel getTableModel()
    {
        return tableModel;
    }
}
