#include "core.h"
#include "console.h"

#include <iostream>
#include <thread>
#include <chrono>
#include <mutex>
#include <condition_variable>
#include <cstring>
#include <cstdio>
#include <map>
#include <vector>
#include <string>

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
unsigned int mining_flags = 0;

//mining data collect from subscribe
std::string mining_sesion_id;
//std::string mining_difficulty;
std::string mining_xnonce1;
unsigned int mining_xnonce2_size;
//mining data collect ex
double mining_cur_difficulty;
//mining data collect from notify
std::string mining_job_id;
std::string mining_version;
std::string *mining_merkle_arr;
std::string mining_ntime;
std::string mining_nbit;
bool mining_clean;
std::string mining_prev_hash;
std::string mining_coinb1;
std::string mining_coinb2;


void miningThread();

void core::startMining(const char *host, const unsigned short port, const char *user, const char *pass, unsigned int flags) {
	mining_mtx.lock();
	unsigned int l = strlen(host);
	mining_host = new char[l+1];
	memcpy(mining_host, host, l);
	mining_host[l] = '\0';
	mining_port = port;
	l = strlen(user);
	mining_user = new char[l+1];
	memcpy(mining_user, user, l);
	mining_user[l] = '\0';
	l = strlen(pass);
	mining_pass = new char[l+1];
	memcpy(mining_pass, pass, l);
	mining_pass[l] = '\0';
	mining_flags = flags;
	mining_mtx.unlock();
	mining_thread = std::thread(miningThread);
	mining_thread.detach();
}

void core::stopMining() {
	std::unique_lock<std::mutex> lck(mining_mtx);
	mining_req |= MININGREQ_DESTROY;
	mining_cv.wait(lck, []()->bool{return mining_req == 0;});
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
		json::jobject::entry j_params = dat["params"];
		mining_job_id = (std::string)j_params.array(0);
		console::write(1, mining_job_id.c_str());
		mining_prev_hash = (std::string)j_params.array(1);
		console::write(1, mining_prev_hash.c_str());
		mining_coinb1 = (std::string)j_params.array(2);
		console::write(1, mining_coinb1.c_str());
		mining_coinb2 = (std::string)j_params.array(3);
		console::write(1, mining_coinb2.c_str());
		if(!(std::string)j_params.array(4).is_array()) throw "notify error";
		mining_version = (std::string)j_params.array(5);
		console::write(1, mining_version.c_str());
		mining_nbit = (std::string)j_params.array(6);
		console::write(1, mining_nbit.c_str());
		mining_ntime = (std::string)j_params.array(7);
		console::write(1, mining_ntime.c_str());
		mining_clean = j_params.array(8).is_true();
	} else if (mth == "client.show_message") {
		console::write(1, dat["params"].array(0));
	} else if (mth == "mining.set_difficulty") {
		mining_cur_difficulty = dat.get("params").array(0).as_string();
	}
}
void miningThread() {
	//create state
	console::write(1, "Start Mining ......");
	char _msgtemp[1024];
	//std::vector<const char*> saved_msg;
	//const char *recvMsgConn;
	try { 
		const unsigned int max_trying = 3; //repeated try limit
		unsigned int i = 0;
		function_set::openConnection(mining_host, mining_port, (mining_flags&1)==1);
		//if open connection failed this loop end directly
		json::jobject dat;
		//subscribe message with initialize machine name : AndroidLTCMiner
		strcpy(_msgtemp, "{\"id\":1,\"method\":\"mining.subscribe\",\"params\":[\"AndroidLTCteMiner\"]}");
		function_set::sendMessage(_msgtemp);
		for (i = 0; i < max_trying; i++) {
			char *mC = const_cast<char*>(function_set::getMessage());
			if (*mC == '\0') continue;
			for(mC = strtok(mC, "\n"); mC != nullptr; mC = strtok(nullptr, "\n")) {
				json::jobject::tryparse(mC, dat);
				if (dat.get("id") == "1") {
					if (!dat["error"].is_null()) throw dat.get("error").c_str();
					json::jobject::entry j_result = dat["result"];
					if (j_result.array(0).array(0) != "mining.notify") throw "error params";
					mining_sesion_id = j_result[0]j[1];
					mining_xnonce1 = j_result[1];
					mining_xnonce2_size = j_result[2];
				} else {
					dataLoadOut(dat);
				}
				
			}
			break;
		}
		dat.clear();
		sprintf(_msgtemp, "Id: %s,n1: %s,n2: %u", mining_sesion_id.c_str(), mining_xnonce1.c_str(), mining_xnonce2_size);
		console::write(0, _msgtemp);
		//data_mining.clear();
		if (i >= max_trying) {
			throw "No received message after subscribe";
		}
		sprintf(_msgtemp, "{\"id\":2,\"method\":\"mining.authorize\",\"params\":[\"%s\",\"%s\"]}",mining_user,mining_pass);
		function_set::sendMessage(_msgtemp);
		for (i = 0; i < max_trying; i++) {
			char *mC = const_cast<char*>(function_set::getMessage());
			if (*mC == '\0') continue;
			for(mC = strtok(mC, "\n"); mC != nullptr; mC = strtok(nullptr, "\n")) {
				json::jobject::tryparse(mC, dat);
				if (dat.get("id") == "2") {
					if (!dat["error"].is_null()) throw dat["error"].as_string().c_str();
					if (!dat["result"].is_true()) throw "false authentications";
				} else {
					dataLoadOut(dat);
				}
			}
			break;
		}
		dat.clear();
		if (i >= max_trying) {
			throw "No received message after authorize";
		}
		function_set::afterStart();
		bool running = false;
		while (running){
			//console::write(0, function_set::recvConnection());
			//do nothing right now
			std::this_thread::sleep_for(std::chrono::seconds(1)); 
			//receive Mesage
			mining_mtx.lock();
			if (mining_req) {
				if (mining_req&MININGREQ_DESTROY) {
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
	//cleaning
	console::write(1, "End Mining...");
	function_set::closeConnection();
	function_set::afterStop();
	/*
	mining_mtx.lock();
	mining_mtx.unlock();
	*/
	//countTowards = 0;
}




