#include "leveldbjni.h"
#include <jni.h>

#include <iostream>
#include <algorithm>

#include <arpa/inet.h>

#ifdef __cplusplus
extern "C" {
#endif

  int chunk_pairs(void *iterPtr, struct ChunkMetadata* meta, int maxByteSize, char* buffer, int encodeKeys, int encodeVals, int keyWidth, int valWidth) {
    leveldb::Iterator *iter = (leveldb::Iterator *) iterPtr;

    int count = 0;
    long usedBufferSize = 0;

    // Error check first
    if (meta == NULL) {
      return -1;
    }

    if (iterPtr == NULL) {
      return -2;
    }

    if (buffer == NULL) {
      return -3;
    }

    // We simply iterate as long as the iterator is valid. We do free space checks on our buffers later
    for (int i = 0; iter->Valid(); ++i) {
      leveldb::Slice ks = iter->key();
      leveldb::Slice vs = iter->value();

      // Actual allocated space depends on whether we're variable length, fixed non-zero length, 
      // or if we're fixed at zero bytes we just encode the whole thing
      int keyByteWidth = encodeKeys ? 4 + ks.size() : (keyWidth == 0 ? ks.size() : keyWidth);
      int valByteWidth = encodeVals ? 4 + vs.size() : (valWidth == 0 ? vs.size() : valWidth);

      // We have to stop if the combine key/value pair would exceed our chunk buffer
      if ((usedBufferSize + keyByteWidth + valByteWidth) > maxByteSize) {
        break;
      }

      ++count;
      
      // Compact key and value onto the end of the buffer
      if (encodeKeys) {
        *(int *)(buffer + usedBufferSize) = htonl(ks.size());
        usedBufferSize += 4;
      }
      
      int keyDataWidth = encodeKeys ? ks.size() : (keyWidth == 0 ? ks.size() : keyWidth);

      // If we don't have enough bytes to fulfill (fixed width but insufficient data size), return an error
      if (ks.size() < keyDataWidth) {
        return -4;
      }

      memcpy(buffer + usedBufferSize, ks.data(), keyDataWidth);
      usedBufferSize += keyDataWidth;

      if (encodeVals) {
        *(int *)(buffer + usedBufferSize) = htonl(vs.size());
        usedBufferSize += 4;
      }
        
      int valDataWidth = encodeVals ? vs.size() : (valWidth == 0 ? vs.size() : valWidth);

      if (vs.size() < valDataWidth) {
        return -4;
      }

      memcpy(buffer + usedBufferSize, vs.data(), valDataWidth);
      usedBufferSize += valDataWidth;

      iter->Next();
    }

    meta->byteLength = usedBufferSize;
    meta->pairLength = count;

    return usedBufferSize;
}

#ifdef __cplusplus
}
#endif

