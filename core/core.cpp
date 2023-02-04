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

void core::startMining(std::function<void()> &f) {
	if(loc_data) return;
	loc_data = new data_transfer;
	loc_data->create = true;
	thread = std::thread(miningThread);
	std::unique_lock<std::mutex> lck(mutex);
	cv.wait(lck, []{return !loc_data->create;});
	f();
}

void core::stopMining(std::function<void()> &f) {
	if(!loc_data) return;
	loc_data->destroy = true;
	thread.join();
	delete loc_data;
	loc_data = nullptr;
	f();
}

void miningThread() {
	//this for preparation like socket validation auth etc.
	std::this_thread::sleep_for(std::chrono::seconds(3));
	mutex.lock();
	loc_data->create = false;
	cv.notify_all();
	mutex.unlock();

	for(;;) {
		std::this_thread::sleep_for(std::chrono::seconds(1)); 
		//do nothing right now6
		mutex.lock();
		if (loc_data->destroy)
			break;
		mutex.unlock();
	}
	//this for cleaning like socket close etc.
	std::this_thread::sleep_for(std::chrono::seconds(3));
	mutex.lock();
	loc_data->destroy = false;
	mutex.unlock();
}
