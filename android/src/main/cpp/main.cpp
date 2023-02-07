#include <jni.h>

#include "core.h"
#include "console.h"

bool initializedOnce = false;
jmethodID receiveMsgId;

#define JNI_Call(R,M) extern "C" JNIEXPORT R JNICALL Java_com_ariasaproject_cpuminingopt_MainActivity_##M
JNI_Call(void, startMining) (JNIEnv *env, jobject o) {
	if (!initializedOnce) {
		JavaVM *vm;
		env->GetJavaVM(&vm);
		initializedOnce = true;
		console::initialize([vm,&o](const char *msg, const unsigned int length){
			JNIEnv *n;
			vm->AttachCurrentThread(&n, 0);
			char tmsg[length+1];
			memcpy(tmsg, msg, length);
			tmsg[length] = '\0';
			jstring p2 = n->NewStringUTF(tmsg);
			n->CallVoidMethod(o, receiveMsgId, 9, p2);
			vm->DetachCurrentThread();
		});
		
	}
	core::startMining();
}
JNI_Call(void, stopMining) (JNIEnv *, jobject) {
	core::stopMining();
}
#undef JNI_Call
//native management
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void*) {
	initializedOnce = false;
  JNIEnv* env;
  if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK)
      return JNI_ERR;
  jclass cls = env->FindClass("com/ariasaproject/cpuminingopt/MainActivity");
  receiveMsgId = env->GetMethodID(cls,"receiveMessage", "(ILjava/lang/String;)V");
  return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM*, void*) {
	//uninitialize static variable
	console::destroy();
	initializedOnce = false;
}
