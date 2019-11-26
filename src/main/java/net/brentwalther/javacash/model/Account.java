package net.brentwalther.javacash.model;

public class Account {
  public static final Account NONE = Account.newBuilder().build();
  private final String id;
  private final String name;
  private final String type;
  private final String parentAccountId;

  private Account(String id, String name, String type, String parentAccountId) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.parentAccountId = parentAccountId;
  }

  public static Account.Builder newBuilder() {
    return new Builder();
  }

  public String getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public String getType() {
    return this.type;
  }

  public String getParentAccountId() {
    return this.parentAccountId;
  }

  public enum Type {
    CREDIT;
  }

  public static class Builder {
    private String id = "";
    private String name = "";
    private String type = "";
    private String parentAccountId = "";

    public Builder setId(String id) {
      this.id = id;
      return this;
    }

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Builder setType(String type) {
      this.type = type;
      return this;
    }

    public Builder setParentAccountId(String id) {
      this.parentAccountId = id;
      return this;
    }

    public Account build() {
      return new Account(this.id, this.name, this.type, this.parentAccountId);
    }
  }
}
