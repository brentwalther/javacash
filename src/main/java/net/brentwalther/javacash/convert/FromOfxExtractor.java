package net.brentwalther.javacash.convert;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.webcohesion.ofx4j.domain.data.ResponseEnvelope;
import com.webcohesion.ofx4j.domain.data.ResponseMessageSet;
import com.webcohesion.ofx4j.domain.data.creditcard.CreditCardAccountDetails;
import com.webcohesion.ofx4j.domain.data.creditcard.CreditCardResponseMessageSet;
import com.webcohesion.ofx4j.domain.data.creditcard.CreditCardStatementResponseTransaction;
import com.webcohesion.ofx4j.io.AggregateUnmarshaller;
import com.webcohesion.ofx4j.io.OFXHandler;
import com.webcohesion.ofx4j.io.OFXParseException;
import com.webcohesion.ofx4j.io.OFXReader;
import com.webcohesion.ofx4j.io.OFXSyntaxException;
import com.webcohesion.ofx4j.io.nanoxml.NanoXMLOFXReader;
import net.brentwalther.javacash.convert.api.CashModelDataExtractor;
import net.brentwalther.javacash.model.Account;
import net.brentwalther.javacash.model.Commodity;
import net.brentwalther.javacash.model.JavaCashModel;
import net.brentwalther.javacash.model.Split;
import net.brentwalther.javacash.model.Transaction;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;

public class FromOfxExtractor implements CashModelDataExtractor<FromOfxExtractor.Error> {

  private final File ofxFile;

  public FromOfxExtractor(File ofxFile) {
    this.ofxFile = ofxFile;
  }

  public static FromOfxExtractor create(File ofxFile) {
    return new FromOfxExtractor(ofxFile);
  }

  @Override
  public JavaCashModel extract() {
    if (!this.ofxFile.exists() || !this.ofxFile.isFile()) {
      return JavaCashModel.empty();
    }

    OFXReader ofxReader = new NanoXMLOFXReader();
    AggregateUnmarshaller<ResponseEnvelope> transactionList =
        new AggregateUnmarshaller<>(ResponseEnvelope.class);
    ofxReader.setContentHandler(
        new OFXHandler() {
          @Override
          public void onHeader(String s, String s1) throws OFXSyntaxException {
            System.out.format("h: %s %s\n", s, s1);
          }

          @Override
          public void onElement(String s, String s1) throws OFXSyntaxException {
            System.out.format("e: %s %s\n", s, s1);
          }

          @Override
          public void startAggregate(String s) throws OFXSyntaxException {
            System.out.format("....... %s\n", s);
          }

          @Override
          public void endAggregate(String s) throws OFXSyntaxException {
            System.out.format("^^^^^ %s\n", s);
          }
        });
    try {
      ResponseEnvelope response = transactionList.unmarshal(new FileInputStream(this.ofxFile));
      System.out.format("UID %s\n", response.getUID());

      ImmutableMap.Builder<String, Account> accounts = ImmutableMap.builder();
      ImmutableMap.Builder<String, Transaction> transactions = ImmutableMap.builder();
      for (ResponseMessageSet set : response.getMessageSets()) {
        switch (set.getType()) {
          case creditcard:
            CreditCardResponseMessageSet creditCardResponse = (CreditCardResponseMessageSet) set;
            for (CreditCardStatementResponseTransaction t :
                creditCardResponse.getStatementResponses()) {
              CreditCardAccountDetails creditCardAccountDetails = t.getMessage().getAccount();
              Account account =
                  Account.newBuilder()
                      .setId(creditCardAccountDetails.getAccountNumber())
                      .setName(creditCardAccountDetails.getAccountNumber())
                      //                      .setType(Account.Type.CREDIT)
                      .build();
              accounts.put(account.getId(), account);
              for (com.webcohesion.ofx4j.domain.data.common.Transaction t2 :
                  t.getMessage().getTransactionList().getTransactions()) {
                BigDecimal amount = t2.getBigDecimalAmount();
                Transaction transaction =
                    Transaction.newBuilder()
                        .setId(t2.getId())
                        .setCommodityId(Commodity.UNKNOWN.getId())
                        .addSplit(
                            Split.newBuilder()
                                .setAccountId(account.getId())
                                .setAmount(amount)
                                .build())
                        .setPostDate(t2.getDatePosted().toInstant())
                        .setDescription(t2.getMemo())
                        .build();
                transactions.put(transaction.getId(), transaction);
              }
            }
            break;
          default:
            System.out.format("No OFX handler for OFX data type %s\n", set.getType());
            break;
        }
      }
      return JavaCashModel.create(
          accounts.build().values(), ImmutableList.of(), transactions.build().values());
    } catch (IOException e) {
      return JavaCashModel.empty();
    } catch (OFXParseException e) {
      e.printStackTrace();
    }

    return JavaCashModel.empty();
  }

  public enum Error {
    UNKNOWN("Unknown error."),
    INVALID_FILE("The selected file is not a valid OFX file."),
    IO_EXCEPTION("An I/O exception occurred while reading the OFX file.");

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
