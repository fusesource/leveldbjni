/*
 * Copyright (C) 2011, FuseSource Corp.  All rights reserved.
 *
 *     http://fusesource.com
 *
 * The software in this package is published under the terms of the
 * CDDL license a copy of which has been included with this distribution
 * in the license.txt file.
 */
package org.fusesource.leveldbjni;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static org.fusesource.leveldbjni.DB.*;

/**
 * A Unit test for the DB class implementation.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class DBTest extends TestCase {

    File getTestDirectory(String name) {
        File rc = new File(new File("test-data"), name);
        delete(rc);
        rc.mkdirs();
        return rc;
    }

    private void delete(File rc) {
        if (rc.isDirectory()) {
            for (File f : rc.listFiles()) {
                delete(f);
            }
        }
        rc.delete();
    }

    @Test
    public void testOpen() throws IOException {

        Options options = new Options().createIfMissing(true);

        File path = getTestDirectory(getName());
        DB db = DB.open(options, path);

        db.delete();

        // Try again.. this time we expect a failure since it exists.
        options = new Options().errorIfExists(true);
        try {
            DB.open(options, path);
            fail("Expected exception.");
        } catch (DB.DBException e) {
        }

    }

    @Test
    public void testCRUD() throws IOException {

        Options options = new Options().createIfMissing(true);

        File path = getTestDirectory(getName());
        DB db = DB.open(options, path);

        WriteOptions wo = new WriteOptions().sync(false);
        ReadOptions ro = new ReadOptions().fillCache(true).verifyChecksums(true);


        db.put(wo, bytes("Tampa"), bytes("green"));
        db.put(wo, bytes("London"), bytes("red"));
        db.put(wo, bytes("New York"), bytes("blue"));

        assertEquals(db.get(ro, bytes("Tampa")), bytes("green"));
        assertEquals(db.get(ro, bytes("London")), bytes("red"));
        assertEquals(db.get(ro, bytes("New York")), bytes("blue"));

        db.delete(wo, bytes("New York"));
        try {
            db.get(ro, bytes("New York"));
            fail("Expecting exception");
        } catch (DB.DBException e) {
        }

        // leveldb does not consider deleting something that does not exist an error.
        db.delete(wo, bytes("New York"));

        db.delete();
    }

    @Test
    public void testIterator() throws IOException {

        Options options = new Options().createIfMissing(true);

        File path = getTestDirectory(getName());
        DB db = DB.open(options, path);

        WriteOptions wo = new WriteOptions();
        ReadOptions ro = new ReadOptions();

        db.put(wo, bytes("Tampa"), bytes("green"));
        db.put(wo, bytes("London"), bytes("red"));
        db.put(wo, bytes("New York"), bytes("blue"));

        ArrayList<String> expecting = new ArrayList<String>();
        expecting.add("London");
        expecting.add("New York");
        expecting.add("Tampa");

        ArrayList<String> actual = new ArrayList<String>();

        Iterator iterator = db.iterator(ro);
        for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
            actual.add(asString(iterator.key()));
        }
        iterator.delete();
        assertEquals(expecting, actual);

        db.delete();
    }

    @Test
    public void testSnapshot() throws IOException {

        Options options = new Options().createIfMissing(true);

        File path = getTestDirectory(getName());
        DB db = DB.open(options, path);

        WriteOptions wo = new WriteOptions().sync(false);

        db.put(wo, bytes("Tampa"), bytes("green"));
        db.put(wo, bytes("London"), bytes("red"));

        ReadOptions ro = new ReadOptions().snapshot(db.getSnapshot());

        db.put(wo, bytes("New York"), bytes("blue"));

        assertEquals(db.get(ro, bytes("Tampa")), bytes("green"));
        assertEquals(db.get(ro, bytes("London")), bytes("red"));

        // Should not be able to get "New York" since it was added
        // after the snapshot
        try {
            db.get(ro, bytes("New York"));
            fail("Expecting exception");
        } catch (DB.DBException e) {
        }

        db.releaseSnapshot(ro.snapshot());

        // Now try again without the snapshot..
        ro.snapshot(null);
        assertEquals(db.get(ro, bytes("New York")), bytes("blue"));

        db.delete();
    }

    @Test
    public void testWriteBatch() throws IOException {

        Options options = new Options().createIfMissing(true);

        File path = getTestDirectory(getName());
        DB db = DB.open(options, path);
        WriteOptions wo = new WriteOptions().sync(false);

        db.put(wo, bytes("NA"), bytes("Na"));

        WriteBatch batch = new WriteBatch();
        batch.delete(bytes("NA"));
        batch.put(bytes("Tampa"), bytes("green"));
        batch.put(bytes("London"), bytes("red"));
        batch.put(bytes("New York"), bytes("blue"));
        db.write(wo, batch);
        batch.delete();

        ArrayList<String> expecting = new ArrayList<String>();
        expecting.add("London");
        expecting.add("New York");
        expecting.add("Tampa");

        ArrayList<String> actual = new ArrayList<String>();

        Iterator iterator = db.iterator(new ReadOptions());
        for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
            actual.add(asString(iterator.key()));
        }
        iterator.delete();
        assertEquals(expecting, actual);

        db.delete();
    }

    @Test
    public void testApproximateSizes() throws IOException {
        Options options = new Options().createIfMissing(true);

        File path = getTestDirectory(getName());
        DB db = DB.open(options, path);
        WriteOptions wo = new WriteOptions().sync(false);

        Random r = new Random(0);
        String data="";
        for(int i=0; i < 1024; i++) {
            data+= 'a'+r.nextInt(26);
        }
        for(int i=0; i < 5*1024; i++) {
            db.put(wo, bytes("row"+i), bytes(data));
        }

        long[] approximateSizes = db.getApproximateSizes(new Range(bytes("row"), bytes("s")));
        assertNotNull(approximateSizes);
        assertEquals(1, approximateSizes.length);
        assertTrue("Wrong size", approximateSizes[0] > 0);

        db.delete();
    }

    @Test
    public void testGetProperty() throws IOException {
        Options options = new Options().createIfMissing(true);

        File path = getTestDirectory(getName());
        DB db = DB.open(options, path);
        WriteOptions wo = new WriteOptions().sync(false);

        Random r = new Random(0);
        String data="";
        for(int i=0; i < 1024; i++) {
            data+= 'a'+r.nextInt(26);
        }
        for(int i=0; i < 5*1024; i++) {
            db.put(wo, bytes("row"+i), bytes(data));
        }

        String stats = db.getProperty("leveldb.stats");
        assertNotNull(stats);
        assertTrue(stats.contains("Compactions"));

        db.delete();
    }

    public void assertEquals(byte[] arg1, byte[] arg2) {
        assertTrue(Arrays.equals(arg1, arg2));
    }
}
