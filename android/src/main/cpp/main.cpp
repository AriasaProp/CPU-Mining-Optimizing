#include <jni.h>

#include <cstdint>
#include <cstring>
#include <string>
#include <ctime>

            
#define JNI_Call(R,M) extern "C" JNIEXPORT R JNICALL Java_com_ariasaproject_cpuminingopt_Console_##M

const std::string *logs;
const size_t *logs_state;

JNI_Call(void, initialize) (JNIEnv* env, jclass) {
	logs = new std::string[30]; //each logs only 1000 chars 
	for (size_t i = 0; i < 30; i++) {
		logs[i] = "None\n"
	}
	logs_state = new size_t[30];
}
JNI_Call(void, write) (JNIEnv* env, jclass, jint lv, jstring l) {
	if (!l) return;
  for(size_t i = 29; i > 0; i--) {
  	logs[i] = logs[i - 1];
  	logs_state[i] = logs_state[i - 1];
  }
	const char *cstr = env->GetStringUTFChars(l, NULL);
	//const jsize *cstr_length = env->GetStringLength(l);
  char buf[10];
  std::time t = std::time(0);
  if(strftime(buf, "hh:mm:ss| ", std::localtime(&t))){
  	logs[0] = std::string() + buf + cstr;
  } else {
  	logs[0] = "problem in write native i guest";
  }
  logs[0] += '\n';
  logs_state[0] = lv;
  env->ReleaseStringUTFChars(l, cstr);
}
JNI_Call(jstring, outLogs) (JNIEnv*env, jclass) {
	std::string out;
	for (size_t i = 0; i < 30; i++) {
		out += logs[i];
	}
	return env->NewStringUTF(out.c_str());
}
JNI_Call(void, destroy) (JNIEnv*, jclass) {
	delete logs;
	delete logs_state;
}
