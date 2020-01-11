package net.brentwalther.javacash.lanterna;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.googlecode.lanterna.gui2.Border;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.ScrollBar;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import io.reactivex.rxjava3.subjects.Subject;
import net.brentwalther.javacash.model.Account;
import net.brentwalther.javacash.model.Split;
import net.brentwalther.javacash.model.Transaction;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class TransactionTable implements OptionalComponent {
  private final Subject<Optional<Component>> componentObservable = ReplaySubject.createWithSize(1);
  private final Component container;
  private final Table<String> table;

  private TransactionTable(Component container, Table<String> table) {
    this.container = container;
    this.table = table;
    componentObservable.onNext(Optional.empty());
  }

  public static Observable<Optional<Component>> create(Observable<UIState> uiStateObservable) {
    Panel contentPanel = new Panel(new BorderLayout());

    ScrollBar scrollBar = new ScrollBar(Direction.VERTICAL);
    Table<String> table = new ScrollableTable(scrollBar);
    table.setCellSelection(true);
    table.setTableModel(new TableModel<>());

    contentPanel.addComponent(table, BorderLayout.Location.CENTER);
    contentPanel.addComponent(scrollBar, BorderLayout.Location.RIGHT);

    Border container = Borders.singleLine("Transactions");
    container.setComponent(contentPanel);
    TransactionTable transactionTable = new TransactionTable(container, table);
    uiStateObservable.subscribe(transactionTable::setUiState);
    return transactionTable.observable();
  }

  @Override
  public Observable<Optional<Component>> observable() {
    return componentObservable;
  }

  private void setUiState(UIState newState) {
    String[] columnHeaders =
        FluentIterable.from(newState.columns).transform(Column::toString).toArray(String.class);
    TableModel<String> tableModel = new TableModel<>(columnHeaders);
    BigDecimal cumulativeAmount = BigDecimal.ZERO;
    for (Transaction transaction : newState.transactions) {
      for (Split split : transaction.getSplitList()) {
        if (split.getAccountId().equals(newState.account.getId())) {
          // Don't show the portion of the split for this account. We're interested
          // in seeing where it came from.
          continue;
        }
        BigDecimal amount = split.getAmount().negate();
        cumulativeAmount = cumulativeAmount.add(amount);
        tableModel.addRow(
            ImmutableList.of(
                transaction
                    .getPostDate()
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ISO_LOCAL_DATE),
                transaction.getDescription(),
                newState.accountNamesById.get(split.getAccountId()),
                amount.toString(),
                cumulativeAmount.toString()));
      }
    }
    this.table.setTableModel(tableModel);
    if (tableModel.getRowCount() == 0) {
      componentObservable.onNext(Optional.empty());
    } else {
      componentObservable.onNext(Optional.of(container));
    }
  }

  public enum Column {
    DATE,
    DESCRIPTION,
    ACCOUNT,
    AMOUNT,
    BALANCE
  }

  public static class UIState {
    private final ImmutableList<Column> columns = ImmutableList.copyOf(Column.values());
    private final Account account;
    private final ImmutableMap<String, String> accountNamesById;
    private final ImmutableList<Transaction> transactions;

    public UIState(
        Account account,
        ImmutableMap<String, String> accountNamesById,
        ImmutableList<Transaction> transactions) {
      this.account = account;
      this.accountNamesById = accountNamesById;
      this.transactions = transactions;
    }
  }
}
