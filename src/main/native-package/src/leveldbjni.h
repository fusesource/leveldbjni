/*******************************************************************************
 * Copyright (C) 2011, FuseSource Corp.  All rights reserved.
 *
 *     http://fusesource.com
 *
 * The software in this package is published under the terms of the
 * CDDL license a copy of which has been included with this distribution
 * in the license.txt file.
 *******************************************************************************/
#ifndef LEVELDBJNI_H
#define LEVELDBJNI_H

#ifdef HAVE_CONFIG_H
  /* configure based build.. we will use what it discovered about the platform */
  #include "config.h"
#else
  #if defined(_WIN32) || defined(_WIN64)
    /* Windows based build */
    #define HAVE_STDLIB_H 1
    #define HAVE_STRINGS_H 1
    #include <windows.h>
  #endif
#endif

#ifdef HAVE_UNISTD_H
  #include <unistd.h>
#endif

#ifdef HAVE_STDLIB_H
  #include <stdlib.h>
#endif

#ifdef HAVE_STRINGS_H
  #include <string.h>
#endif

#include "jni.h"
#include <stdint.h>
#include <stdarg.h>

#ifdef __cplusplus

#include "leveldb/db.h"
#include "leveldb/options.h"
#include "leveldb/write_batch.h"
#include "leveldb/cache.h"
#include "leveldb/comparator.h"
#include "leveldb/env.h"
#include "leveldb/slice.h"

struct JNIComparator : public leveldb::Comparator {
  JNIEnv *env;
  jobject target;
  jmethodID compare_method;
  const char *name;

  int Compare(const leveldb::Slice& a, const leveldb::Slice& b) const {
     return env->CallIntMethod(target, compare_method, (jlong)(intptr_t)&a, (jlong)(intptr_t)&b);
  }

  const char* Name() const {
     return name;
  }

  void FindShortestSeparator(std::string*, const leveldb::Slice&) const { }
  void FindShortSuccessor(std::string*) const { }
};

struct JNILogger : public leveldb::Logger {
  JNIEnv *env;
  jobject target;
  jmethodID log_method;

  void Logv(const char* format, va_list ap) {

    char buffer[1024];
    vsnprintf(buffer, sizeof(buffer), format, ap);

    jstring message = env->NewStringUTF(buffer);
    if( message ) {
      env->CallVoidMethod(target, log_method, message);
      env->DeleteLocalRef(message);
    }
  }

};

#endif


#ifdef __cplusplus
extern "C" {
#endif

void buffer_copy(const void *source, size_t source_pos, void *dest, size_t dest_pos, size_t length);

#ifdef __cplusplus
} /* extern "C" */
#endif


#endif /* LEVELDBJNI_H */
