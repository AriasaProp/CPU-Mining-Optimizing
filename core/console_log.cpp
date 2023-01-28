#include "console_log.h"

#include <cstdint>
#include <cstdio>
#include <ctime>
#include <cstring>
#include <string>
//2^20 

char *htmlMsg;
char *endHtmlMsg;

void console_log::initialize() {
	htmlMsg = new char[console_log::MAX_MSG_SIZE+1];
	memset(htmlMsg,' ', console_log::MAX_MSG_SIZE);
	endHtmlMsg = htmlMsg+console_log::MAX_MSG_SIZE;
	write(0,"Wellcome to CPU Mining Opt", 27);
}
const char *console_log::write(unsigned int lv, const char *msg, unsigned long length) {
  memmove(htmlMsg+length+45, htmlMsg, console_log::MAX_MSG_SIZE-length-41);
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
  *(modif+=6) = '>';
  std::time_t t = std::time(0);
  if(!strftime((++modif), 9, "%T", std::localtime(&t))){
  	memcpy(modif, "Error!!   ", 9);
  }
  memcpy((modif+=9), msg, length);
  memcpy((modif+=length), "</font> <br>", 12);
  memcpy((modif+=12), "   ", 3);
	return htmlMsg;
}

void console_log::destroy() {
	delete htmlMsg;
	endHtmlMsg = nullptr;
}
