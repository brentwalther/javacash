package net.brentwalther.javacash.lanterna;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.googlecode.lanterna.gui2.AbstractWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Panel;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import net.brentwalther.javacash.model.Account;
import net.brentwalther.javacash.model.JavaCashModel;
import net.brentwalther.javacash.model.Transaction;

import java.math.BigDecimal;
import java.util.HashMap;

public class HomeWindow extends AbstractWindow {

  private HomeWindow() {
    super("Home");
  }

  public static HomeWindow create(Observable<JavaCashModel> modelObservable, Account rootAccount) {
    HomeWindow window = new HomeWindow();
    window.setHints(ImmutableSet.of(Hint.FIT_TERMINAL_WINDOW));

    ReplaySubject<Account> selectedAccountSubject = ReplaySubject.createWithSize(1);
    selectedAccountSubject.onNext(rootAccount);

    AccountList accountList = AccountList.create(selectedAccountSubject::onNext);
    modelObservable
        .map(
            model -> {
              ImmutableMap<String, BigDecimal> accountBalances =
                  ImmutableMap.copyOf(
                      model.getTransactions().stream()
                          .flatMap(t -> t.getSplitList().stream())
                          .collect(
                              () -> new HashMap<>(),
                              (map, split) ->
                                  map.put(
                                      split.getAccountId(),
                                      map.getOrDefault(split.getAccountId(), BigDecimal.ZERO)
                                          .add(split.getAmount())),
                              (map1, map2) -> {
                                for (String accountId : map1.keySet()) {
                                  map2.put(
                                      accountId, map2.getOrDefault(accountId, BigDecimal.ZERO));
                                }
                              }));
              return new AccountList.UIState(
                  rootAccount,
                  Multimaps.index(model.getAccounts(), Account::getParentAccountId),
                  accountBalances);
            })
        .subscribe(accountList::setUiState);

    TransactionTable transactions = TransactionTable.create();
    Observable.combineLatest(
            modelObservable,
            selectedAccountSubject,
            (model, selectedAccount) -> {
              return new TransactionTable.UiState(
                  selectedAccount,
                  ImmutableMap.copyOf(
                      FluentIterable.from(model.getAccounts()).transform(account -> Maps.immutableEntry(account.getId(), account.getName()))),
                  FluentIterable.from(model.getTransactions())
                      .filter(
                          transaction ->
                              transaction.getSplitList().stream()
                                  .anyMatch(
                                      split ->
                                          split.getAccountId().equals(selectedAccount.getId())))
                      .toSortedList(Ordering.natural().onResultOf(Transaction::getPostDate)));
            })
        .subscribe(transactions::setUiState);

    Panel content = new Panel(new BorderLayout());
    content.addComponent(accountList.get(), BorderLayout.Location.LEFT);
    content.addComponent(transactions.get(), BorderLayout.Location.CENTER);
    window.setComponent(content);
    return window;
  }
}
