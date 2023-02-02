#include "core.h"

#include <iostream>
#include <unistd.h>
#include <pthread.h>

pthread_mutex_t mutex;
pthread_cond_t cond;
pthread_t thread;

static void *miningThread(void*);
struct data_transfer {
	bool destroy;
	void(function*)() toRun;
} *loc_data;

void core::startMining(void(function*)() r) {
	loc_data = new data_transfer;
	loc_data->toRun = r;
	pthread_mutex_init(&mutex, NULL);
  pthread_cond_init(&cond, NULL);
  pthread_attr_t attr; 
  pthread_attr_init(&attr);
  pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);
  pthread_create(&thread, &attr, miningThread, loc_data);
  pthread_attr_destroy(&attr);
}

void core::stopMining(void(function*)() r) {
  pthread_mutex_lock(&mutex);
	loc_data->toRun = r;
  loc_data->destroy = true;
  pthread_mutex_unlock(&mutex);
  pthread_cond_destroy(&cond);
  pthread_mutex_destroy(&mutex);
  delete loc_data;
}

static void *miningThread(void *dat) {
	data_transfer *dt = (data_transfer*)dat;
	
  pthread_mutex_lock(&mutex);
	sleep(3);
	dt->running = true;
	if(dt->toRun) {
		(*dt->toRun)();
		dt->toRun = nullptr;
	}
  pthread_mutex_unlock(&mutex);
	
	
	for(;;) {
		//do nothing right now
		sleep(1); 
	  pthread_mutex_lock(&mutex);
  	if (dt->destroy)
			break;
	  pthread_mutex_unlock(&mutex);
	}
	
	sleep(3);
  pthread_mutex_lock(&mutex);
	if(dt->toRun) {
		(*dt->toRun)();
		dt->toRun = nullptr;
	}
  pthread_mutex_unlock(&mutex);
  return NULL;
}