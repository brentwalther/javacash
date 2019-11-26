package net.brentwalther.javacash.model;

import com.google.common.collect.ImmutableList;

import java.time.Instant;
import java.util.List;

public class Transaction {
  private final String id;
  private final String commodityId;
  private final List<Split> splitList;
  private final Instant postDate;
  private final String description;

  public Transaction(
      String id,
      String commodityId,
      ImmutableList<Split> build,
      Instant postDate,
      String description) {
    this.id = id;
    this.commodityId = commodityId;
    this.splitList = build;
    this.postDate = postDate;
    this.description = description;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public String getId() {
    return this.id;
  }

  public String getCommodityId() {
    return this.commodityId;
  }

  public List<Split> getSplitList() {
    return this.splitList;
  }

  public Instant getPostDate() {
    return this.postDate;
  }

  public String getDescription() {
    return this.description;
  }

  public static class Builder {
    private String id = "";
    private String commodityId = "";
    private ImmutableList.Builder<Split> splitListBuilder = ImmutableList.builder();
    private Instant postDate = Instant.EPOCH;
    private String description = "";

    public Builder setId(String id) {
      this.id = id;
      return this;
    }

    public Builder setCommodityId(String commodityId) {
      this.commodityId = commodityId;
      return this;
    }

    public Builder addSplit(Split split) {
      this.splitListBuilder.add(split);
      return this;
    }

    public Builder addAllSplit(Iterable<Split> split) {
      this.splitListBuilder.addAll(split);
      return this;
    }

    public Builder setPostDate(Instant postDate) {
      this.postDate = postDate;
      return this;
    }

    public Builder setDescription(String description) {
      this.description = description;
      return this;
    }

    public Transaction build() {
      return new Transaction(
          this.id,
          this.commodityId,
          this.splitListBuilder.build(),
          this.postDate,
          this.description);
    }
  }
}
