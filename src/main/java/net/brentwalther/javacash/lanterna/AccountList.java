package net.brentwalther.javacash.lanterna;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.Border;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;
import net.brentwalther.javacash.model.Account;
import net.brentwalther.javacash.util.StringUtil;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AccountList implements Supplier<Component> {

  private static final String RIGHT_ARROW = "\u2192";
  private static final String BOTTOM_ARROW = "\u2193";

  private final Container container;
  private final ActionListBox accountList;
  private final Consumer<Account> onAccountSelected;
  private final Set<Account> expandedAccounts = new HashSet<>();
  private Account lastSelectedAccount = Account.NONE;
  private UIState uiState = new UIState(Account.NONE, ImmutableMultimap.of(), ImmutableMap.of());

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
    this.uiState = state;
    redraw();
  }

  public void redraw() {
    this.accountList.clearItems();
    for (Account topLevelAccount : uiState.childrenByParentId.get(uiState.rootAccount.getId())) {
      preOrderTraversal(0, topLevelAccount, uiState.childrenByParentId);
    }
  }

  private void preOrderTraversal(
      int depth, Account account, Multimap<String, Account> childrenByParentId) {
    boolean hasChildren = childrenByParentId.containsKey(account.getId());
    boolean isExpanded = expandedAccounts.contains(account);
    String prefix =
        StringUtil.repeatString(" ", depth)
            + (hasChildren ? (isExpanded ? BOTTOM_ARROW : RIGHT_ARROW) : " ");
    String suffix =
        uiState.accountBalances.containsKey(account.getId())
            ? " (" + uiState.accountBalances.get(account.getId()) + ")"
            : "";
    String label = prefix + account.getName() + suffix;
    accountList.addItem(label, () -> onAccountSelected(account));
    if (account == lastSelectedAccount) {
      accountList.setSelectedIndex(accountList.getItems().size() - 1);
    }
    if (isExpanded) {
      List<Account> sortedChildren =
          ImmutableList.sortedCopyOf(
              Ordering.natural().onResultOf(Account::getName),
              childrenByParentId.get(account.getId()));
      for (Account child : sortedChildren) {
        preOrderTraversal(depth + 1, child, childrenByParentId);
      }
    }
  }

  private void onAccountSelected(Account account) {
    if (expandedAccounts.contains(account)) {
      expandedAccounts.remove(account);
    } else {
      expandedAccounts.add(account);
    }
    lastSelectedAccount = account;
    redraw();
    onAccountSelected.accept(account);
  }

  @Override
  public Component get() {
    return this.container;
  }

  public static class UIState {

    private final Account rootAccount;
    private final ImmutableMultimap<String, Account> childrenByParentId;
    private final ImmutableMap<String, BigDecimal> accountBalances;

    public UIState(
        Account rootAccount,
        ImmutableMultimap<String, Account> childrenByParentId,
        ImmutableMap<String, BigDecimal> accountBalances) {
      this.rootAccount = rootAccount;
      this.childrenByParentId = childrenByParentId;
      this.accountBalances = accountBalances;
    }
  }
}
