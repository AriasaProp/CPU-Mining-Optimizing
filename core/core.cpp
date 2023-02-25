#include "core.h"
#include "utils/hex.h"
#include "console.h"

#include <chrono>
#include <condition_variable>
#include <cstdio>
#include <cstring>
#include <iostream>
#include <map>
#include <mutex>
#include <string>
#include <thread>
#include <vector>

#include "pass_function_set.h"
#include "json/json.h"

#define MININGREQ_CREATE 1
#define MININGREQ_DESTROY 2

std::thread mining_thread;
std::mutex mining_mtx;
std::condition_variable mining_cv;
unsigned int mining_req;
char *mining_host;
unsigned short mining_port;
char *mining_user;
char *mining_pass;

// mining data collect from subscribe
std::string mining_sesion_id = "kosong";
// std::string mining_difficulty;
std::string mining_xnonce1;
unsigned int mining_xnonce2_size;
// mining data collect ex
double mining_cur_difficulty;
// mining data collect from notify
std::string mining_job_id;
std::string mining_version;
std::vector<std::string> mining_merkle_root;
std::string mining_ntime;
std::string mining_nbit;
bool mining_clean;
std::string mining_prev_hash;
std::string mining_coinb1;
std::string mining_coinb2;

void miningThread();

void core::startMining(const char *host, const unsigned short port,
                       const char *user, const char *pass) {
  mining_mtx.lock();
  unsigned int l = strlen(host);
  mining_host = new char[l + 1];
  memcpy(mining_host, host, l);
  mining_host[l] = '\0';
  mining_port = port;
  l = strlen(user);
  mining_user = new char[l + 1];
  memcpy(mining_user, user, l);
  mining_user[l] = '\0';
  l = strlen(pass);
  mining_pass = new char[l + 1];
  memcpy(mining_pass, pass, l);
  mining_pass[l] = '\0';
  mining_mtx.unlock();
  mining_thread = std::thread(miningThread);
  mining_thread.detach();
}

void core::stopMining() {
  std::unique_lock<std::mutex> lck(mining_mtx);
  mining_req |= MININGREQ_DESTROY;
  mining_cv.wait(lck, []() -> bool { return mining_req == 0; });
  delete[] mining_host;
  mining_host = nullptr;
  delete[] mining_user;
  mining_user = nullptr;
  delete[] mining_pass;
  mining_pass = nullptr;
}

static inline void dataLoadOut(json::jobject &dat) {
  std::string mth = dat["method"];
  if (mth == "mining.notify") {
    json::jobject::proxy j_params = dat["params"];
    mining_job_id = (std::string)j_params.array(0);
    console::write(1, mining_job_id.c_str());
    mining_prev_hash = (std::string)j_params.array(1);
    console::write(1, mining_prev_hash.c_str());
    mining_coinb1 = (std::string)j_params.array(2);
    console::write(1, mining_coinb1.c_str());
    mining_coinb2 = (std::string)j_params.array(3);
    console::write(1, mining_coinb2.c_str());
    if (!j_params.array(4).is_array()) throw "notify error";
    mining_merkle_root = (std::vector<std::string>)j_params.array(4);
    mining_version = (std::string)j_params.array(5);
    console::write(1, mining_version.c_str());
    mining_nbit = (std::string)j_params.array(6);
    console::write(1, mining_nbit.c_str());
    mining_ntime = (std::string)j_params.array(7);
    console::write(1, mining_ntime.c_str());
    mining_clean = j_params.array(8).is_true();
  } else if (mth == "client.show_message") {
    console::write(1, ((std::string)dat["params"].array(0)).c_str());
  } else if (mth == "mining.set_difficulty") {
    mining_cur_difficulty = (double)dat["params"].array(0);
  }
}
void miningThread() {
  console::write(1, "Start Mining ......");
  char _msgtemp[1024];
  try {
    const unsigned int max_trying = 5; // repeated try limit
    unsigned int i = 0;
    function_set::openConnection(mining_host, mining_port);
    json::jobject dat;
    strcpy(_msgtemp, "{\"id\": 1, \"method\": \"mining.subscribe\", \"params\": [\"Android_CPU_Test\"]}\n");
    function_set::sendMessage(_msgtemp);
    for (i = 0; i < max_trying; i++) {
      char *mC = function_set::getMessage();
      if (*mC == '\0')
        continue;
      for (mC = strtok(mC, "\n"); mC; mC = strtok(nullptr, "\n")) {
        if (!json::jobject::tryparse(mC, dat))
          continue;
        if (dat.get("id") == "1") {
          if (!dat["error"].is_null())
            throw dat.get("error").c_str();
          json::jobject::proxy j_result = dat["result"];
          if (j_result.array(0).array(0).array(0).as_string() != "mining.notify") throw "no notify";
          mining_sesion_id = (std::string)j_result.array(0).array(0).array(1);
          if (j_result.array(0).array(1).array(0).as_string() != "mining.set_difficulty") throw "no difficulty";
          mining_cur_difficulty = (double)hex(((std::string)j_result.array(0).array(1).array(1)).c_str());
          mining_xnonce1 = (std::string)j_result.array(1);
          mining_xnonce2_size = (int)j_result.array(2);
        } else {
          dataLoadOut(dat);
        }
      }
      dat.clear();
      break;
    }
    sprintf(_msgtemp, "Id: %s,n1: %s,n2: %u", mining_sesion_id.c_str(),
            mining_xnonce1.c_str(), mining_xnonce2_size);
    console::write(0, _msgtemp);
    if (i >= max_trying)
      throw "No received message after subscribe";
    sprintf(_msgtemp,
            "{\"id\": 2,\"method\": \"mining.authorize\",\"params\": [\"%s\",\"%s\"]}",
            mining_user, mining_pass);
    function_set::sendMessage(_msgtemp);
    for (i = 0; i < max_trying; i++) {
      char *mC = function_set::getMessage();
      if (*mC == '\0')
        continue;
      for (mC = strtok(mC, "\n"); mC; mC = strtok(nullptr, "\n")) {
        if (!json::jobject::tryparse(mC, dat))
          continue;
        if (dat.get("id") == "2") {
          if (!dat["error"].is_null())
            throw dat.get("error").c_str();
          if (!dat["result"].is_true())
            throw "false authentications";
        } else {
          dataLoadOut(dat);
        }
      }
      break;
    }
    dat.clear();
    if (i >= max_trying)
      throw "No received message after authorize";
    function_set::afterStart();
    bool running = false;
    while (running) {
      // console::write(0, function_set::recvConnection());
      std::this_thread::sleep_for(std::chrono::seconds(1));
      mining_mtx.lock();
      if (mining_req) {
        if (mining_req & MININGREQ_DESTROY) {
          running = false;
        }
        std::this_thread::sleep_for(std::chrono::milliseconds(100));
        mining_req = 0;
        mining_cv.notify_all();
      }
      mining_mtx.unlock();
    }
  } catch (const char *exceptionMsg) {
    console::write(4, exceptionMsg);
  }
  console::write(1, "End Mining...");
  function_set::closeConnection();
  function_set::afterStop();
}
