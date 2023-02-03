#include <jni.h>

#include "core.h"

#define JNI_Call(R,M) extern "C" JNIEXPORT R JNICALL Java_com_ariasaproject_cpuminingopt_MainActivity_##M
JNI_Call(void, startMining) (JNIEnv *, jobject) {
	core::startMining(0);
}
JNI_Call(void, stopMining) (JNIEnv *, jobject) {
	core::stopMining(0);
}
#undef JNI_Call
//native management
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void*) {
  JNIEnv* env;
  if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
      return JNI_ERR;
  }
  return JNI_VERSION_1_6;
	//nothing todo
}

JNIEXPORT void JNI_OnUnload(JavaVM*, void*) {
	//nothing todo
}
