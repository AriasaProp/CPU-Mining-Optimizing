#include <jni.h>

#include <cstdint>
#include <cstring>
#include <string>
#include <ctime>

#include "console_log.h"
#include "hasher.h"
            
#define JNI_Call(R,M) extern "C" JNIEXPORT R JNICALL Java_com_ariasaproject_cpuminingopt_Console_##M

JNI_Call(jstring, write) (JNIEnv* env, jclass, jint lv, jstring l) {
		if (!l) return env->NewStringUTF("Null");
		const char *cstr = env->GetStringUTFChars(l, NULL);
		const jsize cstr_length = env->GetStringLength(l);
	  const char *r = console_log::write(lv, cstr, cstr_length);
	  env->ReleaseStringUTFChars(l, cstr);
		return env->NewStringUTF(r);
}

#undef JNI_Call
#define JNI_Call(R,M) extern "C" JNIEXPORT R JNICALL Java_com_ariasaproject_cpuminingopt_Hasher_##M

JNI_Call(void, nHash) (JNIEnv* env, jclass, jbyteArray B, jintArray X) {
	jint *c_X = env->GetIntArrayElements(X, NULL);
	jbyte *c_B = env->GetByteArrayElements(B, NULL);
	
	hasher::hash((void*)c_B, (unsigned int *)c_X);
	
	env->ReleaseIntArrayElements(X, c_X, JNI_ABORT);
	env->ReleaseByteArrayElements(B, c_B, 0);
}
#undef JNI_Call

//native management
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void*) {
  JNIEnv* env;
  if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
      return JNI_ERR;
  }
  console_log::initialize();
  //hasher::initialize();
  return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM*, void*) {
	console_log::destroy();
	//hasher::destroy();
}
