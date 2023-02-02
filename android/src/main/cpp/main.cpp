#include <jni.h>

#include "core.h"

#define JNI_Call(R,M) extern "C" JNIEXPORT R JNICALL Java_com_ariasaproject_cpuminingopt_MainActivity_##M
JNI_Call(void, startMining) (JNIEnv *env, jobject o) {
	core::startMining([&](){
		JavaVM* jvm;
	  env->GetJavaVM(&jvm);
	  jvm->AttachCurrentThread(&env, NULL);
	  jclass cls = env->GetObjectClass(o);
	  jmethodID mid = env->GetMethodID(cls, "afterStartMining", "()V");
	  env->CallVoidMethod(o, mid);
	  jvm->DetachCurrentThread();
	});
}
JNI_Call(void, stopMining) (JNIEnv *env, jobject o) {
	core::stopMining([&](){
		JavaVM* jvm;
	  env->GetJavaVM(&jvm);
	  jvm->AttachCurrentThread(&env, NULL);
	  jclass cls = env->GetObjectClass(o);
	  jmethodID mid = env->GetMethodID(cls, "afterStopMining", "()V");
	  env->CallVoidMethod(o, mid);
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
  return JNI_VERSION_1_6;
	//nothing todo
}

JNIEXPORT void JNI_OnUnload(JavaVM*, void*) {
	//nothing todo
}
