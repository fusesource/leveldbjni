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
#include <jni.h>

#include <iostream>
#include <algorithm>

#include <arpa/inet.h>

#define DEBUG_CHUNK_PAIRS(x) 0;
//#define DEBUG_CHUNK_PAIRS(x) x

#ifdef __cplusplus
extern "C" {
#endif

  int chunk_pairs(void *iterPtr, struct ChunkMetadata* meta, int keyLimit, int valLimit, char* keyBuffer, char* valBuffer, int encodeKeys, int encodeVals, int keyWidth, int valWidth) {
    DEBUG_CHUNK_PAIRS(std::cout << "Starting chunk fill" << std::endl;)

    leveldb::Iterator *iter = (leveldb::Iterator *) iterPtr;

    int count = 0;
    long usedKeyBufferSize = 0;
    long usedValBufferSize = 0;

    // Error check first
    if (meta == NULL) {
      return ERR_INVALID_META;
    }

    if (iterPtr == NULL) {
      return ERR_INVALID_ITER;
    }

    if (keyBuffer == NULL || valBuffer == NULL) {
      return ERR_INVALID_BUFFER;
    }

    DEBUG_CHUNK_PAIRS(std::cout << "Safety checks complete" << std::endl;)

    // We simply iterate as long as the iterator is valid. We do free space checks on our buffers later
    for (int i = 0; iter->Valid(); ++i) {
      leveldb::Slice ks = iter->key();
      leveldb::Slice vs = iter->value();

      // Actual allocated space depends on whether we're variable length, fixed non-zero length, 
      // or if we're fixed at zero bytes we just encode the whole thing
      int keyByteWidth = encodeKeys ? 4 + ks.size() : (keyWidth == 0 ? ks.size() : keyWidth);
      int valByteWidth = encodeVals ? 4 + vs.size() : (valWidth == 0 ? vs.size() : valWidth);

      // We have to stop if the combine key/value pair would exceed our chunk buffer
      if ((usedKeyBufferSize + keyByteWidth) > keyLimit || (usedValBufferSize  + valByteWidth) > valLimit) {
        break;
      }

      DEBUG_CHUNK_PAIRS(std::cout << "Space OK" << std::endl;)

      ++count;
      
      // Compact key and value onto the end of the buffer
      if (encodeKeys) {
        *(int *)(keyBuffer + usedKeyBufferSize) = htonl(ks.size());
        usedKeyBufferSize += 4;
      }
      
      int keyDataWidth = encodeKeys ? ks.size() : (keyWidth == 0 ? ks.size() : keyWidth);

      // If we don't have enough bytes to fulfill (fixed width but insufficient data size), return an error
      if (ks.size() < keyDataWidth) {
        return ERR_INSUFFICIENT_BYTES;
      }

      memcpy(keyBuffer + usedKeyBufferSize, ks.data(), keyDataWidth);
      usedKeyBufferSize += keyDataWidth;

      DEBUG_CHUNK_PAIRS(std::cout << "  Key encoded" << std::endl;)

      if (encodeVals) {
        *(int *)(valBuffer + usedValBufferSize) = htonl(vs.size());
        usedValBufferSize += 4;
      }
        
      int valDataWidth = encodeVals ? vs.size() : (valWidth == 0 ? vs.size() : valWidth);

      if (vs.size() < valDataWidth) {
        return ERR_INSUFFICIENT_BYTES;
      }

      memcpy(valBuffer + usedValBufferSize, vs.data(), valDataWidth);
      usedValBufferSize += valDataWidth;

      DEBUG_CHUNK_PAIRS(std::cout << "  Val encoded" << std::endl;)

      iter->Next();
    }

    DEBUG_CHUNK_PAIRS(std::cout << "Chunk filled" << std::endl;)

    meta->keyByteLength = usedKeyBufferSize;
    meta->valByteLength = usedValBufferSize;
    meta->pairLength = count;

    return count;
}

#ifdef __cplusplus
}
#endif

