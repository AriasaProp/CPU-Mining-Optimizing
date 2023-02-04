#include <jni.h>

#include "core.h"

#define JNI_Call(R,M) extern "C" JNIEXPORT R JNICALL Java_com_ariasaproject_cpuminingopt_MainActivity_##M
static jmethodID mainClass_afterstartId;
static jmethodID mainClass_afterstopId;
JNI_Call(void, startMining) (JNIEnv *env, jobject o) {
	JavaVM* jvm;
  env->GetJavaVM(&jvm);
	core::startMining([&]{
    JNIEnv* nEnv;
    jvm->AttachCurrentThread(&nEnv, 0);
  	nEnv->CallVoidMethod(o, mainClass_afterstartId, 0);
    jvm->DetachCurrentThread();
	});
}
JNI_Call(void, stopMining) (JNIEnv *env, jobject o) {
	JavaVM* jvm;
  env->GetJavaVM(&jvm);
	core::stopMining([&]{
    JNIEnv* nEnv;
    jvm->AttachCurrentThread(&nEnv, 0);
  	nEnv->CallVoidMethod(o, mainClass_afterstopId, 0);
    jvm->DetachCurrentThread();
	});
}
#undef JNI_Call
//native management
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void*) {
  JNIEnv* env;
  if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
      return JNI_ERR;
  }
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
