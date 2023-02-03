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
} *loc_data;

void core::startMining(void*) {
	loc_data = new data_transfer;
	pthread_mutex_init(&mutex, NULL);
  pthread_cond_init(&cond, NULL);
  pthread_attr_t attr; 
  pthread_attr_init(&attr);
  pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);
  pthread_create(&thread, &attr, miningThread, loc_data);
  pthread_attr_destroy(&attr);
}

void core::stopMining(void*) {
  pthread_mutex_lock(&mutex);
  loc_data->destroy = true;
  pthread_mutex_unlock(&mutex);
  pthread_cond_destroy(&cond);
  pthread_mutex_destroy(&mutex);
  delete loc_data;
}

static void *miningThread(void *dat) {
	data_transfer *dt = (data_transfer*)dat;
	
	//this for preparation like socket validation auth etc.
	sleep(3);
  pthread_mutex_lock(&mutex);
  //do nothing
  pthread_mutex_unlock(&mutex);
	
	
	for(;;) {
		//do nothing right now
		sleep(1); 
	  pthread_mutex_lock(&mutex);
  	if (dt->destroy)
			break;
	  pthread_mutex_unlock(&mutex);
	}
	
	//this for cleaning like socket close etc.
	sleep(3);
  pthread_mutex_lock(&mutex);
  //do nothing
  pthread_mutex_unlock(&mutex);
  return NULL;
}