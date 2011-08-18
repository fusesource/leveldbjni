/*
 * Copyright (C) 2011, FuseSource Corp.  All rights reserved.
 *
 *     http://fusesource.com
 *
 * The software in this package is published under the terms of the
 * CDDL license a copy of which has been included with this distribution
 * in the license.txt file.
 */
package org.fusesource.leveldbjni.test;

import junit.framework.TestCase;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.fusesource.leveldbjni.JniDBFactory.asString;
import static org.fusesource.leveldbjni.JniDBFactory.bytes;

/**
 * A Unit test for the DB class implementation.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class DBTest extends TestCase {
    DBFactory factory = JniDBFactory.factory;

    File getTestDirectory(String name) throws IOException {
        File rc = new File(new File("test-data"), name);
        factory.destroy(rc, new Options().setCreateIfMissing(true));
        rc.mkdirs();
        return rc;
    }

    @Test
    public void testOpen() throws IOException {

        Options options = new Options().setCreateIfMissing(true);

        File path = getTestDirectory(getName());
        DB db = factory.open(path, options);

        db.close();

        // Try again.. this time we expect a failure since it exists.
        options = new Options().setErrorIfExists(true);
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

        Options options = new Options().setCreateIfMissing(true);

        File path = getTestDirectory(getName());
        DB db = factory.open(path, options);

        WriteOptions wo = new WriteOptions().setSync(false);
        ReadOptions ro = new ReadOptions().setFillCache(true).setVerifyChecksums(true);

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

        Options options = new Options().setCreateIfMissing(true);

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

        Options options = new Options().setCreateIfMissing(true);

        File path = getTestDirectory(getName());
        DB db = factory.open(path, options);

        db.put(bytes("Tampa"), bytes("green"));
        db.put(bytes("London"), bytes("red"));

        ReadOptions ro = new ReadOptions().setSnapshot(db.getSnapshot());

        db.put(bytes("New York"), bytes("blue"));

        assertEquals(db.get(bytes("Tampa"), ro), bytes("green"));
        assertEquals(db.get(bytes("London"), ro), bytes("red"));

        // Should not be able to get "New York" since it was added
        // after the snapshot
        assertNull(db.get(bytes("New York"), ro));

        ro.getSnapshot().close();

        // Now try again without the snapshot..
        ro.setSnapshot(null);
        assertEquals(db.get(bytes("New York"), ro), bytes("blue"));

        db.close();
    }

    @Test
    public void testWriteBatch() throws IOException, DBException {

        Options options = new Options().setCreateIfMissing(true);

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
        Options options = new Options().setCreateIfMissing(true);

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
        Options options = new Options().setCreateIfMissing(true);

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
        Options options = new Options().setCreateIfMissing(true);
        options.setComparator(new DBComparator() {

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
        Options options = new Options().setCreateIfMissing(true);
        options.setComparator(new DBComparator() {

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

        Options options = new Options().setCreateIfMissing(true);
        options.setLogger(new Logger() {
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

    public void assertEquals(byte[] arg1, byte[] arg2) {
        assertTrue(Arrays.equals(arg1, arg2));
    }
}
