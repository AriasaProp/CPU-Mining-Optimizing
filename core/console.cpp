#include "console.h"

#include <cstdint>
#include <cstdio>
#include <ctime>
#include <cstring>
#include <string>
#include <mutex>

#define MAX_MSG_SIZE 16383

char *htmlMsg;
char *endHtmlMsg;
const char *frontKey = "<font color='#";
const char *color1 = "808080";
const char *color2 = "0000ff";
const char *color3 = "00ff00";
const char *color4 = "ffff00";
const char *color5 = "ff0000";
const char *frontKey1 = "'>";
const char *endKey = "</font><br>";

std::mutex mutex;
std::function<void(const char *, const unsigned int)> receiveMsg;

void console::initialize(std::function<void(const char *, const unsigned int)> f) {
    receiveMsg = f;
    htmlMsg = new char[MAX_MSG_SIZE + 1];
    memset(htmlMsg, ' ', MAX_MSG_SIZE);
    endHtmlMsg = htmlMsg + MAX_MSG_SIZE;
    *endHtmlMsg = '\0';
}
void console::write(const unsigned int &lv, const char *msg, const unsigned int length) {
		if (length > 8192) {
				unsigned int hLength = length/2;
				unsigned int nLength = length - hLength;//to prove when length is odd
				write(lv,msg,hLength);
				write(lv,msg+hLength,nLength);
				return;
		}
		mutex.lock();
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
    mutex.unlock();
    if (receiveMsg)
        receiveMsg(htmlMsg, lastLength);
}

void console::destroy() {
    delete[] htmlMsg;
    htmlMsg = nullptr;
    endHtmlMsg = nullptr;
}

