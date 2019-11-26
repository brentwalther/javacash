package net.brentwalther.javacash.model;

public class Commodity {
  public static final Commodity UNKNOWN = Commodity.newBuilder().build();

  private final String id;
  private final String mnemonic;
  private final String name;

  public Commodity(String id, String mnemonic, String name) {
    this.id = id;
    this.mnemonic = mnemonic;
    this.name = name;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public String getId() {
    return this.id;
  }

  public String getMnemonic() {
    return this.mnemonic;
  }

  public String getName() {
    return this.name;
  }

  public static class Builder {
    private String id = "Unknown";
    private String mnemonic = "UNK";
    private String name = "Unknown";

    public Builder setId(String id) {
      this.id = id;
      return this;
    }

    public Builder setMnemonic(String mnemonic) {
      this.mnemonic = mnemonic;
      return this;
    }

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Commodity build() {
      return new Commodity(this.id, this.mnemonic, this.name);
    }
  }
}
