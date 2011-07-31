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

#ifdef __cplusplus
#include "leveldb/db.h"
#endif

#ifdef __cplusplus
extern "C" {
#endif

void buffer_copy(const void *source, size_t source_pos, void *dest, size_t dest_pos, size_t length);

#ifdef __cplusplus
} /* extern "C" */
#endif


#endif /* LEVELDBJNI_H */
