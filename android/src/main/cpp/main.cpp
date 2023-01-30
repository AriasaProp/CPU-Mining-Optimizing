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

#undefine JNI_Call
#define JNI_Call(R,M) extern "C" JNIEXPORT R JNICALL Java_com_ariasaproject_cpuminingopt_Hasher_##M

JNI_Call(void, xorSalsa) (JNIEnv* env, jclass, jintArray X) {
	unsigned int *c_X = (unsigned int *)env->GetIntArrayElements(X, NULL);
	hasher::xorSalsa(c_X);
	env->Release<PrimitiveType>ArrayElements(X, c_X, 0);
}
#undefine JNI_Call

//native management
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void*) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    console_log::initialize();
    hasher::initialize();
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM*, void*) {
	console_log::destroy();
	hasher::destroy();
}