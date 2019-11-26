package net.brentwalther.javacash.lanterna;

import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.input.KeyStroke;

public class ScrollableTable extends Table<String> {

  public ScrollableTable() {
    super("");
  }

  @Override
  public synchronized Table<String> setTableModel(TableModel<String> tableModel) {
    setViewTopRow(0);
    return super.setTableModel(tableModel);
  }

  @Override
  public Result handleKeyStroke(KeyStroke keyStroke) {
    switch (keyStroke.getKeyType()) {
      case ArrowUp:
        moveSelectedRowBy(-1);
        return Result.HANDLED;
      case ArrowDown:
        moveSelectedRowBy(1);
        return Result.HANDLED;
    }

    return super.handleKeyStroke(keyStroke);
  }

  private void moveSelectedRowBy(int delta) {
    int lastRowIndex = getTableModel().getRowCount();
    int visibleRows = getSize().getRows() - 1;
    int rowOffset = getViewTopRow();
    int currentRow = getSelectedRow();
    if (rowOffset == currentRow && delta < 0) {
      rowOffset = Math.max(0, rowOffset + delta);
    } else if ((currentRow - rowOffset) == visibleRows - 1 && delta > 0) {
      rowOffset = Math.min(lastRowIndex - visibleRows, rowOffset + 1);
    }
    int newCurrentRow = Math.min(Math.max(0, currentRow + delta), lastRowIndex - 1);
    setViewTopRow(rowOffset);
    setSelectedRow(newCurrentRow);
  }
}
