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

/*
    This class performs a somewhat tricky mapping of semantics.

    The NativeIterator is always either "valid" and pointing at specific
    key/value pair, or it is invalid. That's it.

    In contrast, the DBIterator is more like a bi-directional cursor that only
    takes positions *between* key/value pairs (including before the first and
    after the last key/value pair).

    Therefore, we must keep track of whether the NativeIterator, if valid,
    points to the key/value pair before or after our current cursor position.
    This is done via this.position.

    INVARIANTS:

        - The iterator is invalid IFF the iteration is empty
        - If the iterator is valid:
            - If position == RIGHT, we are to the right of the key/value pair
            - If position == LEFT, we are to the left of the key/value pair

*/

    private static final boolean LEFT = false;
    private static final boolean RIGHT = true;

    private final NativeIterator iterator;
    private boolean position;

    JniDBIterator(NativeIterator iterator) {
        this.iterator = iterator;
        this.seekToFirst();
    }

// Public methods

    @Override
    public void close() {
        this.iterator.delete();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void seekToFirst() {
        this.iterator.seekToFirst();
        this.position = LEFT;
    }

    @Override
    public void seekToLast() {
        this.iterator.seekToLast();
        this.position = RIGHT;
    }

    @Override
    public void seek(byte[] key) {
        try {
            this.iterator.seek(key);
        } catch (NativeDB.DBException e) {
            throw new RuntimeException(e);
        }
        if (!this.iterator.isValid()) {
            this.seekToLast();
            return;
        }
        this.position = LEFT;
    }

    @Override
    public boolean hasNext() {
        if (!this.iterator.isValid())
            return false;
        if (this.position == LEFT)
            return true;
        if (!this.forward())
            return false;
        assert this.iterator.isValid();
        this.position = LEFT;
        return true;
    }

    @Override
    public boolean hasPrev() {
        if (!this.iterator.isValid())
            return false;
        if (this.position == RIGHT)
            return true;
        if (!this.backward())
            return false;
        assert this.iterator.isValid();
        this.position = RIGHT;
        return true;
    }

    @Override
    public Map.Entry<byte[], byte[]> peekNext() {
        if (!this.hasNext())
            throw new NoSuchElementException();
        assert this.position == LEFT;
        assert this.iterator.isValid();
        return this.readCursor();
    }

    @Override
    public Map.Entry<byte[], byte[]> peekPrev() {
        if (!this.hasPrev())
            throw new NoSuchElementException();
        assert this.position == RIGHT;
        assert this.iterator.isValid();
        return this.readCursor();
    }

    @Override
    public Map.Entry<byte[], byte[]> next() {
        if (!this.hasNext())
            throw new NoSuchElementException();
        assert this.iterator.isValid();
        assert this.position == LEFT;
        final Map.Entry<byte[], byte[]> entry = this.readCursor();
        this.forward();
        return entry;
    }

    @Override
    public Map.Entry<byte[], byte[]> prev() {
        if (!this.hasPrev())
            throw new NoSuchElementException();
        assert this.iterator.isValid();
        assert this.position == RIGHT;
        final Map.Entry<byte[], byte[]> entry = this.readCursor();
        this.backward();
        return entry;
    }

// Internal methods

    /**
     * Read current iterator key/value pair.
     *
     * <p>
     * This method assumes that the iterator is valid on entry.
     */
    private Map.Entry<byte[], byte[]> readCursor() {
        assert this.iterator.isValid();
        try {
            return new AbstractMap.SimpleImmutableEntry<byte[],byte[]>(iterator.key(), iterator.value());
        } catch (NativeDB.DBException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Try to move the iterator backward.
     *
     * <p>
     * This method assumes that the iterator is valid on entry.
     *
     * <p>
     * On return, either the iterator is valid and has been successfully
     * moved backward, or there are no key/value pairs below the current value,
     * in which case the iterator will point to the very first entry, if any.
     *
     * @return true if iterator was successfully moved backward (and is therefore valid)
     */
    private boolean backward() {
        assert iterator.isValid();
        try {
            iterator.prev();
        } catch (NativeDB.DBException e) {
            throw new RuntimeException(e);
        }
        if (!iterator.isValid()) {
            seekToFirst();
            return false;
        }
        return true;
    }

    /**
     * Try to move the iterator forward.
     *
     * <p>
     * This method assumes that the iterator is valid on entry.
     *
     * <p>
     * On return, either the iterator is valid and has been successfully
     * moved forward, or there are no key/value pairs above the current value,
     * in which case the iterator will point to the very last entry, if any.
     *
     * @return true if iterator was successfully moved forward (and is therefore valid)
     */
    private boolean forward() {
        assert iterator.isValid();
        try {
            iterator.next();
        } catch (NativeDB.DBException e) {
            throw new RuntimeException(e);
        }
        if (!iterator.isValid()) {
            seekToLast();
            return false;
        }
        return true;
    }
}
