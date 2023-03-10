#include "console_log.h"

#include <cstdint>
#include <cstdio>
#include <ctime>
#include <cstring>
#include <mutex>

#define MAX_MSG_SIZE 16383

std::mutex console_mtx;

char htmlMsg[MAX_MSG_SIZE + 1];
char *endHtmlMsg = htmlMsg + MAX_MSG_SIZE;
const char *frontKey = "<font color='#";
const char *color1 = "808080";
const char *color2 = "0000ff";
const char *color3 = "00ff00";
const char *color4 = "ffff00";
const char *color5 = "ff0000";
const char *frontKey1 = "'>";
const char *endKey = "</font><br>";

void console_log::initialize() {
  memset(htmlMsg, ' ', MAX_MSG_SIZE);
  *endHtmlMsg = '\0';
}
const char *console_log::write(unsigned int lv, const char *msg, unsigned long length) {
	if (length > 8192) {
		unsigned int hL = length / 2;
		unsigned int hL2 = length - hL;
		char msg1[hL+1];
		memcpy(msg1,msg,hL);
		msg1[hL] = '\0';
		char msg2[hL2];
		memcpy(msg2,msg+hL,hL2);
		write(lv,msg1, hL);
		return write(lv,msg2, hL2);
	}
	console_mtx.lock();
	memmove(htmlMsg + length + 43, htmlMsg, MAX_MSG_SIZE - length - 43);
	char *modif = htmlMsg;
	memcpy(modif, frontKey, 14);
	modif += 14;
	switch (lv) {
	default:
	case 0:
	  memcpy(modif, color1, 6); //debug
	  break;
	case 1:
	  memcpy(modif, color2, 6); //info
	  break;
	case 2:
	  memcpy(modif, color3, 6); //success
	  break;
	case 3:
	  memcpy(modif, color4, 6); //warning
	  break;
	case 4:
	  memcpy(modif, color5, 6); //Error
	  break;
	}
	memcpy((modif += 6), frontKey1, 2);
	std::time_t t = std::time(0);
	if (!strftime((modif += 2), 11, "%T| ", std::localtime(&t))) {
	  memcpy(modif, "Error!!!! ", 10);
	}
	//strftime include \0 at last,so overlap it
	memcpy((modif += 10), msg, length);
	memcpy((modif += length), endKey, 11);
	modif += 11;
	//check if html format was incomplete
	char *tF = endHtmlMsg - 7;
	unsigned int lastLength = 43 + length;
	while (--tF > modif) {
	  if (memcmp(tF, endKey, 7) == 0) {
      tF += 7;
      memset(tF, ' ', endHtmlMsg - tF);
      lastLength = tF - htmlMsg;
      break;
	  }
	}
	console_mtx.unlock();
	htmlMsg[lastLength] = '\0';
	return htmlMsg;
}

void console_log::destroy() {
  endHtmlMsg = nullptr;
}

