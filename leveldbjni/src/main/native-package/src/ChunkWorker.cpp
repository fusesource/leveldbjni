/*
 * Copyright (C) 2012, FuseSource Corp.  All rights reserved.
 *
 *     http://fusesource.com
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *    * Neither the name of FuseSource Corp. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
#include "leveldbjni.h"

#define FAIL_IF_NULL(arg) if (arg == NULL) { info.status = ERR_JNI_INTERNAL; return info; }
#define FAIL_ON_EXCEPTION if (env->ExceptionCheck()) { info.status = ERR_JNI_INTERNAL; return info; }

struct BufferInfo {
  int status;
  jobject buffer;
  char *store;
  jarray original;
  jlong capacity;
};

BufferInfo obtainBufferStore(JNIEnv *env, jobject buffer) {
  BufferInfo info;

  info.status = 0;
  info.buffer = buffer;

  // Try and obtain as a direct buffer first. This will return NULL if it's not direct
  info.store = (char *) env->GetDirectBufferAddress(buffer);

  if (info.store != NULL) {
    info.original = NULL;
    info.capacity = env->GetDirectBufferCapacity(buffer);
  } else {
    // This isn't a direct buffer, so we need to obtain a reference to the backing array, if it has it. First, confirm we have an array
    jclass byteBufferClass = env->FindClass("java/nio/ByteBuffer");
    
    FAIL_IF_NULL(byteBufferClass);

    jmethodID hasArrayMethod = env->GetMethodID(byteBufferClass, "hasArray", "()Z");

    FAIL_IF_NULL(hasArrayMethod);

    jboolean hasArray = env->CallBooleanMethod(buffer, hasArrayMethod);

    FAIL_ON_EXCEPTION;

    if (! hasArray) {
      info.status = ERR_INVALID_BUFFER;
    } else {
      // obtain the array
      jmethodID arrayMethod = env->GetMethodID(byteBufferClass, "array", "()[B");

      FAIL_IF_NULL(arrayMethod);

      jbyteArray array = (jbyteArray) env->CallObjectMethod(buffer, arrayMethod);

      FAIL_IF_NULL(array);

      info.original = array;
      info.capacity = env->GetArrayLength(array);
      info.store    = (char *) env->GetPrimitiveArrayCritical(array, 0);
    }
  }

  return info;
}

void setBufferLimit(JNIEnv *env, jobject buffer, jint limit) {
  jclass byteBufferClass = env->FindClass("java/nio/ByteBuffer");
    
  if (byteBufferClass == NULL) {
    return;
  }

  jmethodID limitMethod = env->GetMethodID(byteBufferClass, "limit", "(I)Ljava/nio/Buffer;");

  if (limitMethod == NULL) {
    return;
  }

  env->CallObjectMethod(buffer, limitMethod, limit);
}

void releaseBufferStore(JNIEnv *env, const BufferInfo &info) {
  // We only need to do work if this isn't a direct buffer (original != NULL) and it's valid
  if (info.original != NULL && info.status == 0) {
    env->ReleasePrimitiveArrayCritical(info.original, info.store, 0);
  }
}

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_org_fusesource_leveldbjni_internal_ChunkWorker_getNextChunkNative
(JNIEnv *env, jclass that, jlong iterPtr, jobject keyBuffer, jobject valBuffer, jboolean encodeKeys, jboolean encodeVals, jint keyWidth, jint valWidth) {
  BufferInfo keyInfo = obtainBufferStore(env, keyBuffer);

  if (keyInfo.status != 0) {
    return keyInfo.status;
  }

  BufferInfo valInfo = obtainBufferStore(env, valBuffer);

  if (valInfo.status != 0) {
    releaseBufferStore(env, keyInfo);
    return valInfo.status;
  }

  ChunkMetadata metadata;

  int rc = chunk_pairs((void *)iterPtr, &metadata, keyInfo.capacity, valInfo.capacity, keyInfo.store, valInfo.store, encodeKeys, encodeVals, keyWidth, valWidth);

  setBufferLimit(env, keyBuffer, metadata.keyByteLength);
  setBufferLimit(env, valBuffer, metadata.valByteLength);

  releaseBufferStore(env, keyInfo);
  releaseBufferStore(env, valInfo);

  return rc;
}

#ifdef __cplusplus
}
#endif
