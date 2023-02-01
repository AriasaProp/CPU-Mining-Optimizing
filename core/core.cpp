#include "core.h"

#include <iostream>
#include <pthread.h>

pthread_mutex_t mutex;
pthread_cond_t cond;
pthread_t thread;

static void *miningThread(void*);
struct data_transfer {
	bool running;
	bool begining;
	bool destroy;
} *loc_data;

void core::startMining(void**) {
	loc_data = new data_transfer;
	pthread_mutex_init(&mutex, NULL);
  pthread_cond_init(&cond, NULL);
  pthread_attr_t attr; 
  pthread_attr_init(&attr);
  pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);
  pthread_create(&thread, &attr, miningThread, loc_data);
  pthread_attr_destroy(&attr);
  pthread_mutex_lock(&mutex);
  loc_data->begining = true;
	pthread_cond_broadcast(&cond);
  while (loc_data->begining) {
  	pthread_cond_wait(&cond, &mutex);
  }
  pthread_mutex_unlock(&mutex);
}

void core::stopMining() {
  pthread_mutex_lock(&mutex);
  loc_data->destroy = true;
	pthread_cond_broadcast(&cond);
  while (loc_data->destroy) {
  	pthread_cond_wait(&cond, &mutex);
  }
  pthread_mutex_unlock(&mutex);
  pthread_cond_destroy(&cond);
  pthread_mutex_destroy(&mutex);
  delete loc_data;
}

static void *miningThread(void *dat) {
	data_transfer *dt = (data_transfer*)dat;
	
  pthread_mutex_lock(&mutex);
  //wait till begin allowed
	while(!dt->begining) {
  	pthread_cond_wait(&cond, &mutex);
	}
	dt->begining = false;
	dt->running = true;
	pthread_cond_broadcast(&cond);
  pthread_mutex_unlock(&mutex);
	
	do {
		//do nothing right now
		if (dt->destroy) {
		  pthread_mutex_lock(&mutex);
			dt->running = false;
		  pthread_mutex_unlock(&mutex);
		}
	} while (dt->running);
	
  pthread_mutex_lock(&mutex);
	dt->destroy = false;
	pthread_cond_broadcast(&cond);
  pthread_mutex_unlock(&mutex);
  return NULL;
}