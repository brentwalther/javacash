package net.brentwalther.javacash.lanterna;

import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.Border;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;
import net.brentwalther.javacash.model.Account;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class AccountList implements Supplier<Component> {

  private final Container container;
  private final ActionListBox accountList;
  private final Consumer<Account> onAccountSelected;

  public AccountList(
      Container container, ActionListBox accountList, Consumer<Account> onAccountSelected) {
    this.container = container;
    this.accountList = accountList;
    this.onAccountSelected = onAccountSelected;
  }

  public static AccountList create(Consumer<Account> onAccountSelectedConsumer) {
    Border container = Borders.singleLine("Accounts");
    ActionListBox accountsList = new ActionListBox();

    Panel contentPanel = new Panel(new LinearLayout(Direction.VERTICAL));
    contentPanel.addComponent(accountsList);

    container.setComponent(contentPanel);
    return new AccountList(container, accountsList, onAccountSelectedConsumer);
  }

  public void setUiState(UIState state) {
    this.accountList.clearItems();
    int selectedIndex = 0;
    if (state.grandParent != Account.NONE) {
      this.accountList.addItem(
          ".. Up",
          () -> {
            this.onAccountSelected.accept(state.grandParent);
          });
      selectedIndex = 1;
    }
    this.accountList.addItem(state.parent.getName(), () -> {});
    for (Account account : state.children) {
      this.accountList.addItem(
          "└─>" + account.getName(),
          () -> {
            this.onAccountSelected.accept(account);
          });
    }
    this.accountList.setSelectedIndex(selectedIndex);
  }

  @Override
  public Component get() {
    return this.container;
  }

  public static class UIState {
    public final Account grandParent;
    public final Account parent;
    public final Iterable<Account> children;

    public UIState(Account grandParent, Account parent, Iterable<Account> children) {
      this.grandParent = grandParent;
      this.parent = parent;
      this.children = children;
    }
  }
}
