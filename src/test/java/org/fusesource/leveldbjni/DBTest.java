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
import java.util.Arrays;

/**
 * A Unit test for the DB class implementation.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class DBTest extends TestCase {

    File getTestDirectory(String name) {
        File rc = new File(new File("test-data"), name);
        rc.mkdirs();
        return rc;
    }

    @Test
    public void testOpen() throws IOException {

        Options options = new Options();
        options.setCreateIfMissing(true);

        File path = getTestDirectory("db1");
        System.out.println("Creating DB at: "+path.getCanonicalPath());
        DB db = DB.open(options, path);

        options.delete();
        db.delete();

        // Try again.. this time we expect a failure since it exists.
        options = new Options();
        options.setErrorIfExists(true);
        System.out.println("Creating DB at: "+path.getCanonicalPath());
        try {
            DB.open(options, path);
            fail("Expected exception.");
        } catch (DB.DBException e) {
            e.printStackTrace();
        }
        options.delete();

    }

    @Test
    public void testCRUD() throws IOException {

        Options options = new Options();
        options.setCreateIfMissing(true);

        File path = getTestDirectory("db2");
        System.out.println("Creating DB at: "+path.getCanonicalPath());
        DB db = DB.open(options, path);

        WriteOptions wo = new WriteOptions();
        wo.setSync(false);

        ReadOptions ro = new ReadOptions();
        ro.setFillCache(true);
        ro.setVerifyChecksums(true);


        byte [] red = "red".getBytes("UTF-8");
        byte [] blue = "blue".getBytes("UTF-8");
        byte [] green = "green".getBytes("UTF-8");
        byte [] tampa = "Tampa".getBytes("UTF-8");
        byte [] london = "London".getBytes("UTF-8");
        byte [] newyork = "New York".getBytes("UTF-8");

        db.put(wo, tampa, green);
        db.put(wo, london, red);
        db.put(wo, newyork, blue);

        assertEquals(db.get(ro, tampa), green);
        assertEquals(db.get(ro, london), red);
        assertEquals(db.get(ro, newyork), blue);

        db.delete(wo, newyork);
        try {
            db.get(ro, newyork);
            fail("Expecting exception");
        } catch( DB.DBException e) {
            e.printStackTrace();
        }

        // leveldb does not consider deleting something that does not exist an error.
        db.delete(wo, newyork);

        ro.delete();
        wo.delete();
        options.delete();
        db.delete();
    }

    public void assertEquals(byte [] arg1, byte[] arg2) {
        assertTrue(Arrays.equals(arg1, arg2));
    }
}
