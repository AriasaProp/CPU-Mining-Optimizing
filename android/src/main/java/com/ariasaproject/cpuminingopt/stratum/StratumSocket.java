package com.ariasaproject.cpuminingopt.stratum;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;

public class StratumSocket extends Socket {
  private BufferedWriter _tx;
  private BufferedReader _rx;
  private int _id;

  protected StratumSocket() {}

  public StratumSocket(URI i_url) throws UnknownHostException, IOException {
    super(i_url.getHost(), i_url.getPort());
    this._tx = new BufferedWriter(new OutputStreamWriter(getOutputStream()));
    this._rx = new BufferedReader(new InputStreamReader(getInputStream()));
    this._id = 1;
  }

  public synchronized long subscribe(String i_agent_name) throws IOException {
    long id = this._id;
    this._id++;
    this._tx.write("{\"id\":" + id + ",\"method\":\"mining.subscribe\",\"params\":[]}\n");
    this._tx.flush();
    return id;
  }

  public synchronized long authorize(String i_user, String i_password) throws IOException {
    long id = this._id;
    this._id++;
    this._tx.write(
        String.format(
            "{\"id\":" + id + ",\"method\":\"mining.authorize\",\"params\":[\"%s\",\"%s\"]}\n",
            i_user,
            i_password));
    this._tx.flush();
    return id;
  }

  public synchronized long submit(
      int i_nonce, String i_user, String i_jobid, String i_nonce2, String i_ntime)
      throws IOException {
    long id = this._id;
    this._id++;
    String sn =
        String.format(
            "%08x",
            (((i_nonce & 0xff000000) >> 24)
                | ((i_nonce & 0x00ff0000) >> 8)
                | ((i_nonce & 0x0000ff00) << 8)
                | ((i_nonce & 0x000000ff) << 24)));
    this._tx.write(
        String.format(
            "{\"id\":"
                + id
                + ",\"method\":\"mining.submit\",\"params\":[\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"]}\n",
            i_user,
            i_jobid,
            i_nonce2,
            i_ntime,
            sn));
    this._tx.flush();
    return id;
  }

  public StratumJson recvStratumJson() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jn = mapper.readTree(this._rx.readLine());
    try {
      return new StratumJsonMethodGetVersion(jn);
    } catch (RuntimeException e) {
    }
    try {
      return new StratumJsonMethodMiningNotify(jn);
    } catch (RuntimeException e) {
    }
    try {
      return new StratumJsonMethodReconnect(jn);
    } catch (RuntimeException e) {
    }
    try {
      return new StratumJsonMethodSetDifficulty(jn);
    } catch (RuntimeException e) {
    }
    try {
      return new StratumJsonMethodShowMessage(jn);
    } catch (RuntimeException e) {
    }
    try {
      return new StratumJsonResultSubscribe(jn);
    } catch (RuntimeException e) {
    }
    try {
      return new StratumJsonResultStandard(jn);
    } catch (RuntimeException e) {
    }
    return null;
  }
}
