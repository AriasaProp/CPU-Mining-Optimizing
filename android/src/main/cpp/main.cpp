#include <jni.h>

#include "core.h"

#define JNI_Call(R,M) extern "C" JNIEXPORT R JNICALL Java_com_ariasaproject_cpuminingopt_MainActivity_##M
JNI_Call(void, startMining) (JNIEnv *env, jobject o) {
	m_core::startMining();
}
JNI_Call(void, stopMining) (JNIEnv *, jobject) {
	m_core::stopMining();
}
#undef JNI_Call
//native management
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void*) {
  JNIEnv* env;
  if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK)
      return JNI_ERR;
  //jclass mainClass = env->FindClass("com/ariasaproject/cpuminingopt/MainActivity");
	//initialize static variable
  return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM*, void*) {
	//uninitialize static variable
}
