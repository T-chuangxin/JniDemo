#include <jni.h>
#include <string>
#include "flatbuffers/idl.h"

extern "C" JNIEXPORT jstring

JNICALL
Java_com_jd_jnidemo_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_jd_jnidemo_MainActivity_parseJsonNative(JNIEnv *env, jobject instance, jstring json_,
                                                 jstring schema_) {
    const char *json = env->GetStringUTFChars(json_, 0);
    const char *schema = env->GetStringUTFChars(schema_, 0);

    // TODO
    flatbuffers::Parser parser;
    bool ok = parser.Parse(schema) && parser.Parse(json);

    env->ReleaseStringUTFChars(json_, json);
    env->ReleaseStringUTFChars(schema_, schema);

    if (ok) {
        flatbuffers::uoffset_t length = parser.builder_.GetSize();
        jbyteArray result = env->NewByteArray(length);
        uint8_t *bufferPointer = parser.builder_.GetBufferPointer();
        env->SetByteArrayRegion(result, 0, length, reinterpret_cast<jbyte *>(bufferPointer));
        return result;
    }else{
        return env->NewByteArray(0);
    }
}