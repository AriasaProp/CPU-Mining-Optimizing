#include "console_log.h"

#include <cstdint>
#include <cstdio>
#include <ctime>
#include <cstring>
#include <string>

char *htmlMsg;
char *endHtmlMsg;

void console_log::initialize() {
	htmlMsg = new char[console_log::MAX_MSG_SIZE+1];
	memset(htmlMsg,' ', console_log::MAX_MSG_SIZE);
	endHtmlMsg = htmlMsg+console_log::MAX_MSG_SIZE;
	*endHtmlMsg = '\0';
}
const char *console_log::write(unsigned int lv, const char *msg, unsigned long length) {
  memmove(htmlMsg+length+43, htmlMsg, console_log::MAX_MSG_SIZE-length-45);
  char *modif = htmlMsg;
	memcpy(modif, "<font color='#", 14);
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
  	memcpy(modif, "Error!!!!  ", 11);
  }
  memcpy((modif+=10), msg, length);
  memcpy((modif+=length), "</font><br>", 11);
	return htmlMsg;
}

void console_log::destroy() {
	delete htmlMsg;
	endHtmlMsg = nullptr;
}
