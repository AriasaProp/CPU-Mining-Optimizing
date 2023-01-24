package com.ariasaproject.cpuminingopt.stratum;

import com.ariasaproject.cpuminingopt.HexArray;

public class StratumJson {
  protected StratumJson() {}

  protected StratumJson(StratumJson i_src) {}

  protected static HexArray toHexArray(String i_str, int i_str_len) throws RuntimeException {
    if (i_str.length() != i_str_len) {
      throw new RuntimeException();
    }
    return new HexArray(i_str);
  }
}
