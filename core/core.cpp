#include "core.h"

#include <iostream>
#include <unistd.h>
#include <pthread.h>

pthread_mutex_t mutex;
pthread_cond_t cond;
pthread_t thread;

static void *miningThread(void*);
struct data_transfer {
	bool create;
	bool destroy;
} *loc_data;

void core::startMining(void*) {
	loc_data = new data_transfer;
	pthread_mutex_init(&mutex, NULL);
  pthread_cond_init(&cond, NULL);
  pthread_attr_t attr; 
  pthread_attr_init(&attr);
  pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);
  loc_data->create = true;
  pthread_create(&thread, &attr, miningThread, loc_data);
  pthread_attr_destroy(&attr);
  pthread_mutex_lock(&mutex);
  while(loc_data->create)
  	pthread_cond_wait(&cond,&mutex);
  pthread_mutex_unlock(&mutex);
  
}

void core::stopMining(void*) {
  pthread_mutex_lock(&mutex);
  loc_data->destroy = true;
  while(loc_data->destroy)
  	pthread_cond_wait(&cond,&mutex);
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
  dt->create = false;
	pthread_cond_broadcast(&cond);
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
  dt->destroy = false;
	pthread_cond_broadcast(&cond);
  pthread_mutex_unlock(&mutex);
  return NULL;
}