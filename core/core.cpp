#include "core.h"

#include <iostream>
#include <thread>
#include <condition_variable>
#include <chrono>
#include <mutex>

std::thread thread;
std::mutex mutex;
std::condition_variable cv;

void miningThread();
bool running;

void core::startMining() {
	if(thread.joinable()) return;
	//you should call stopMining first
	running = false;
	thread = std::thread(miningThread);
	thread.detach();
	std::unique_lock lck(mutex);
	cv.wait(lck, []{return running;});
}

void core::stopMining() {
	if(!thread.joinable()) return;
	//you should call startMining first
	mutex.lock();
	running = false;
	mutex.unlock();
	thread.join();
}

void miningThread() {
	//this for preparation like socket validation auth etc.
	//create state
	std::this_thread::sleep_for(std::chrono::seconds(3));
	mutex.lock();
	running = true;
	cv.notify_all();
	mutex.unlock();

	for(;;) {
		std::this_thread::sleep_for(std::chrono::seconds(1)); 
		//do nothing right now
		mutex.lock();
		if(!running) break;
		mutex.unlock();
	}
	//this for cleaning like socket close etc.
	std::this_thread::sleep_for(std::chrono::seconds(3));
	mutex.lock();
	//destroy state
	mutex.unlock();
}
