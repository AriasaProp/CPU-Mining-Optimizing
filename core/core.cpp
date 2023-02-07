#include "core.h"
#include "../console.h"

#include <iostream>
#include <thread>
#include <chrono>
#include <mutex>

#define MININGREQ_CREATE 1
#define MININGREQ_DESTROY 2

std::thread mining_thread;
std::mutex mining_mtx;
unsigned int mining_req;

std::function<void()> afterThat; //use to safe function defined from start and stop
void miningThread();

void core::startMining(std::function<void()> a) {
	mining_mtx.lock();
	afterThat = a;
	mining_mtx.unlock();
	mining_thread = std::thread(miningThread);
	mining_thread.detach();
}

void core::stopMining(std::function<void()> a) {
	mining_mtx.lock();
	mining_req |= MININGREQ_DESTROY;
	afterThat = a;
	mining_mtx.unlock();
}

void miningThread() {
	//create state
	console::write(1, "Logging App ....", 16);
	std::this_thread::sleep_for(std::chrono::seconds(3));
	console::write(1, "Logged App", 10);
	mining_mtx.lock();
	if(afterThat) {
		afterThat();
		afterThat = NULL;
	}
	mining_mtx.unlock();
	bool running = true;
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
	console::write(1, "Unlogging App ....", 18);
	std::this_thread::sleep_for(std::chrono::seconds(3));
	console::write(1, "Unlogged App", 12);
	mining_mtx.lock();
	if(afterThat) {
		afterThat();
		afterThat = NULL;
	}
	mining_mtx.unlock();
}
