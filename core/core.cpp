#include "core.h"

#include <iostream>
#include <thread>
#include <condition_variable>
#include <chrono>
#include <mutex>


struct data_transfer {
	std::mutex mutex;
	std::condition_variable cv;
	std::thread thread;
	std::function<void()> shouldRun;
	bool create;
	bool destroy;
} *loc_data;

void miningThread();

void core::startMining(std::function<void()> f) {
	if(loc_data) return;
	loc_data = new data_transfer;
	loc_data->shouldRun = f;
	loc_data->create = true;
	loc_data->thread = std::thread(miningThread);
}

void core::stopMining(std::function<void()> f) {
	if(!loc_data) return;
	loc_data->mutex.lock();
	loc_data->shouldRun = f;
	loc_data->destroy = true;
	loc_data->mutex.unlock();
	loc_data->thread.join();
	delete loc_data;
	loc_data = nullptr;
}

void miningThread() {
	//this for preparation like socket validation auth etc.
	std::this_thread::sleep_for(std::chrono::seconds(3));
	loc_data->mutex.lock();
	loc_data->create = false;
	if(loc_data->shouldRun) {
		loc_data->shouldRun();
		loc_data->shouldRun = 0;
	}
	loc_data->mutex.unlock();

	for(;;) {
		std::this_thread::sleep_for(std::chrono::seconds(1)); 
		//do nothing right now6
		loc_data->mutex.lock();
		if (loc_data->destroy)
			break;
		loc_data->mutex.unlock();
	}
	//this for cleaning like socket close etc.
	std::this_thread::sleep_for(std::chrono::seconds(3));
	loc_data->mutex.lock();
	loc_data->destroy = false;
	if(loc_data->shouldRun) {
		loc_data->shouldRun();
		loc_data->shouldRun = 0;
	}
	loc_data->mutex.unlock();
}
