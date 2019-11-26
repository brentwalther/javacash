package net.brentwalther.javacash;

import com.google.devtools.common.options.Option;
import com.google.devtools.common.options.OptionsBase;

public class Options extends OptionsBase {
  @Option(
      category = "default",
      name = "database-path",
      abbrev = 'p',
      help = "Path account the sqlite database account open.",
      defaultValue = "")
  public String databasePath;
}
