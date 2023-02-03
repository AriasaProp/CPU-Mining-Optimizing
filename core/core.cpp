#include "core.h"

#include <iostream>
#include <thread>
#include <condition_variable>
#include <chrono>
#include <mutex>

std::mutex mutex;
std::condition_variable cv;
std::thread thread;

struct data_transfer {
	bool create;
	bool destroy;
} *loc_data;

void miningThread();

void core::startMining(void*) {
	if(loc_data) return;
	loc_data = new data_transfer;
	loc_data->create = true;
	thread = std::thread(miningThread);
	std::unique_lock<std::mutex> lck(mutex);
	cv.wait(lck, []{return !loc_data->create;});
}

void core::stopMining(void*) {
	if(!loc_data) return;
	loc_data->destroy = true;
	thread.join();
	delete loc_data;
}

void miningThread() {
	//this for preparation like socket validation auth etc.
	std::this_thread::sleep_for(std::chrono::seconds(3));
	std::unique_lock<std::mutex> lck(mutex);
	loc_data->create = false;
	cv.notify_one();
	lck.unlock();

	for(;;) {
		std::unique_lock<std::mutex> lck(mutex);
		if (loc_data->destroy)
			break;
		lck.unlock();
		//do nothing right now
		std::this_thread::sleep_for(std::chrono::seconds(1)); 
	}

	//this for cleaning like socket close etc.
	std::this_thread::sleep_for(std::chrono::seconds(3));
	std::unique_lock<std::mutex> lck(mutex);
	loc_data->destroy = false;
	lck.unlock();
}
