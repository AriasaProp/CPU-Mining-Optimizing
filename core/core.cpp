#include "core.h"
#include "../console.h"

#include <iostream>
#include <thread>
#include <condition_variable>
#include <chrono>
#include <mutex>
#include <functional>

std::thread mining_thread;
std::mutex mining_mtx;
std::condition_variable mining_cv;

bool running;
std::function<void()> afterThat; //use to safe function defined from start and stop
void miningThread();

void core::startMining(std::function<void()> a) {
	if(mining_thread.joinable()) return;
	//you should call stopMining first
	afterThat = a;
	mining_mtx.lock();
	running = false;
	mining_mtx.unlock();
	mining_thread = std::thread(miningThread);
	std::unique_lock<std::mutex> lck(mining_mtx);
	mining_cv.wait(lck, []{return running;});
}

void core::stopMining(std::function<void()> a) {
	if(!mining_thread.joinable()) return;
	//you should call startMining first
	afterThat = a;
	mining_mtx.lock();
	running = false;
	mining_mtx.unlock();
	mining_thread.join();
}

void miningThread() {
	//this for preparation like socket validation auth etc.
	//create state
	console::write(1, "Logging App ....", 16);
	std::this_thread::sleep_for(std::chrono::seconds(3));
	console::write(1, "Logged App", 10);
	mining_mtx.lock();
	running = true;
	if(afterThat) {
		afterThat();
		afterThat = NULL;
	}
	mining_mtx.unlock();
	mining_cv.notify_all();

	do {
		std::this_thread::sleep_for(std::chrono::seconds(1)); 
		//do nothing right now
		
	} while (running);
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
