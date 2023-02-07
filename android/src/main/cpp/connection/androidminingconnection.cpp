#include "androidminingconnection.h"

#include <iostream>
#include <string>
#include <cstring>

static void *socketLoop(void*);

AndroidSocket::AndroidSocket() {
	ready = false;
	hasConnection = false;
  memset(&server_addr, 0, sizeof(server_addr));
  pthread_mutex_init(&mutex, NULL);
  pthread_cond_init(&cond, NULL);
  pthread_attr_t attr; 
  pthread_attr_init(&attr);
  pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);
  pthread_create(&thread, &attr, socketLoop, (void*)this);
  pthread_attr_destroy(&attr);
  pthread_mutex_lock(&mutex);
  while (!ready)
  	pthread_cond_wait(&cond, &mutex);
  pthread_mutex_unlock(&mutex);
}

AndroidSocket::~AndroidSocket() {
  pthread_mutex_lock(&mutex);
  ready = false;
  while (!ready)
  	pthread_cond_wait(&cond, &mutex);
  pthread_mutex_unlock(&mutex);
  pthread_cond_destroy(&cond);
  pthread_mutex_destroy(&mutex);
	ready = false;
	hasConnection = false;
  memset(&server_addr, 0, sizeof(server_addr));
}

bool AndroidSocket::openConnection(const char *server, unsigned int &port) {
	hostent *srv = gethostbyname(server);
  if (!srv) {
    std::cerr << "Error: failed to resolve hostname" << std::endl;
    return false;
  }
  server_addr.sin_family = AF_INET;
  memcpy(&server_addr.sin_addr.s_addr, srv->h_addr, srv->h_length);
  server_addr.sin_port = htons(port);
  pthread_mutex_lock(&mutex);
  hasConnection = true;
  pthread_cond_broadcast(&cond);
  pthread_mutex_unlock(&mutex);
  
	return true;
}
bool AndroidSocket::write(const char*) {
	
	return 0;
}
char *AndroidSocket::read() {
	
	return 0;
}
bool AndroidSocket::closeConnection() {
	if (!hasConnection) return false;
	pthread_mutex_lock(&mutex);
	hasConnection = false;
  pthread_cond_broadcast(&cond);
  pthread_mutex_unlock(&mutex);
	return true;
}

static void *socketLoop(void *arg) {
	AndroidSocket *as = (AndroidSocket*) arg;
	//try make socket
	int sockfd;
	while ((sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0)
    std::cerr << "Socket: failed to create socket" << std::endl;
  pthread_mutex_lock(&as->mutex);
  as->ready = true;
  as->hasConnection = false;
	pthread_cond_broadcast(&as->cond);
  pthread_mutex_unlock(&as->mutex);
	//loop connection
	while(as->ready) {
		//wait till has connection request
	  pthread_mutex_lock(&as->mutex);
	  while (!as->hasConnection)
	  	pthread_cond_wait(&as->cond, &as->mutex);
	  pthread_mutex_unlock(&as->mutex);
		//connect to connection
		if (connect(sockfd, (sockaddr*) &as->server_addr, sizeof(as->server_addr)) < 0) {
	    std::cerr << "Socket: failed to connect server" << std::endl;
			continue;
		}
	    
	  while (as->hasConnection) {
	  	//loop for connection
	  }
  	//close connection
  	close(sockfd);
	}
	//try destroy socket
  pthread_mutex_lock(&as->mutex);
  as->ready = true;
	pthread_cond_broadcast(&as->cond);
  pthread_mutex_unlock(&as->mutex);
  return NULL;
}

