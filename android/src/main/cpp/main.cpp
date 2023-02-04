#include <jni.h>

#include "core.h"

#define JNI_Call(R,M) extern "C" JNIEXPORT R JNICALL Java_com_ariasaproject_cpuminingopt_MainActivity_##M
static jmethodID mainClass_afterstartId;
static jmethodID mainClass_afterstopId;
static core *m_core;
JNI_Call(void, startMining) (JNIEnv *env, jobject o) {
	if (m_core) return;
	m_core = new core;
	JavaVM *jvm;
  env->GetJavaVM(&jvm);
	m_core->afterCall = [&jvm,&o](bool start){
    JNIEnv* nEnv;
    jvm->AttachCurrentThread(&nEnv, 0);
    jthrowable exception = nEnv->ExceptionOccurred();
    if (exception) {
      nEnv->ExceptionClear();
    } else {
    	if (start) {
  			nEnv->CallVoidMethod(o, mainClass_afterstartId);
    	} else {
      	nEnv->CallVoidMethod(o, mainClass_afterstopId);
    	}
    }
    jvm->DetachCurrentThread();
	};
	m_core->startMining();
}
JNI_Call(void, stopMining) (JNIEnv *env, jobject o) {
	if (!m_core) return;
	m_core->startMining();
}
#undef JNI_Call
//native management
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void*) {
  JNIEnv* env;
  if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK)
      return JNI_ERR;
  jclass mainClass = env->FindClass("com/ariasaproject/cpuminingopt/MainActivity");
	//initialize static variable
  mainClass_afterstartId = env->GetMethodID(mainClass, "callAfterStart", "()V");
  mainClass_afterstopId = env->GetMethodID(mainClass, "callAfterStop", "()V");
  return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM*, void*) {
	//uninitialize static variable
	mainClass_afterstartId = 0;
	mainClass_afterstopId = 0;
}
