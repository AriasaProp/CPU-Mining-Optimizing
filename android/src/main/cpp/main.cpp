#include <jni.h>

#include "core.h"

#define JNI_Call(R,M) extern "C" JNIEXPORT R JNICALL Java_com_ariasaproject_cpuminingopt_MainActivity_##M
jmethodID mainClass_afterstartId;
jmethodID mainClass_afterstopId;
JNI_Call(void, startMining) (JNIEnv *env, jobject o) {
	core::startMining([env,mainClass_afterstartId]{
  	env->CallVoidMethod(o, mainClass_afterstartId, 0);
	});
}
JNI_Call(void, stopMining) (JNIEnv *, jobject) {
	core::stopMining([env,mainClass_afterstopId]{
  	env->CallVoidMethod(o, mainClass_afterstopId, 0);
	});
}
#undef JNI_Call
//native management
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void*) {
  JNIEnv* env;
  if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
      return JNI_ERR;
  }
  jclass mainClass = eny->FindClass("com/ariasaproject/cpuminingopt/MainActivity");
  mainClass_afterstartId = env->GetMethodID(mainClass, "callAfterStart", "()V");
  mainClass_afterstopId = env->GetMethodID(mainClass, "callAfterStop", "()V");
  return JNI_VERSION_1_6;
	//nothing todo
}

JNIEXPORT void JNI_OnUnload(JavaVM*, void*) {
	//nothing todo
}
