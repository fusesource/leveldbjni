/*
 * Copyright (C) 2011, FuseSource Corp.  All rights reserved.
 *
 *     http://fusesource.com
 *
 * The software in this package is published under the terms of the
 * CDDL license a copy of which has been included with this distribution
 * in the license.txt file.
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
 */
public class JniDBIterator implements DBIterator {

    private final NativeIterator iterator;

    JniDBIterator(NativeIterator iterator) {
        this.iterator = iterator;
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
    }

    public void seekToLast() {
        iterator.seekToLast();
    }


    public Map.Entry<byte[], byte[]> peekNext() {
        if(!iterator.isValid()) {
            throw new NoSuchElementException();
        }
        try {
            return new AbstractMap.SimpleImmutableEntry<byte[],byte[]>(iterator.key(), iterator.value());
        } catch (NativeDB.DBException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasNext() {
        return iterator.isValid();
    }

    public Map.Entry<byte[], byte[]> next() {
        Map.Entry<byte[], byte[]> rc = peekNext();
        try {
            iterator.next();
        } catch (NativeDB.DBException e) {
            throw new RuntimeException(e);
        }
        return rc;
    }

    public boolean hasPrev() {
        try {
            iterator.prev();
            try {
                return iterator.isValid();
            } finally {
                iterator.next();
            }
        } catch (NativeDB.DBException e) {
            throw new RuntimeException(e);
        }
    }

    public Map.Entry<byte[], byte[]> peekPrev() {
        try {
            iterator.prev();
            try {
                return peekNext();
            } finally {
                iterator.next();
            }
        } catch (NativeDB.DBException e) {
            throw new RuntimeException(e);
        }
    }

    public Map.Entry<byte[], byte[]> prev() {
        Map.Entry<byte[], byte[]> rc = peekPrev();
        try {
            iterator.prev();
        } catch (NativeDB.DBException e) {
            throw new RuntimeException(e);
        }
        return rc;
    }


}
