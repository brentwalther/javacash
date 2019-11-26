package net.brentwalther.javacash.model;

import java.math.BigDecimal;

public class Split {
  private final String accountId;
  private final BigDecimal amount;

  public Split(String accountId, BigDecimal amount) {
    this.accountId = accountId;
    this.amount = amount;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public String getAccountId() {
    return this.accountId;
  }

  public BigDecimal getAmount() {
    return this.amount;
  }

  public static class Builder {
    private String accountId = "";
    private BigDecimal amount = BigDecimal.ZERO;

    public Builder setAccountId(String accountId) {
      this.accountId = accountId;
      return this;
    }

    public Builder setAmount(BigDecimal amount) {
      this.amount = amount;
      return this;
    }

    public Split build() {
      return new Split(this.accountId, this.amount);
    }
  }
}
