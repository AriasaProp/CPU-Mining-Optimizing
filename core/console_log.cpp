#include "console_log.h"

#include <cstdint>
#include <cstdio>
#include <ctime>
#include <cstring>
#include <string>

using namespace console_log;

#define MAX_MSG_SIZE 65535

char *htmlMsg;
char *endHtmlMsg;

void initialize() {
	htmlMsg = new char[MAX_MSG_SIZE+1];
	memset(htmlMsg,' ', MAX_MSG_SIZE);
	endHtmlMsg = htmlMsg+MAX_MSG_SIZE;
	*endHtmlMsg = '\0';
}
const char *frontKey = "<font color='#";
const char *endKey = "</font><br>";
const char *write(unsigned int lv, const char *msg, unsigned long length) {
  memmove(htmlMsg+length+43, htmlMsg, MAX_MSG_SIZE-length-43);
  char *modif = htmlMsg;
	memcpy(modif, frontKey, 14);
	modif += 14;
	switch(lv) {
		default:
		case 0:
			memcpy(modif,"808080",6);//debug
			break;
		case 1:
			memcpy(modif,"0000ff",6);//info
			break;
		case 2:
			memcpy(modif,"00ff00",6);//success
			break;
		case 3:
			memcpy(modif,"ffff00",6);//warning
			break;
		case 4:
			memcpy(modif,"ff0000",6);//Error
			break;
  }
  memcpy((modif+=6), "'>", 2);
  std::time_t t = std::time(0);
  if(!strftime((modif+=2), 11, "%T| ", std::localtime(&t))){
  	memcpy(modif, "Error!!!! ", 10);
  }
  //strftime include \0 at last,so overlap it
  memcpy((modif+=10), msg, length);
  memcpy((modif+=length), endKey, 11);
  modif += 11;
  //check if html format was incomplete
  char *tF = endHtmlMsg-14;
  while (--tF > modif) {
  	
  	if (memcmp(tF, frontKey, 14) == 0) {
  		char *bF = strstr(tF, endKey);
  		if (bF && ((bF+12)<endHtmlMsg))
				tF = bF+11;
			memset(tF, ' ', endHtmlMsg-tF);
  		break;
  	}
  }
	return htmlMsg;
}

void destroy() {
	delete htmlMsg;
	endHtmlMsg = nullptr;
}