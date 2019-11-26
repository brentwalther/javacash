package net.brentwalther.javacash.convert;

public enum Field {
  GUID("guid"),
  NAME("name"),
  TYPE("account_type"),
  PARENT("parent_guid"),
  DESCRIPTION("description"),
  MNEMONIC("mnemonic"),
  FULL_NAME("fullname"),
  TRANSACTION_GUID("tx_guid"),
  ACCOUNT_GUID("account_guid"),
  VALUE_NUMERATOR("value_num"),
  VALUE_DENOMINATOR("value_denom"),
  POST_DATE("post_date"),
  CURRENCY_GUID("currency_guid");

  private final String columnName;

  Field(String columnName) {
    this.columnName = columnName;
  }

  public String getColumnName() {
    return columnName;
  }
}
