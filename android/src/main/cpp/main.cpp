#include <jni.h>

#include <cstdint>
#include <cstring>
#include <string>
#include <ctime>
            
#define JNI_Call(R,M) extern "C" JNIEXPORT R JNICALL Java_com_ariasaproject_cpuminingopt_Console_##M
#define MAX_LOGS 25
std::string *logs;
size_t *logs_state;

JNI_Call(void, write) (JNIEnv* env, jclass, jint lv, jstring l) {
	if (!l) return;
  for(size_t i = MAX_LOGS-1; i > 0; i--) {
  	logs[i] = logs[i - 1];
  	logs_state[i] = logs_state[i - 1];
  }
	const char *cstr = env->GetStringUTFChars(l, NULL);
	//const jsize *cstr_length = env->GetStringLength(l);
  char buf[15];
  std::time_t t = std::time(0);
  if(strftime(buf, sizeof(buf), "%T| ", std::localtime(&t))){
  	logs[0] = std::string(buf) + std::string(cstr);
  } else {
  	logs[0] = "problem in write native i guest";
  }
  logs[0] += '\n';
  logs_state[0] = lv;
  env->ReleaseStringUTFChars(l, cstr);
}
JNI_Call(jstring, outLogs) (JNIEnv*env, jclass) {
	std::string out;
	for (size_t i = 0; i < MAX_LOGS; i++) {
		out += logs[i];
	}
	return env->NewStringUTF(out.c_str());
}

//native management

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void*) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    logs = new std::string[MAX_LOGS]; //each logs only 1000 chars 
		for (size_t i = 0; i < MAX_LOGS; i++) {
			logs[i] = "None\n";
		}
		logs_state = new size_t[MAX_LOGS];
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM*, void*) {
		delete logs;
		delete logs_state;
}
