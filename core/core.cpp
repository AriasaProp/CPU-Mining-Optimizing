#include "core.h"
#include "console.h"

#include <iostream>
#include <thread>
#include <chrono>
#include <mutex>
#include <condition_variable>
#include <cstring>

#include "pass_function_set.h"

#define MININGREQ_CREATE 1
#define MININGREQ_DESTROY 2

std::thread mining_thread;
std::mutex mining_mtx;
std::condition_variable mining_cv;
unsigned int mining_req;
char *mining_host;
char *mining_user;
char *mining_pass;

void miningThread();

void core::startMining(const char *host, const char *user, const char *pass) {
	mining_mtx.lock();
	unsigned int l = strlen(host);
	mining_host = new char[l+1];
	memcpy(mining_host, host, l);
	mining_host[l] = '\0';
	l = strlen(user);
	mining_user = new char[l+1];
	memcpy(mining_user, user, l);
	mining_user[l] = '\0';
	l = strlen(pass);
	mining_pass = new char[l+1];
	memcpy(mining_pass, pass, l);
	mining_pass[l] = '\0';
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
//unsigned long countTowards = 0;
void miningThread() {
	//create state
	console::write(1, "Start Mining ......");
	//try connect to server
	bool running = true;
	unsigned int trying = 0;
	//https://catfact.ninja/fact
	{
		char _msgtemp[2048];
		strcpy(_msgtemp, "Host: ");
		strcat(_msgtemp, mining_host);
		strcat(_msgtemp, ".\0");
		console::write(0, _msgtemp);
		strcpy(_msgtemp, "Auth: ");
		strcat(_msgtemp, mining_user);
		strcat(_msgtemp, " : ");
		strcat(_msgtemp, mining_pass);
		strcat(_msgtemp, " \0");
		console::write(0, _msgtemp);
	}
	console::write(0, "Unlogged App");
	trying = 0;
	while (!(running = function_set::openConnection(mining_host, 3333)) && (trying++ < 3)) {
		std::this_thread::sleep_for(std::chrono::milliseconds(200)); 
		console::write(0, "Try conect again");
	}
	char sendToServer[1024];
	const char *recvMsgConn;
	if (running) {
		strcpy(sendToServer, "{\"id\": 1,\"method\": \"mining.subscribe\",\"params\": []}\0");
		function_set::sendMessage(sendToServer);
		while ( ((recvMsgConn = function_set::recvConnection())[0] == ' ') && (++trying < 3))
			console::write(0, "No message");
		if (trying >= 3) {
			running = false;
		}
	}
	if (running) {
		strcpy(sendToServer, "{\"id\": 2,\"method\": \"mining.authorize\",\"params\": [\"");
		strcat(sendToServer, mining_user);
		strcat(sendToServer, "\",\"");
		strcat(sendToServer, mining_pass);
		strcat(sendToServer, "\"]}\0");
		function_set::sendMessage(sendToServer);
		while ( ((recvMsgConn = function_set::recvConnection())[0] == ' ') && (++trying < 3))
			console::write(0, "No message");
		if (trying >= 3) {
			running = false;
		}
	}
	if (running)
		function_set::afterStart();
	while (running) {
		console::write(0, function_set::recvConnection());
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
	//cleaning
	console::write(1, "Unlogged App");
	function_set::closeConnection();
	function_set::afterStop();
	/*
	mining_mtx.lock();
	mining_mtx.unlock();
	*/
	//countTowards = 0;
}
