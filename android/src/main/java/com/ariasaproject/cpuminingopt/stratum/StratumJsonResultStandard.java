package com.ariasaproject.cpuminingopt.stratum;

import com.fasterxml.jackson.databind.JsonNode;

/** Created by Ben David on 01/08/2017. */
public class StratumJsonResultStandard extends StratumJsonResult {
  public static final String TEST_PATT =
      "{\"error\": null, \"jsonrpc\": \"2.0\", \"id\": 2, \"result\": true}";
  public final boolean result;

  public StratumJsonResultStandard(JsonNode i_json_node) throws RuntimeException {
    super(i_json_node);
    this.result = i_json_node.get("result").asBoolean();
    return;
  }
}