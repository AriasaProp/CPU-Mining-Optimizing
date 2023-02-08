#include <jni.h>

namespace function_set {
	//trigger mining
	void (*afterStart) ();
	void (*afterStop) ();
	//console receiver msg
	void (*consoleMessage) (const char*,const unsigned int);
}

#include "core.h"
#include "console.h"
#include <cstring>
#include <pthread.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <netinet/in.h>
#include <unistd.h>


bool initializedOnce = false;
jmethodID receiveMsgId;
JavaVM *mainVM;
jobject mainobj;

#define JNI_Call(R,M) extern "C" JNIEXPORT R JNICALL Java_com_ariasaproject_cpuminingopt_MainActivity_##M
JNI_Call(void, startMining) (JNIEnv *env, jobject o) {
	if (!initializedOnce) {
		mainobj = env->NewGlobalRef(o);
		void(*afterStart)() = []{
			JNIEnv *n;
			mainVM->AttachCurrentThread(&n, 0);
			n->CallVoidMethod(mainobj, receiveMsgId, 2, 0);
			mainVM->DetachCurrentThread();
		};
		void(*afterStop)() = []{
			JNIEnv *n;
			mainVM->AttachCurrentThread(&n, 0);
			n->CallVoidMethod(mainobj, receiveMsgId, 4, 0);
			mainVM->DetachCurrentThread();
		};
		void(*consoleMessage)(const char *, const unsigned int) = [](const char *msg, const unsigned int length){
			JNIEnv *n;
			mainVM->AttachCurrentThread(&n, 0);
			char tmsg[length+1];
			memcpy(tmsg, msg, length);
			tmsg[length] = '\0';
			jstring p2 = n->NewStringUTF(tmsg);
			n->CallVoidMethod(mainobj, receiveMsgId, 9, p2);
			mainVM->DetachCurrentThread();
		};
		function_set::afterStart = afterStart;
		function_set::afterStop = afterStop;
		function_set::consoleMessage = consoleMessage;
		initializedOnce = true;
	}
	core::startMining();
}
JNI_Call(void, stopMining) (JNIEnv *, jobject) {
	core::stopMining();
}
#undef JNI_Call
//native management
JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void*) {
	initializedOnce = false;
  JNIEnv* env;
  if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK)
      return JNI_ERR;
  mainVM = vm;
  jclass cls = env->FindClass("com/ariasaproject/cpuminingopt/MainActivity");
  receiveMsgId = env->GetMethodID(cls,"receiveMessage", "(ILjava/lang/String;)V");
  return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM *vm, void*) {
  JNIEnv* env;
  vm->GetEnv((void**)&env,  0);
	//uninitialize static variable
	if (initializedOnce) {
		console::destroy();
		env->DeleteGlobalRef(mainobj);
		initializedOnce = false;
	}
}
