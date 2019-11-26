package net.brentwalther.javacash.convert.api;

import net.brentwalther.javacash.model.JavaCashModel;

public interface CashModelDataExtractor<S extends Enum> {

  /**
   * Returns the CashModel data that was extracted from some other data source or an enum Status
   * code if an error occurred.
   */
  JavaCashModel extract();
}
