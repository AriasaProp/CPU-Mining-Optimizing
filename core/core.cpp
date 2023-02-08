#include "core.h"
#include "console.h"

#include <iostream>
#include <thread>
#include <chrono>
#include <mutex>

#include "pass_function_set.h"

#define MININGREQ_CREATE 1
#define MININGREQ_DESTROY 2

std::thread mining_thread;
std::mutex mining_mtx;
unsigned int mining_req;

void miningThread();

void core::startMining() {
	mining_mtx.lock();
	mining_mtx.unlock();
	mining_thread = std::thread(miningThread);
	mining_thread.detach();
}

void core::stopMining() {
	mining_mtx.lock();
	mining_req |= MININGREQ_DESTROY;
	mining_mtx.unlock();
}

void miningThread() {
	//create state
	console::write(1, "Logging App ....");
	std::this_thread::sleep_for(std::chrono::seconds(3));
	console::write(1, "Logged App");
	mining_mtx.lock();
	mining_mtx.unlock();
	
	bool running = function_set::openConnection("us2.litecoinpool.org",8080);
	if (running) {
		function_set::afterStart();
	}
	while (running) {
		std::this_thread::sleep_for(std::chrono::seconds(1)); 
		//do nothing right now
		
		mining_mtx.lock();
		if (mining_req) {
			
			if (mining_req&MININGREQ_DESTROY) {
				running = false;
			}
			mining_req = 0;
		}
		mining_mtx.unlock();
	}
	//this for cleaning like socket close etc.
	console::write(1, "Unlogging App ....");
	std::this_thread::sleep_for(std::chrono::seconds(3));
	console::write(1, "Unlogged App");
	function_set::closeConnection();
	function_set::afterStop();
	mining_mtx.lock();
	mining_mtx.unlock();
}
