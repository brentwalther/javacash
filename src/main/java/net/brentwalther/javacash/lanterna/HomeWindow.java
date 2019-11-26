package net.brentwalther.javacash.lanterna;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.googlecode.lanterna.gui2.AbstractWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Panel;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import net.brentwalther.javacash.model.Account;
import net.brentwalther.javacash.model.JavaCashModel;
import net.brentwalther.javacash.model.Transaction;

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
    Observable.combineLatest(
            modelObservable,
            selectedAccountSubject,
            (model, selectedAccount) -> {
              Iterable<Account> accounts = model.getAccounts();

              return new AccountList.UIState(
                  /* grandParent= */ FluentIterable.from(accounts)
                      .firstMatch(
                          (account) -> account.getId().equals(selectedAccount.getParentAccountId()))
                      .or(Account.NONE),
                  /* parent= */ selectedAccount,
                  FluentIterable.from(accounts)
                      .filter(
                          account -> account.getParentAccountId().equals(selectedAccount.getId()))
                      .toSortedList(Ordering.natural().onResultOf(Account::getName)));
            })
        .subscribe(accountList::setUiState);

    TransactionTable transactions = TransactionTable.create();
    Observable.combineLatest(
            modelObservable,
            selectedAccountSubject,
            (model, selectedAccount) -> {
              return new TransactionTable.UiState(
                  FluentIterable.from(model.getTransactions())
                      .filter(
                          (transaction) ->
                              Iterables.any(
                                  transaction.getSplitList(),
                                  (split) -> split.getAccountId().equals(selectedAccount.getId())))
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
