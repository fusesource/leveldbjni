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

import static org.fusesource.leveldbjni.JniDBFactory.asString;
import static org.fusesource.leveldbjni.JniDBFactory.bytes;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import junit.framework.TestCase;
import org.fusesource.leveldbjni.JniDBFactory;
import org.fusesource.leveldbjni.internal.JniDB;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBComparator;
import org.iq80.leveldb.DBException;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Logger;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.Range;
import org.iq80.leveldb.ReadOptions;
import org.iq80.leveldb.WriteBatch;
import org.iq80.leveldb.WriteOptions;
import org.junit.Test;

/**
 * A Unit test for the DB class implementation.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class DBTest extends TestCase {
    DBFactory factory = JniDBFactory.factory;

    File getTestDirectory(String name) throws IOException {
        File rc = new File(new File("test-data"), name);
        factory.destroy(rc, new Options().createIfMissing(true));
        rc.mkdirs();
        return rc;
    }

    @Test
    public void testOpen() throws IOException {

        Options options = new Options().createIfMissing(true);

        File path = getTestDirectory(getName());
        DB db = factory.open(path, options);

        db.close();

        // Try again.. this time we expect a failure since it exists.
        options = new Options().errorIfExists(true);
        try {
            factory.open(path, options);
            fail("Expected exception.");
        } catch (IOException e) {
        }

    }

    @Test
    public void testRepair() throws IOException, DBException {
        testCRUD();
        factory.repair(new File(new File("test-data"), getName()), new Options());
    }

    @Test
    public void testCRUD() throws IOException, DBException {

        Options options = new Options().createIfMissing(true);

        File path = getTestDirectory(getName());
        DB db = factory.open(path, options);

        WriteOptions wo = new WriteOptions().sync(false);
        ReadOptions ro = new ReadOptions().fillCache(true).verifyChecksums(true);

        db.put(bytes("Tampa"), bytes("green"));
        db.put(bytes("London"), bytes("red"));
        db.put(bytes("New York"), bytes("blue"));

        assertEquals(db.get(bytes("Tampa"), ro), bytes("green"));
        assertEquals(db.get(bytes("London"), ro), bytes("red"));
        assertEquals(db.get(bytes("New York"), ro), bytes("blue"));

        db.delete(bytes("New York"), wo);
        assertNull(db.get(bytes("New York"), ro));

        // leveldb does not consider deleting something that does not exist an error.
        db.delete(bytes("New York"), wo);

        db.close();
    }

    @Test
    public void testIterator() throws IOException, DBException {

        Options options = new Options().createIfMissing(true);

        File path = getTestDirectory(getName());
        DB db = factory.open(path, options);

        db.put(bytes("Tampa"), bytes("green"));
        db.put(bytes("London"), bytes("red"));
        db.put(bytes("New York"), bytes("blue"));

        ArrayList<String> expecting = new ArrayList<String>();
        expecting.add("London");
        expecting.add("New York");
        expecting.add("Tampa");

        ArrayList<String> actual = new ArrayList<String>();

        DBIterator iterator = db.iterator();
        for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
            actual.add(asString(iterator.peekNext().getKey()));
        }
        iterator.close();
        assertEquals(expecting, actual);

        db.close();
    }

    @Test
    public void testSnapshot() throws IOException, DBException {

        Options options = new Options().createIfMissing(true);

        File path = getTestDirectory(getName());
        DB db = factory.open(path, options);

        db.put(bytes("Tampa"), bytes("green"));
        db.put(bytes("London"), bytes("red"));
        db.delete(bytes("New York"));

        ReadOptions ro = new ReadOptions().snapshot(db.getSnapshot());

        db.put(bytes("New York"), bytes("blue"));

        assertEquals(db.get(bytes("Tampa"), ro), bytes("green"));
        assertEquals(db.get(bytes("London"), ro), bytes("red"));

        // Should not be able to get "New York" since it was added
        // after the snapshot
        assertNull(db.get(bytes("New York"), ro));

        ro.snapshot().close();

        // Now try again without the snapshot..
        ro.snapshot(null);
        assertEquals(db.get(bytes("New York"), ro), bytes("blue"));

        db.close();
    }

    @Test
    public void testWriteBatch() throws IOException, DBException {

        Options options = new Options().createIfMissing(true);

        File path = getTestDirectory(getName());
        DB db = factory.open(path, options);

        db.put(bytes("NA"), bytes("Na"));

        WriteBatch batch = db.createWriteBatch();
        batch.delete(bytes("NA"));
        batch.put(bytes("Tampa"), bytes("green"));
        batch.put(bytes("London"), bytes("red"));
        batch.put(bytes("New York"), bytes("blue"));
        db.write(batch);
        batch.close();

        ArrayList<String> expecting = new ArrayList<String>();
        expecting.add("London");
        expecting.add("New York");
        expecting.add("Tampa");

        ArrayList<String> actual = new ArrayList<String>();

        DBIterator iterator = db.iterator();
        for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
            actual.add(asString(iterator.peekNext().getKey()));
        }
        iterator.close();
        assertEquals(expecting, actual);

        db.close();
    }

    @Test
    public void testApproximateSizes() throws IOException, DBException {
        Options options = new Options().createIfMissing(true);

        File path = getTestDirectory(getName());
        DB db = factory.open(path, options);

        Random r = new Random(0);
        String data="";
        for(int i=0; i < 1024; i++) {
            data+= 'a'+r.nextInt(26);
        }
        for(int i=0; i < 5*1024; i++) {
            db.put(bytes("row"+i), bytes(data));
        }

        long[] approximateSizes = db.getApproximateSizes(new Range(bytes("row"), bytes("s")));
        assertNotNull(approximateSizes);
        assertEquals(1, approximateSizes.length);
        assertTrue("Wrong size", approximateSizes[0] > 0);

        db.close();
    }

    @Test
    public void testGetProperty() throws IOException, DBException {
        Options options = new Options().createIfMissing(true);

        File path = getTestDirectory(getName());
        DB db = factory.open(path, options);

        Random r = new Random(0);
        String data="";
        for(int i=0; i < 1024; i++) {
            data+= 'a'+r.nextInt(26);
        }
        for(int i=0; i < 5*1024; i++) {
            db.put(bytes("row"+i), bytes(data));
        }

        String stats = db.getProperty("leveldb.stats");
        assertNotNull(stats);
        assertTrue(stats.contains("Compactions"));

        db.close();
    }

    @Test
    public void testCustomComparator1() throws IOException, DBException {
        Options options = new Options().createIfMissing(true);
        options.comparator(new DBComparator() {

            public int compare(byte[] key1, byte[] key2) {
                return new String(key1).compareTo(new String(key2));
            }

            public String name() {
                return getName();
            }

            public byte[] findShortestSeparator(byte[] start, byte[] limit) {
                return start;
            }

            public byte[] findShortSuccessor(byte[] key) {
                return key;
            }
        });

        File path = getTestDirectory(getName());
        DB db = factory.open(path, options);

        ArrayList<String> expecting = new ArrayList<String>();
        for(int i=0; i < 26; i++) {
            String t = ""+ ((char) ('a' + i));
            expecting.add(t);
            db.put(bytes(t), bytes(t));
        }

        ArrayList<String> actual = new ArrayList<String>();

        DBIterator iterator = db.iterator();
        for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
            actual.add(asString(iterator.peekNext().getKey()));
        }
        iterator.close();
        assertEquals(expecting, actual);


        db.close();
    }

    @Test
    public void testCustomComparator2() throws IOException, DBException {
        Options options = new Options().createIfMissing(true);
        options.comparator(new DBComparator() {

            public int compare(byte[] key1, byte[] key2) {
                return new String(key1).compareTo(new String(key2)) * -1;
            }

            public String name() {
                return getName();
            }

            public byte[] findShortestSeparator(byte[] start, byte[] limit) {
                return start;
            }

            public byte[] findShortSuccessor(byte[] key) {
                return key;
            }
        });

        File path = getTestDirectory(getName());
        DB db = factory.open(path, options);

        ArrayList<String> expecting = new ArrayList<String>();
        for(int i=0; i < 26; i++) {
            String t = ""+ ((char) ('a' + i));
            expecting.add(t);
            db.put(bytes(t), bytes(t));
        }
        Collections.reverse(expecting);

        ArrayList<String> actual = new ArrayList<String>();
        DBIterator iterator = db.iterator();
        for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
            actual.add(asString(iterator.peekNext().getKey()));
        }
        iterator.close();
        assertEquals(expecting, actual);

        db.close();
    }

    @Test
    public void testLogger() throws IOException, InterruptedException, DBException {
        final List<String> messages = Collections.synchronizedList(new ArrayList<String>());

        Options options = new Options().createIfMissing(true);
        options.logger(new Logger() {
            public void log(String message) {
                messages.add(message);
            }
        });

        File path = getTestDirectory(getName());
        DB db = factory.open(path, options);

        for( int j=0; j < 5; j++) {
            Random r = new Random(0);
            String data="";
            for(int i=0; i < 1024; i++) {
                data+= 'a'+r.nextInt(26);
            }
            for(int i=0; i < 5*1024; i++) {
                db.put(bytes("row"+i), bytes(data));
            }
            Thread.sleep(100);
        }

        db.close();

        assertFalse(messages.isEmpty());

    }

    @Test
    public void testCompactRanges() throws IOException, InterruptedException, DBException {
        Options options = new Options().createIfMissing(true);
        File path = getTestDirectory(getName());
        DB db = factory.open(path, options);
        if( db instanceof JniDB) {
            Random r = new Random(0);
            String data="";
            for(int i=0; i < 1024; i++) {
                data+= 'a'+r.nextInt(26);
            }
            for(int i=0; i < 5*1024; i++) {
                db.put(bytes("row"+i), bytes(data));
            }
            for(int i=0; i < 5*1024; i++) {
                db.delete(bytes("row" + i));
            }

            String stats = db.getProperty("leveldb.stats");
            System.out.println(stats);

            //                                     Compactions
            //                         Level  Files Size(MB) Time(sec) Read(MB) Write(MB)
            //                         --------------------------------------------------
            assertFalse(stats.contains("1        0        0         0"));
            assertFalse(stats.contains("2        0        0         0"));

            // After the compaction, level 1 and 2 should not have any files in it..
            ((JniDB) db).compactRange(null, null);

            stats = db.getProperty("leveldb.stats");
            System.out.println(stats);
            assertTrue(stats.contains("1        0        0         0"));
            assertTrue(stats.contains("2        0        0         0"));

        }
        db.close();
    }

    public void assertEquals(byte[] arg1, byte[] arg2) {
        assertTrue(Arrays.equals(arg1, arg2));
    }

    @Test
    public void testIssue26() throws IOException {

        JniDBFactory.pushMemoryPool(1024 * 512);
        try {
            Options options = new Options();
            options.createIfMissing(true);

            DB db = factory.open(getTestDirectory(getName()), options);

            for (int i = 0; i < 1024 * 1024; i++) {
                byte[] key = ByteBuffer.allocate(4).putInt(i).array();
                byte[] value = ByteBuffer.allocate(4).putInt(-i).array();
                db.put(key, value);
                assertTrue(Arrays.equals(db.get(key), value));
            }
            db.close();
        } finally {
            JniDBFactory.popMemoryPool();
        }

    }

    @Test
    public void testIssue27() throws IOException {

        Options options = new Options();
        options.createIfMissing(true);
        DB db = factory.open(getTestDirectory(getName()), options);
        db.close();

        try {
            db.iterator();
            fail("Expected a DBException");
        } catch(DBException e) {
        }

    }

    @Test
    public void testIssue40_1() throws IOException {
        // incorrect behaviour.., but it shouldn't crash JVM:
        // test: seekToLast() -> next() -> prev()
        Options options = new Options().createIfMissing(true);

        File path = getTestDirectory(getName());
        DB db = factory.open(path, options);

        DBIterator it = db.iterator();
        it.seekToLast();

        try {
            it.next();
            fail("NoSuchElementException is expected");
        } catch (NoSuchElementException ex) {
        }
        try {
            it.prev(); // was SIGSEV
            fail("NoSuchElementException is expected");
        } catch (NoSuchElementException ex) {
        }

        it.close();
        db.close();
    }

    @Test
    public void testIssue40_2() throws IOException {
        // incorrect behaviour.., but it shouldn't crash JVM
        // test: seekToLast() -> next() -> peekPrev()
        Options options = new Options().createIfMissing(true);

        File path = getTestDirectory(getName());
        DB db = factory.open(path, options);

        DBIterator it = db.iterator();
        it.seekToLast();
        try {
            it.next();
            fail("NoSuchElementException is expected");
        } catch (NoSuchElementException ex) {
        }
        try {
            it.peekPrev(); // was SIGSEV
            fail("NoSuchElementException is expected");
        } catch (NoSuchElementException ex) {
        }

        it.close();
        db.close();
    }

    DBComparator byteComparator = new DBComparator() {

        public int compare(byte[] key1, byte[] key2) {
            return key1[0] - key2[0];
        }

        public String name() {
            return "ByteComparator";
        }

        public byte[] findShortestSeparator(byte[] start, byte[] limit) {
            return start;
        }

        public byte[] findShortSuccessor(byte[] key) {
            return key;
        }
    };

    public byte[] newKey(byte value) {
        final byte[] result = new byte[1];
        result[0] = value;
        return result;
    }

    public byte[] getData() {
        final byte[] result = new byte[10];
        for (int i = 0 ; i<10; i++) {
            result[i] = (byte) i;
        }
        return result;
    }

    @Test
    public void testIssue40_3() throws IOException {
        // test seek(after last record) -> peekPrev()/prev()
        Options options = new Options().createIfMissing(true);
        options.comparator(byteComparator);

        File path = getTestDirectory(getName());
        DB db = factory.open(path, options);

        byte[] key = newKey((byte) 10);
        byte[] big_key = newKey((byte) 20);

        byte[] data = getData();

        db.put(key, data);

        DBIterator it = db.iterator();
        it.seek(big_key);
        try {
            data = it.peekPrev().getValue();
            fail("NoSuchElementException is expected");
        } catch (NoSuchElementException ex) {
        }
        try {
            data = it.prev().getValue();
            fail("NoSuchElementException is expected");
        } catch (NoSuchElementException ex) {
        }

        it.close();
        db.close();
    }

    @Test
    public void testSeekAndIterator() throws IOException {
        final byte[] key_001 = newKey((byte) 1);
        final byte[] key_025 = newKey((byte) 25);
        final byte[] key_050 = newKey((byte) 50);
        final byte[] key_075 = newKey((byte) 75);
        final byte[] key_100 = newKey((byte) 100);
        final byte[] value_025 = bytes("25");
        final byte[] value_050 = bytes("50");
        final byte[] value_075 = bytes("75");

        Map.Entry<byte[], byte[]> entry;

        Options options = new Options().createIfMissing(true);
        options.comparator(byteComparator);

        File path = getTestDirectory(getName());
        DB db = factory.open(path, options);

        db.put(key_025, value_025);
        db.put(key_050, value_050);
        db.put(key_075, value_075);

        DBIterator it = db.iterator();

        //
        // check hasNext:
        //
        it.seek(key_001);
        assertTrue(it.hasNext());
        it.seek(key_025);
        assertTrue(it.hasNext());
        it.seek(key_050);
        assertTrue(it.hasNext());
        it.seek(key_075);
        assertTrue(it.hasNext());
        it.seek(key_100);
        assertFalse(it.hasNext());

        //
        // check next:
        //
        it.seek(key_001);
        entry = it.next();
        assertEquals(key_025, entry.getKey());
        assertEquals(value_025, entry.getValue());

        it.seek(key_025);
        entry = it.next();
        assertEquals(key_025, entry.getKey());
        assertEquals(value_025, entry.getValue());

        it.seek(key_050);
        entry = it.next();
        assertEquals(key_050, entry.getKey());
        assertEquals(value_050, entry.getValue());

        it.seek(key_075);
        entry = it.next();
        assertEquals(key_075, entry.getKey());
        assertEquals(value_075, entry.getValue());

        it.seek(key_100);
        try {
            it.next();
            fail("NoSuchElementException is expected");
        } catch (NoSuchElementException ex) {
        }

        //
        // check peekNext:
        //
        it.seek(key_001);
        entry = it.peekNext();
        assertEquals(key_025, entry.getKey());
        assertEquals(value_025, entry.getValue());

        it.seek(key_025);
        entry = it.peekNext();
        assertEquals(key_025, entry.getKey());
        assertEquals(value_025, entry.getValue());

        it.seek(key_050);
        entry = it.peekNext();
        assertEquals(key_050, entry.getKey());
        assertEquals(value_050, entry.getValue());

        it.seek(key_075);
        entry = it.peekNext();
        assertEquals(key_075, entry.getKey());
        assertEquals(value_075, entry.getValue());

        it.seek(key_100);
        try {
            it.peekNext();
            fail("NoSuchElementException is expected");
        } catch (NoSuchElementException ex) {
        }

        //
        // check hasPrev
        //
        it.seek(key_001);
        assertFalse(it.hasPrev());
        it.seek(key_025);
        assertFalse(it.hasPrev());
        it.seek(key_050);
        assertTrue(it.hasPrev());
        it.seek(key_075);
        assertTrue(it.hasPrev());
        it.seek(key_100);
        assertFalse(it.hasPrev()); // TODO: Expected result?

        //
        // check prev:
        //
        it.seek(key_001);
        try {
            it.prev();
            fail("NoSuchElementException is expected");
        } catch (NoSuchElementException ex) {
        }

        it.seek(key_025);
        try {
            it.prev();
            fail("NoSuchElementException is expected");
        } catch (NoSuchElementException ex) {
        }

        it.seek(key_050);
        entry = it.prev();
        assertEquals(key_025, entry.getKey());
        assertEquals(value_025, entry.getValue());

        it.seek(key_075);
        entry = it.prev();
        assertEquals(key_050, entry.getKey());
        assertEquals(value_050, entry.getValue());

        it.seek(key_100);
        try {
            it.prev(); // TODO: Expected result?
            fail("NoSuchElementException is expected");
        } catch (NoSuchElementException ex) {
        }

        //
        // check peekPrev:
        //
        it.seek(key_001);
        try {
            it.peekPrev();
            fail("NoSuchElementException is expected");
        } catch (NoSuchElementException ex) {
        }

        it.seek(key_025);
        try {
            it.peekPrev();
            fail("NoSuchElementException is expected");
        } catch (NoSuchElementException ex) {
        }

        it.seek(key_050);
        entry = it.peekPrev();
        assertEquals(key_025, entry.getKey());
        assertEquals(value_025, entry.getValue());

        it.seek(key_075);
        entry = it.peekPrev();
        assertEquals(key_050, entry.getKey());
        assertEquals(value_050, entry.getValue());

        it.seek(key_100);
        try {
            it.peekPrev(); // TODO: Expected result?
            fail("NoSuchElementException is expected");
        } catch (NoSuchElementException ex) {
        }

        it.close();
        db.close();
    }

    @Test
    public void testIteratorNegative() throws IOException {
        final byte[] key_001 = newKey((byte) 1);
        final byte[] key_025 = newKey((byte) 25);
        final byte[] key_050 = newKey((byte) 50);
        final byte[] key_075 = newKey((byte) 75);
        final byte[] key_100 = newKey((byte) 100);
        final byte[] value_025 = bytes("25");
        final byte[] value_050 = bytes("50");
        final byte[] value_075 = bytes("75");

        Map.Entry<byte[], byte[]> entry;

        Options options = new Options().createIfMissing(true);
        options.comparator(byteComparator);

        File path = getTestDirectory(getName());
        DB db = factory.open(path, options);

        db.put(key_025, value_025);
        db.put(key_050, value_050);
        db.put(key_075, value_075);

        DBIterator it = db.iterator();

        //
        // check next:
        //
        it.seekToFirst();
        entry = it.next();
        assertEquals(key_025, entry.getKey());
        assertEquals(value_025, entry.getValue());

        entry = it.next();
        assertEquals(key_050, entry.getKey());
        assertEquals(value_050, entry.getValue());

        entry = it.next();
        assertEquals(key_075, entry.getKey());
        assertEquals(value_075, entry.getValue());

        try {
            it.next();
            fail("NoSuchElementException is expected");
        } catch (NoSuchElementException ex) {
        }

        //
        // check prev:
        //
        it.seekToLast();
        entry = it.prev();
        assertEquals(key_050, entry.getKey());
        assertEquals(value_050, entry.getValue());

        entry = it.prev();
        assertEquals(key_025, entry.getKey());
        assertEquals(value_025, entry.getValue());

        try {
            it.prev();
            fail("NoSuchElementException is expected");
        } catch (NoSuchElementException ex) {
        }

        it.close();
        db.close();
    }
}
