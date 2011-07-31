/*******************************************************************************
 * Copyright (C) 2011, FuseSource Corp.  All rights reserved.
 *
 *     http://fusesource.com
 *
 * The software in this package is published under the terms of the
 * CDDL license a copy of which has been included with this distribution
 * in the license.txt file.
 *******************************************************************************/
#include "leveldbjni.h"

void buffer_copy(const void *source, size_t source_pos, void *dest, size_t dest_pos, size_t length) {
  memmove(dest+dest_pos, source+source_pos, length);
}
