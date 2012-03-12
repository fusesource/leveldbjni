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
package org.fusesource.leveldbjni;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class represents a "chunk" of key/value pairs. Consolidating 
 * multiple pairs into a single object instance reduces the penalty
 * for crossing the JNI boundary when talking to the underlying leveldb
 * library.
 *
 * Although an Iterator interface is provided for convenience,
 * all members are public to allow for optimal performance in
 * use cases that can directly consume the byte stream.
 *
 * Key and value Data is encoded into the buffer based on selectable encodings.
 */
public class KeyValueChunk {
    /** The number of pairs contained in this chunk */
    public int pairLength;

    /** The encoding to use for key data extraction */
    public DataWidth keyWidth;

    /** The encoding to use for value data extraction */
    public DataWidth valueWidth;

    /** The key backing store */
    public ByteBuffer keyData;

    /** The value backing store */
    public ByteBuffer valueData;

    public KeyValueChunk(ByteBuffer keyData, ByteBuffer valueData, int pairLength, DataWidth keyWidth, DataWidth valueWidth) {
        this.keyData = keyData;
        this.valueData = valueData;
        this.pairLength = pairLength;
        this.keyWidth = keyWidth;
        this.valueWidth = valueWidth;
    }

    public static class KeyValuePair {
        private byte[] key;
        private byte[] value;

        private KeyValuePair(byte[] key, byte[] value) {
            this.key = key;
            this.value = value;
        }

        public byte[] getKey() {
            return key;
        }

        public byte[] getValue() {
            return value;
        }
    }

    public int getSize() {
        return pairLength;
    }

    public Iterator<KeyValuePair> getIterator() {
        return new Iterator<KeyValuePair>() {
            private ByteBuffer keyBacking = keyData.asReadOnlyBuffer();
            private ByteBuffer valueBacking = valueData.asReadOnlyBuffer();

            public boolean hasNext() {
                return keyBacking.hasRemaining();
            }

            public KeyValuePair next() {
                if (! keyBacking.hasRemaining()) {
                    throw new NoSuchElementException("Chunk limit reached");
                }

                int keyLen = keyWidth.getWidth(keyBacking);
                byte[] key = new byte[keyLen];
                keyBacking.get(key, 0, keyLen);
                
                int valueLen = valueWidth.getWidth(valueBacking);
                byte[] value = new byte[valueLen];
                valueBacking.get(value, 0, valueLen);

                return new KeyValuePair(key, value);
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
