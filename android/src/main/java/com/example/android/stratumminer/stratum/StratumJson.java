package com.example.android.stratumminer.stratum;

import com.example.android.stratumminer.HexArray;

public class StratumJson {
  protected StratumJson() {
    return;
  }
  protected StratumJson(StratumJson i_src) {
    return;
  }
  protected static HexArray toHexArray(String i_str, int i_str_len) throws RuntimeException {
    if (i_str.length() != i_str_len) {
      throw new RuntimeException();
    }
    return new HexArray(i_str);
  }
}
