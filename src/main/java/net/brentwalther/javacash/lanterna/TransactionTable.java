package net.brentwalther.javacash.lanterna;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.googlecode.lanterna.gui2.Border;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.ScrollBar;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import net.brentwalther.javacash.model.Transaction;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

public class TransactionTable implements Supplier<Component> {
  private final Component container;
  private final Table<String> table;

  private TransactionTable(Component container, Table<String> table) {
    this.container = container;
    this.table = table;
  }

  public static TransactionTable create() {
    Panel contentPanel = new Panel(new BorderLayout());

    ScrollBar scrollBar = new ScrollBar(Direction.VERTICAL);
    Table<String> table = new ScrollableTable(scrollBar);
    table.setCellSelection(true);
    table.setTableModel(new TableModel<>());

    contentPanel.addComponent(table, BorderLayout.Location.CENTER);
    contentPanel.addComponent(scrollBar, BorderLayout.Location.RIGHT);

    Border container = Borders.singleLine("Transactions");
    container.setComponent(contentPanel);
    return new TransactionTable(container, table);
  }

  public void setUiState(UiState newState) {
    String[] columnHeaders =
        FluentIterable.from(newState.columns).transform(Column::toString).toArray(String.class);
    TableModel<String> tableModel = new TableModel<>(columnHeaders);
    for (Transaction transaction : newState.transactions) {
      tableModel.addRow(
          ImmutableList.of(
              transaction
                  .getPostDate()
                  .atZone(ZoneId.systemDefault())
                  .format(DateTimeFormatter.ISO_LOCAL_DATE),
              transaction.getDescription(),
              transaction.getSplitList().get(0).getAmount().toString()));
    }
    this.table.setTableModel(tableModel);
  }

  @Override
  public Component get() {
    return this.container;
  }

  public enum Column {
    DATE,
    DESCRIPTION,
    AMOUNT
  }

  public static class UiState {
    private final ImmutableList<Column> columns = ImmutableList.copyOf(Column.values());
    private final Iterable<Transaction> transactions;

    public UiState(Iterable<Transaction> transactions) {
      this.transactions = transactions;
    }
  }
}
