package net.brentwalther;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.devtools.common.options.OptionsParser;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.FileDialog;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import io.reactivex.rxjava3.core.Observable;
import net.brentwalther.javacash.Options;
import net.brentwalther.javacash.convert.FromGnuCashSqliteDbExtractor;
import net.brentwalther.javacash.lanterna.HomeWindow;
import net.brentwalther.javacash.model.Account;
import net.brentwalther.javacash.model.JavaCashModel;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

public class App {
  public static void main(String[] args) throws IOException {
    OptionsParser parser = OptionsParser.newOptionsParser(Options.class);
    parser.parseAndExitUponError(args);
    Options options = parser.getOptions(Options.class);

    DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
    Screen screen = defaultTerminalFactory.createScreen();
    screen.startScreen();

    WindowBasedTextGUI gui = new MultiWindowTextGUI(screen);

    Optional<File> databaseFileOptional = Optional.empty();
    if (!Strings.isNullOrEmpty(options.databasePath)) {
      databaseFileOptional = Optional.of(new File(options.databasePath));
    } else {
      databaseFileOptional =
          Optional.of(
              new FileDialog(
                      "Select a database.",
                      "Select a GnuCash sqlite database.",
                      "Open",
                      screen.getTerminalSize(),
                      /* showHiddenFilesAndDirs= */ false,
                      new File(System.getProperty("user.dir")))
                  .showDialog(gui));
    }

    File databaseFile = databaseFileOptional.get();
    if (!databaseFile.exists()) {
      System.out.format(
          "Could not open database (%s). File does not exist!", databaseFile.getAbsolutePath());
      System.exit(1);
    }

    FromGnuCashSqliteDbExtractor fromGnuCashSqliteDbExtractor =
        new FromGnuCashSqliteDbExtractor(databaseFile);
    JavaCashModel extractedData = fromGnuCashSqliteDbExtractor.extract();

    gui.addWindowAndWait(
        HomeWindow.create(Observable.just(extractedData), findRootAccount(extractedData)));
  }

  private static Account findRootAccount(JavaCashModel extractedData) {
    Map<String, Account> accountsById =
        Maps.uniqueIndex(extractedData.getAccounts(), Account::getId);
    Multiset<Account> rootCandidates = HashMultiset.create();
    for (Account account : extractedData.getAccounts()) {
      while (!account.getParentAccountId().isEmpty()) {
        account = accountsById.get(account.getParentAccountId());
      }
      rootCandidates.add(account);
    }
    return Collections.max(
            rootCandidates.entrySet(), Comparator.comparingInt(Multiset.Entry::getCount))
        .getElement();
  }
}
