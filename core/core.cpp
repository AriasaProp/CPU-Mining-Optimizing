#include "core.h"
#include "../console.h"

#include <iostream>
#include <thread>
#include <chrono>
#include <mutex>

std::thread mining_thread;
std::mutex mining_mtx;

bool mining_running = false;
std::function<void()> afterThat; //use to safe function defined from start and stop
void miningThread();

void core::startMining(std::function<void()> a) {
	if(mining_thread.joinable()) {
		a();
		return;
	}
	//you should call stopMining first
	mining_mtx.lock();
	mining_running = false;
	afterThat = a;
	mining_mtx.unlock();
	mining_thread = std::thread(miningThread);
}

void core::stopMining(std::function<void()> a) {
	if(!mining_thread.joinable()) {
		a();
		return;
	}
	//you should call startMining first
	mining_mtx.lock();
	afterThat = a;
	mining_running = false;
	mining_mtx.unlock();
}

void miningThread() {
	//create state
	console::write(1, "Logging App ....", 16);
	std::this_thread::sleep_for(std::chrono::seconds(3));
	console::write(1, "Logged App", 10);
	mining_mtx.lock();
	mining_running = true;
	if(afterThat) {
		afterThat();
		afterThat = NULL;
	}
	mining_mtx.unlock();

	do {
		std::this_thread::sleep_for(std::chrono::seconds(1)); 
		//do nothing right now
		
	} while (mining_running);
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
