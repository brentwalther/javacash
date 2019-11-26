package net.brentwalther.javacash.model;

import com.google.common.collect.ImmutableList;

import java.util.Collection;

public class JavaCashModel {
  private final Collection<Account> accounts;
  private final Collection<Commodity> commodities;
  private final Collection<Transaction> transactions;

  public JavaCashModel(
      Collection<Account> accounts,
      Collection<Commodity> commodities,
      Collection<Transaction> transactions) {
    this.accounts = accounts;
    this.commodities = commodities;
    this.transactions = transactions;
  }

  public static JavaCashModel create(
      Collection<Account> accounts,
      Collection<Commodity> commodities,
      Collection<Transaction> transactions) {
    return new JavaCashModel(accounts, commodities, transactions);
  }

  public static JavaCashModel empty() {
    return create(ImmutableList.of(), ImmutableList.of(), ImmutableList.of());
  }

  public Collection<Account> getAccounts() {
    return this.accounts;
  }

  public Collection<Commodity> getCommodities() {
    return this.commodities;
  }

  public Collection<Transaction> getTransactions() {
    return this.transactions;
  }
}
