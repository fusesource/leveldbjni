dnl ---------------------------------------------------------------------------
dnl  Copyright (C) 2011, FuseSource Corp.  All rights reserved.
dnl
dnl      http://fusesource.com
dnl
dnl  The software in this package is published under the terms of the
dnl  CDDL license a copy of which has been included with this distribution
dnl  in the license.txt file.
dnl ---------------------------------------------------------------------------

AC_DEFUN([CUSTOM_M4_SETUP],
[
  AC_LANG_PUSH(C++)

  AC_CHECK_HEADER([pthread.h],[AC_DEFINE([HAVE_PTHREAD_H], [1], [Define to 1 if you have the <pthread.h> header file.])])

  AC_ARG_WITH([leveldb],
  [AS_HELP_STRING([--with-leveldb@<:@=PATH@:>@],
    [Directory where leveldb was built. Example: --with-leveldb=/opt/leveldb])],
  [
    CFLAGS="$CFLAGS -I${withval}/include"
    CXXFLAGS="$CXXFLAGS -I${withval}/include"
    AC_SUBST(CXXFLAGS)
    LDFLAGS="$LDFLAGS -lleveldb -L${withval}"
    AC_SUBST(LDFLAGS)
  ])

  AC_CHECK_HEADER([leveldb/db.h],,AC_MSG_ERROR([cannot find headers for leveldb]))

  AC_ARG_WITH([snappy],
  [AS_HELP_STRING([--with-snappy@<:@=PATH@:>@],
    [Directory where snappy was built. Example: --with-snappy=/opt/snappy])],
  [
    LDFLAGS="$LDFLAGS -lsnappy -L${withval}"
    AC_SUBST(LDFLAGS)
  ])

  AC_LANG_POP()
])