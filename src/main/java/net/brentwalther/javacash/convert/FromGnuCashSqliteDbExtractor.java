package net.brentwalther.javacash.convert;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import net.brentwalther.javacash.convert.api.CashModelDataExtractor;
import net.brentwalther.javacash.model.Account;
import net.brentwalther.javacash.model.Commodity;
import net.brentwalther.javacash.model.JavaCashModel;
import net.brentwalther.javacash.model.Split;
import net.brentwalther.javacash.model.Transaction;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class FromGnuCashSqliteDbExtractor
    implements CashModelDataExtractor<FromGnuCashSqliteDbExtractor.Error> {

  private final File sqliteDatabase;

  public FromGnuCashSqliteDbExtractor(File sqliteDatabase) {
    this.sqliteDatabase = sqliteDatabase;
  }

  @Override
  public JavaCashModel extract() {
    Connection connection = null;
    try {
      // create a database connection
      connection =
          DriverManager.getConnection("jdbc:sqlite:" + this.sqliteDatabase.getAbsolutePath());
      Statement statement = connection.createStatement();
      statement.setQueryTimeout(30); // set timeout account 30 sec.

      ResultSet accountResults = statement.executeQuery("select * from accounts");
      ResultSetMatcher accountMatcher =
          new ResultSetMatcher(
              ImmutableList.of(
                  Field.GUID, Field.NAME, Field.TYPE, Field.PARENT, Field.DESCRIPTION));
      Map<String, Account> accountsByGuid = new HashMap<>();
      if (accountMatcher.matches(accountResults.getMetaData())) {
        for (ImmutableMap<Field, String> result : accountMatcher.getResults(accountResults)) {
          Account account =
              Account.newBuilder()
                  .setId(result.get(Field.GUID))
                  .setName(result.get(Field.NAME))
                  .setType(result.get(Field.TYPE))
                  .setParentAccountId(result.get(Field.PARENT))
                  .build();
          //          if (account.isValid()) {
          accountsByGuid.put(result.get(Field.GUID), account);
          //          }
        }
      } else {
        System.err.println("Could not initialize accounts. Matcher did not match.");
      }

      ResultSet commodityResults = statement.executeQuery("select * from commodities");
      ResultSetMatcher commodityMatcher =
          new ResultSetMatcher(ImmutableList.of(Field.GUID, Field.FULL_NAME, Field.MNEMONIC));
      Map<String, Commodity> commodityByGuid = new HashMap<>();
      if (commodityMatcher.matches(commodityResults.getMetaData())) {
        for (ImmutableMap<Field, String> result : commodityMatcher.getResults(commodityResults)) {
          commodityByGuid.put(
              result.get(Field.GUID),
              Commodity.newBuilder()
                  .setId(result.get(Field.GUID))
                  .setMnemonic(result.get(Field.MNEMONIC))
                  .setName(result.get(Field.FULL_NAME))
                  .build());
        }
      } else {
        System.err.println("Could not initialize commodities. Matcher did not match.");
      }

      ResultSet splitsResults = statement.executeQuery("select * from splits");
      ResultSetMatcher splitsMatcher =
          new ResultSetMatcher(
              ImmutableList.of(
                  Field.TRANSACTION_GUID,
                  Field.ACCOUNT_GUID,
                  Field.VALUE_NUMERATOR,
                  Field.VALUE_DENOMINATOR));
      Multimap<String, Split> splitsByTransactionGuid = ArrayListMultimap.create();
      if (splitsMatcher.matches(splitsResults.getMetaData())) {
        for (ImmutableMap<Field, String> result : splitsMatcher.getResults(splitsResults)) {
          int scale = 0;
          long denominator = Long.parseLong(result.get(Field.VALUE_DENOMINATOR));
          while (denominator >= 10) {
            scale++;
            denominator /= 10;
          }
          splitsByTransactionGuid.put(
              result.get(Field.TRANSACTION_GUID),
              Split.newBuilder()
                  .setAccountId(accountsByGuid.get(result.get(Field.ACCOUNT_GUID)).getId())
                  .setAmount(
                      new BigDecimal(new BigInteger(result.get(Field.VALUE_NUMERATOR)), scale))
                  .build());
        }
      } else {
        System.err.println("Could not initialize splits. Matcher did not match.");
      }

      ResultSet transactionResults = statement.executeQuery("select * from transactions");
      ResultSetMatcher transactionMatcher =
          new ResultSetMatcher(
              ImmutableList.of(
                  Field.GUID, Field.CURRENCY_GUID, Field.POST_DATE, Field.DESCRIPTION));
      Map<String, Transaction> transactionsByGuid = new HashMap<>();
      if (transactionMatcher.matches(transactionResults.getMetaData())) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (ImmutableMap<Field, String> result :
            transactionMatcher.getResults(transactionResults)) {
          String transactionGuid = result.get(Field.GUID);
          transactionsByGuid.put(
              transactionGuid,
              Transaction.newBuilder()
                  .setId(transactionGuid)
                  .setCommodityId(
                      result.getOrDefault(Field.CURRENCY_GUID, Commodity.UNKNOWN.getId()))
                  .addAllSplit(splitsByTransactionGuid.get(transactionGuid))
                  .setPostDate(
                      Instant.from(
                          ZonedDateTime.of(
                              LocalDateTime.parse(result.get(Field.POST_DATE), formatter),
                              ZoneId.systemDefault())))
                  .setDescription(result.get(Field.DESCRIPTION))
                  .build());
        }
      } else {
        System.err.println("Could not initialize transactions. Matcher did not match.");
      }

      return JavaCashModel.create(
          accountsByGuid.values(), commodityByGuid.values(), transactionsByGuid.values());

    } catch (SQLException e) {
      // if the error message is "out of memory",
      // it probably means no database file is found
      System.err.println(e.getMessage());
      return JavaCashModel.empty();
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (SQLException e) {
        // connection close failed.
        System.err.println(e.getMessage());
        return JavaCashModel.empty();
      }
    }
  }

  public enum Error {
    UNKNOWN("Unknown error"),
    SQL_EXCEPTION("Unrecoverable SQL Exception");

    private final String errorMessage;

    Error(String errorMessage) {
      this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
      return this.errorMessage;
    }

    @Override
    public String toString() {
      return getErrorMessage();
    }
  }
}
