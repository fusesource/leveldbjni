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
package org.fusesource.leveldbjni.test;

import static org.fusesource.leveldbjni.JniDBFactory.factory;
import static org.junit.Assert.*;

import java.io.*;
import java.util.NoSuchElementException;

import org.iq80.leveldb.*;
import org.junit.*;
import org.junit.rules.*;

/**
 * A Unit test for the JniDBIterator class implementation.
 *
 * @author Jaap-Jan van der Veen <jaap-jan@shockmedia.nl>
 */
public class TestIterator {
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    
    DB db;
    
    /*
     * Tests will have to call this because the temporary folder is created just
     * the test is run
     */
    public void setUp() {
        try {
            Options options = new Options().createIfMissing(true);
            db = factory.open(testFolder.getRoot(), options);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void tearDown() throws IOException {
        if (null != db) {
            db.close();
        }
    }

    @Test
    public void testPeekNextUnitialized() {
        setUp();
        fillDatabase();

        // We initialize the iterator to the first element, so it always points
        // to a valid element when not empty
        db.iterator().peekNext();
    }

    @Test(expected = NoSuchElementException.class)
    public void testPeekPrevUnitialized() {
        setUp();
        fillDatabase();

        // We initialize the iterator to the first element, so it points to the
        // first valid element when not empty
        db.iterator().peekPrev();
    }

    @Test
    public void testEmptyHasNext() {
        setUp();
        DBIterator it = db.iterator();
        it.seekToFirst();

        assertFalse(it.hasNext());
    }

    @Test
    public void testEmptyHasPrev() {
        setUp();
        DBIterator it = db.iterator();
        it.seekToLast();

        assertFalse(it.hasPrev());
    }

    @Test
    public void testOneElementNext() {
        setUp();
        db.put(toByteArray((byte) 1), toByteArray());

        DBIterator it = db.iterator();
        it.seekToFirst();

        assertEquals(1, it.next().getKey()[0]);
    }

    @Test
    public void testOneElementPrev() {
        setUp();
        db.put(toByteArray((byte) 1), toByteArray());

        DBIterator it = db.iterator();
        it.seekToLast();

        assertEquals(1, it.prev().getKey()[0]);
    }

    @Test
    public void testForwardIterator() {
        setUp();
        fillDatabase();
        DBIterator it = db.iterator();
        StringBuffer sb = new StringBuffer();

        for (it.seekToFirst(); it.hasNext();) {
            sb.append(it.next().getKey()[0]);
            sb.append(' ');
        }

        assertEquals("1 2 3 4 ", sb.toString());
    }

    @Test
    public void testBackwardIterator() {
        setUp();
        fillDatabase();
        DBIterator it = db.iterator();
        StringBuffer sb = new StringBuffer();

        for (it.seekToLast(); it.hasPrev();) {
            sb.append(it.prev().getKey()[0]);
            sb.append(' ');
        }

        assertEquals("4 3 2 1 ", sb.toString());
    }

    @Test
    public void testHasNextAtEnd() {
        setUp();
        fillDatabase();
        DBIterator it = db.iterator();
        it.seekToLast();
        assertFalse(it.hasNext());
    }

    @Test
    public void testHasPrevAtStart() {
        setUp();
        fillDatabase();
        DBIterator it = db.iterator();
        it.seekToFirst();
        assertFalse(it.hasPrev());
    }

    @Test(expected = NoSuchElementException.class)
    public void testNextAtEnd() {
        setUp();
        fillDatabase();
        DBIterator it = db.iterator();
        it.seekToLast();
        it.next();
    }

    @Test(expected = NoSuchElementException.class)
    public void testPrevAtStart() {
        setUp();
        fillDatabase();
        DBIterator it = db.iterator();
        it.seekToFirst();
        it.prev();
    }

    @Test(expected = NoSuchElementException.class)
    public void testEmptyPeekNext() {
        setUp();
        DBIterator it = db.iterator();
        it.seekToFirst();
        it.peekNext();
    }

    @Test(expected = NoSuchElementException.class)
    public void testEmptyPeekPrev() {
        setUp();
        DBIterator it = db.iterator();
        it.seekToLast();
        it.peekPrev();
    }

    @Test
    public void testOneElementPeekNext() {
        setUp();
        db.put(toByteArray((byte) 1), toByteArray());
        DBIterator it = db.iterator();
        it.seekToFirst();
    
        assertEquals(1, it.peekNext().getKey()[0]);
    }

    @Test
    public void testOneElementPeekPrev() {
        setUp();
        db.put(toByteArray((byte) 1), toByteArray());
        DBIterator it = db.iterator();
        it.seekToLast();
    
        assertEquals(1, it.peekPrev().getKey()[0]);
    }

    @Test
    public void testPeekNext() {
        setUp();
        db.put(toByteArray((byte) 1), toByteArray());

        StringBuffer sb = new StringBuffer();
        DBIterator it = db.iterator();
        it.seekToFirst();

        sb.append(it.peekNext().getKey()[0]);
        sb.append(' ');
        sb.append(it.peekNext().getKey()[0]);
        sb.append(' ');

        assertEquals("1 1 ", sb.toString());
    }

    @Test
    public void testPeekPrev() {
        setUp();
        fillDatabase();

        StringBuffer sb = new StringBuffer();
        DBIterator it = db.iterator();
        it.seekToLast();

        sb.append(it.peekPrev().getKey()[0]);
        sb.append(' ');
        sb.append(it.peekPrev().getKey()[0]);
        sb.append(' ');

        assertEquals("4 4 ", sb.toString());
    }

    @Test
    public void testForwardAndBackwardsIterator() {
        setUp();
        fillDatabase();
        DBIterator it = db.iterator();
        StringBuffer sb = new StringBuffer();
    
        it.seekToFirst();
        
        // first move to second item
        sb.append(it.next().getKey()[0]);
        sb.append(' ');
        
        // then move iterator back and forth three times
        sb.append(it.next().getKey()[0]);
        sb.append(' ');
        sb.append(it.prev().getKey()[0]);
        sb.append(' ');
    
        sb.append(it.next().getKey()[0]);
        sb.append(' ');
        sb.append(it.prev().getKey()[0]);
        sb.append(' ');
    
        sb.append(it.next().getKey()[0]);
        sb.append(' ');
        sb.append(it.prev().getKey()[0]);
        sb.append(' ');
    
        assertEquals("1 2 2 2 2 2 2 ", sb.toString());
    }

    @Test
    public void testForwardAndPeekBackwardsIterator() {
        setUp();
        fillDatabase();
        DBIterator it = db.iterator();
        StringBuffer sb = new StringBuffer();

        it.seekToFirst();

        // first move to second item
        sb.append(it.next().getKey()[0]);
        sb.append(' ');

        // then peek back twice
        sb.append(it.peekPrev().getKey()[0]);
        sb.append(' ');
        sb.append(it.peekPrev().getKey()[0]);
        sb.append(' ');

        assertEquals("1 1 1 ", sb.toString());
    }

    @Test
    public void testBackwardAndPeekForwardIterator() {
        setUp();
        fillDatabase();
        DBIterator it = db.iterator();
        StringBuffer sb = new StringBuffer();

        it.seekToLast();

        // first move to second last item
        sb.append(it.prev().getKey()[0]);
        sb.append(' ');

        // then peek at next item twice
        sb.append(it.peekNext().getKey()[0]);
        sb.append(' ');
        sb.append(it.peekNext().getKey()[0]);
        sb.append(' ');

        assertEquals("4 4 4 ", sb.toString());
    }

    @Ignore // seems to be wrong
    @Test(expected = NoSuchElementException.class)
    public void testSeekNotFound() {
        setUp();
        fillDatabase();
        DBIterator it = db.iterator();
        it.seek(toByteArray((byte) 5));
    }

    @Ignore // is wrong
    @Test
    public void testSeekFound() {
        setUp();
        fillDatabase();
        DBIterator it = db.iterator();
        it.seek(toByteArray((byte) 2));
    
        assertEquals(3, it.peekNext().getKey()[0]);
    }

    @Test
    public void testClose() {
        setUp();
        // shouldn't be a problem, right?
        try {
            db.iterator().close();
        } catch (Exception e) {
            fail();
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        setUp();
        // free coverage
        db.iterator().remove();
    }

    private void fillDatabase() {
        for (byte i = 1; i <= 4; i++) {
            db.put(toByteArray(i), toByteArray());
        }
    }

    /**
     * @return
     */
    private byte[] toByteArray(byte... b) {
        return b;
    }
}