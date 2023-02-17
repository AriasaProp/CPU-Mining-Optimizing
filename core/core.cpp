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

#include "pass_function_set.h"

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
	for (auto &el : data_mining) {
		data_mining.clear();
	}
}
#include <string>
std::map<std::string, std::string> data_mining;
void setsData(const char *readit) {
  const char *be1,*be2;
  for (const char *c = readit; *c != '\0'; c++) {
    c = strchr(c, '{');
    if (!c) break;
    while (*c!='}') {
    	//get the key
      be1 = strchr(c, '\"') + 1; //quote 1
      be2 = strchr(be1, '\"');   //quote 2
      std::string key = std::string(be1, be2);
      be1 = be2+2; //ignore : and next
      //get the value
	    unsigned int bracket = 1;
      switch (*be1) {
	      case '{':
          for (be2 = be1 + 1; bracket; be2++) {
            switch (*be2) {
              default:
                break;
              case '{':
                bracket++;
                break;
              case '}':
                bracket--;
                break;
            }
          }
	      	break;
	      case '[':
          for (be2 = be1 + 1; bracket; be2++) {
            switch (*be2) {
              default:
                break;
              case '[':
                bracket++;
                break;
              case ']':
                bracket--;
                break;
            }
          }
	      	break;
	      case '\"':
          be2 = strchr(be1+1, '\"')+1;
          break;
	      default:
	      	be2 = be1+1;
          while(*be2 != ',' && *be2 != '}')
            be2++;
          break;
	    }
	    std::string value = std::string(be1, be2);
      c = be2;
	    if (value == "null") continue;
      data_mining[key] = value;
  	}
	}
}
//unsigned long countTowards = 0;
void miningThread() {
	//create state
	console::write(1, "Start Mining ......");
	char _msgtemp[1024];
	//std::vector<const char*> saved_msg;
	//const char *recvMsgConn;
	try {
		const unsigned int max_trying = 3; //repeated try limit
		unsigned int i = 0; 
		//try connect to server
		//https://catfact.ninja/fact
		function_set::openConnection(mining_host, mining_port, (mining_flags&1)==1);
		//if open connection failed this loop end directly
		//subscribe message with initialize machine name : AndroidLTCMiner_ForLearningTest
		strcpy(_msgtemp, "{\"id\":1,\"method\":\"mining.subscribe\",\"params\":[\"AndroidLTCteMiner\"]}\n");
		function_set::sendMessage(_msgtemp);
		for (i = 0; i < max_trying; i++) {
			setsData(function_set::getMessage());
			std::map<std::string, std::string>::iterator idDat = data_mining.find("id");
			if ((idDat == data_mining.end()) || (idDat->second == "1")) {
				continue;
			}
			std::map<std::string, std::string>::iterator errDat = data_mining.find("error");
			if (errDat == data_mining.end()) {
				std::string tr = errDat->second;
				data_mining.clear();
				throw tr;
			}
			break;
			/*
			const char *response = function_set::getMessage();
			if (*response != '\0') {
				if (memcmp(response, "{\"id\":1,", 8) == 0) {
					response += 8;
					const char *resM;
					if ((resM = strstr(response, ",\"result\":"))) {
						const char *errM;
						if ((errM = strstr(response, "\"error\":"))) {
							errM += 8;
							if (memcmp(errM,"null",4) != 0) {
								strcpy(_msgtemp, "Error Authentications: ");
								strncat(_msgtemp, errM, resM-errM-1);
								throw _msgtemp;
							}
							
							break;
						}
					}
				}
			}
			console::write(0, "No message");
			std::this_thread::sleep_for(std::chrono::seconds(2)); 
			*/
		}
		console::write(0, data_mining["result"].c_str());
		data_mining.clear();
		if (i >= max_trying) {
			throw "No received message after subscribe";
		}
		sprintf(_msgtemp, "{\"id\":2,\"method\":\"mining.authorize\",\"params\":[\"%s\",\"%s\"]}\n",mining_user,mining_pass);
		function_set::sendMessage(_msgtemp);
		for (i = 0; i < max_trying; i++) {
			setsData(function_set::getMessage());
			std::map<std::string, std::string>::iterator idDat = data_mining.find("id");
			if ((idDat == data_mining.end()) || (idDat->second == "2")) {
				continue;
			}
			std::map<std::string, std::string>::iterator resDat = data_mining.find("result");
			if ((resDat == data_mining.end()) || (resDat->second == "false")) {
				std::map<std::string, std::string>::iterator errDat = data_mining.find("error");
				if (errDat != data_mining.end()) {
					std::string tr = errDat->second;
					data_mining.clear();
					throw tr;
				} else {
					data_mining.clear();
					throw "Wrong Authentication";
				}
			}
			break;
			/*
			const char *response = function_set::getMessage();
			if (*response != '\0') {
				if (memcmp(response, "{\"id\":2,", 8) == 0) {
					response += 8;
					const char *resM;
					if ((resM = strstr(response, ",\"result\":"))){
						if(memcmp(resM+10, "true", 4) != 0) throw "Authentications wrong!";
						const char *errM;
						if ((errM = strstr(response, "\"error\":"))) {
							errM += 8;
							if (memcmp(errM,"null",4) != 0) {
								strcpy(_msgtemp, "Error Authentications: ");
								strncat(_msgtemp, errM, resM-errM-1);
								throw _msgtemp;
							}
							break;
						}
					}
				}
			}
			console::write(0, "No message");
			std::this_thread::sleep_for(std::chrono::seconds(2));
			*/
		}
		if (i >= max_trying) {
			throw "No received message after authorize";
		}
		console::write(0, "Authorize succes");
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




