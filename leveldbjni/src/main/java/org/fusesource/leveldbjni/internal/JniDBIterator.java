/*
 * Copyright (C) 2011, FuseSource Corp.  All rights reserved.
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
package org.fusesource.leveldbjni.internal;

import org.fusesource.leveldbjni.internal.NativeDB;
import org.fusesource.leveldbjni.internal.NativeIterator;
import org.iq80.leveldb.DBIterator;

import java.util.AbstractMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 * @author Jaap-Jan van der Veen <jaap-jan@shockmedia.nl>
 */
public class JniDBIterator implements DBIterator {

    private final NativeIterator iterator;
    private boolean atEnd;

    JniDBIterator(NativeIterator iterator) {
        this.iterator = iterator;
        seekToFirst();
    }

    public void close() {
        iterator.delete();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public void seek(byte[] key) {
        try {
            iterator.seek(key);
        } catch (NativeDB.DBException e) {
            if( e.isNotFound() ) {
                throw new NoSuchElementException();
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public void seekToFirst() {
        iterator.seekToFirst();
        atEnd = false;
    }

    public void seekToLast() {
        iterator.seekToLast();
        atEnd = true;
    }


    public Map.Entry<byte[], byte[]> peekNext() {
        checkExists();
        try {
            return new AbstractMap.SimpleImmutableEntry<byte[],byte[]>(iterator.key(), iterator.value());
        } catch (NativeDB.DBException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasNext() {
        return !atEnd && iterator.isValid();
    }

    public Map.Entry<byte[], byte[]> next() {
        if (atEnd) {
            throw new NoSuchElementException();
        }
        Map.Entry<byte[], byte[]> rc = peekNext();
        moveNext();
        if (!iterator.isValid()) {
            seekToLast();
        }
        return rc;
    }

    public boolean hasPrev() {
        if (!iterator.isValid()) {
            return false;
        }
        movePrev();

        try {
            return iterator.isValid();
        } finally {
            if (iterator.isValid()) {
                moveNext();
            } else {
                seekToFirst();
            }
        }
    }

    public Map.Entry<byte[], byte[]> peekPrev() {
        checkExists();
        try {
            return prev();
        } finally {
            moveNext();
            if (!iterator.isValid()) {
                seekToLast();
            }
        }
    }

    public Map.Entry<byte[], byte[]> prev() {
        if (atEnd) {
            atEnd = false;
        } else {
            movePrev();
            if (!iterator.isValid()) {
                seekToFirst();
                throw new NoSuchElementException();
            }
        }
        return peekNext();
    }

    /**
     * Checks if the iterator points to a valid element.
     * 
     * @throws NoSuchElementException
     *             when the iterator doesn't point to a valid element
     */
    private void checkExists() {
        if (!iterator.isValid()) {
            throw new NoSuchElementException();
        }
    }

    /**
     * Moves iterator to the previous element.
     * 
     * @throws RuntimeException
     *             when an exception occurred in the underlying database
     */
    private void movePrev() {
        try {
            iterator.prev();
        } catch (NativeDB.DBException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Moves iterator to the next element.
     * 
     * @throws RuntimeException
     *             when an exception occurred in the underlying database
     */
    private void moveNext() {
        try {
            iterator.next();
        } catch (NativeDB.DBException e) {
            throw new RuntimeException(e);
        }
    }
}
