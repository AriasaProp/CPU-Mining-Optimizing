#include <jni.h>

#include "core.h"
#include "console.h"

bool initializedOnce = false;
jmethodID receiveMsgId;
jobject mainobj;

#define JNI_Call(R,M) extern "C" JNIEXPORT R JNICALL Java_com_ariasaproject_cpuminingopt_MainActivity_##M
JNI_Call(void, startMining) (JNIEnv *env, jobject o) {
	JavaVM *vm;
	env->GetJavaVM(&vm);
	if (!initializedOnce) {
		mainobj = env->NewGlobalRef(o);
		console::initialize([&vm](const char *msg, const unsigned int length){
			JNIEnv *n;
			vm->AttachCurrentThread(&n, 0);
			char tmsg[length+1];
			memcpy(tmsg, msg, length);
			tmsg[length] = '\0';
			jstring p2 = n->NewStringUTF(tmsg);
			n->CallVoidMethod(mainobj, receiveMsgId, 9, p2);
			vm->DetachCurrentThread();
		});
		initializedOnce = true;
	}
	core::startMining([&vm]{
		JNIEnv *n;
		vm->AttachCurrentThread(&n, 0);
		n->CallVoidMethod(mainobj, receiveMsgId, 2, 0);
		vm->DetachCurrentThread();
	});
}
JNI_Call(void, stopMining) (JNIEnv *env, jobject) {
	JavaVM *vm;
	env->GetJavaVM(&vm);
	core::stopMining([&vm]{
		JNIEnv *n;
		vm->AttachCurrentThread(&n, 0);
		n->CallVoidMethod(mainobj, receiveMsgId, 4, 0);
		vm->DetachCurrentThread();
	});
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
