package com.ariasaproject.cpuminingopt.stratum;

import com.fasterxml.jackson.databind.JsonNode;

public class StratumJsonMethodShowMessage extends StratumJsonMethod {
  // {"method":"client.reconnect",params:["test",1]}
  public static final String TEST_PATT =
      "{\"params\": [\"TEST\"], \"jsonrpc\": \"2.0\", \"method\": \"client.show_message\", \"id\":"
          + " null}";
  public final String val;

  public StratumJsonMethodShowMessage(JsonNode i_json_node) throws RuntimeException {
    super(i_json_node);
    String s = i_json_node.get("method").asText();
    if (s.compareTo("client.show_message") != 0) {
      throw new RuntimeException();
    }
    this.val = i_json_node.get("params").asText();
    return;
  }
}
