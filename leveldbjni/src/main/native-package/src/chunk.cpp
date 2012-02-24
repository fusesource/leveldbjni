#include "leveldbjni.h"
#include <jni.h>

#include <iostream>
#include <list>

#include <arpa/inet.h>

//#define DEBUG_CHUNKS 1

#ifdef __cplusplus
extern "C" {
#endif

  void chunk_pairs(void *iterPtr, struct ChunkMetadata* meta, int maxByteSize, char* buffer, int encodeKeys, int encodeVals, int keyWidth, int valWidth) {
    leveldb::Iterator *iter = (leveldb::Iterator *) iterPtr;

    int count = 0;
    long usedBufferSize = 0;

#ifdef DEBUG_CHUNKS
    std::cout << "Iterating" << std::endl;
#endif

    // We simply iterate as long as the iterator is valid. We do free space checks on our buffers later
    for (int i = 0; iter->Valid(); ++i) {
      leveldb::Slice ks = iter->key();
      leveldb::Slice vs = iter->value();

      // Actual allocated space depends on whether we're variable length, fixed non-zero length, 
      // or if we're fixed at zero bytes we just encode the whole thing
      int keyByteWidth = encodeKeys ? 4 + ks.size() : (keyWidth == 0 ? ks.size() : keyWidth);
      int valByteWidth = encodeVals ? 4 + vs.size() : (valWidth == 0 ? vs.size() : valWidth);

      // We have to stop if the combine key/value pair would exceed our chunk buffer
      if ((keyByteWidth + valByteWidth) > maxByteSize) {
        break;
      }
      
#ifdef DEBUG_CHUNKS
      std::cout << "Grabbing next pair" << std::endl;
#endif

      ++count;

      // Compact key and value onto the end of the buffer (big-endian)
      if (encodeKeys) {
        *(int *)(buffer + usedBufferSize) = htonl(ks.size());
        usedBufferSize += 4;
      }

      int keyDataWidth = encodeKeys ? ks.size() : (keyWidth == 0 ? ks.size() : keyWidth);
      memcpy(buffer + usedBufferSize, ks.data(), keyDataWidth);
      usedBufferSize += keyDataWidth;

#ifdef DEBUG_CHUNKS
      std::cout << "Got key" << std::endl;
#endif

      if (encodeVals) {
        *(int *)(buffer + usedBufferSize) = htonl(vs.size());
        usedBufferSize += 4;
      }

      int valDataWidth = encodeVals ? vs.size() : (valWidth == 0 ? vs.size() : valWidth);
      memcpy(buffer + usedBufferSize, vs.data(), valDataWidth);
      usedBufferSize += valDataWidth;

      iter->Next();
    }

#ifdef DEBUG_CHUNKS
    std::cout << "Iteration complete, setting meta" << std::endl;
#endif

    meta->byteLength = usedBufferSize;
    meta->pairLength = count;
}

#ifdef __cplusplus
}
#endif

