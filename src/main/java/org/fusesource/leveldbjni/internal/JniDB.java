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

import org.iq80.leveldb.*;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class JniDB implements DB {

    private final NativeDB db;
    private final NativeCache cache;
    private final NativeComparator comparator;
    private final NativeLogger logger;

    public JniDB(NativeDB db, NativeCache cache, NativeComparator comparator, NativeLogger logger) {
        this.db = db;
        this.cache = cache;
        this.comparator = comparator;
        this.logger = logger;
    }

    public void close() {
        db.delete();
        if(cache!=null) {
            cache.delete();
        }
        if(comparator!=null){
            comparator.delete();
        }
        if(logger!=null) {
            logger.delete();
        }
    }


    public byte[] get(byte[] key) throws DBException {
        return get(key, new ReadOptions());
    }

    public byte[] get(byte[] key, ReadOptions options) throws DBException {
        try {
            return db.get(convert(options), key);
        } catch (NativeDB.DBException e) {
            if(e.isNotFound()) {
                return null;
            }
            throw new DBException(e.getMessage(), e);
        }
    }

    public DBIterator iterator() {
        return iterator(new ReadOptions());
    }

    public DBIterator iterator(ReadOptions options) {
        return new JniDBIterator(db.iterator(convert(options)));
    }

    public void put(byte[] key, byte[] value) throws DBException {
        put(key, value, new WriteOptions());
    }

    public void delete(byte[] key) throws DBException {
        delete(key, new WriteOptions());
    }

    public void write(WriteBatch updates) throws DBException {
        write(updates, new WriteOptions());
    }

    public WriteBatch createWriteBatch() {
        return new JniWriteBatch(new NativeWriteBatch());
    }

    public Snapshot put(byte[] key, byte[] value, WriteOptions options) throws DBException {
        try {
            db.put(convert(options), key, value);
            return null;
        } catch (NativeDB.DBException e) {
            throw new DBException(e.getMessage(), e);
        }
    }

    public Snapshot delete(byte[] key, WriteOptions options) throws DBException {
        try {
            db.delete(convert(options), key);
            return null;
        } catch (NativeDB.DBException e) {
            throw new DBException(e.getMessage(), e);
        }
    }

    public Snapshot write(WriteBatch updates, WriteOptions options) throws DBException {
        try {
            db.write(convert(options), ((JniWriteBatch) updates).writeBatch());
            return null;
        } catch (NativeDB.DBException e) {
            throw new DBException(e.getMessage(), e);
        }
    }

    public Snapshot getSnapshot() {
        return new JniSnapshot(db, db.getSnapshot());
    }

    public long[] getApproximateSizes(Range... ranges) {
        NativeRange args[] = new NativeRange[ranges.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = new NativeRange(ranges[i].start(), ranges[i].limit());
        }
        return db.getApproximateSizes(args);
    }

    public String getProperty(String name) {
        return db.getProperty(name);
    }

    private NativeReadOptions convert(ReadOptions options) {
        if(options==null) {
            return null;
        }
        NativeReadOptions rc = new NativeReadOptions();
        rc.fillCache(options.isFillCache());
        rc.verifyChecksums(options.isVerifyChecksums());
        if(options.getSnapshot()!=null) {
            rc.snapshot(((JniSnapshot) options.getSnapshot()).snapshot());
        }
        return rc;
    }

    private NativeWriteOptions convert(WriteOptions options) {
        if(options==null) {
            return null;
        }
        NativeWriteOptions rc = new NativeWriteOptions();
        rc.sync(options.isSync());
        if(options.isSnapshot()) {
            throw new UnsupportedOperationException("WriteOptions snapshot not supported");
        }
        return rc;
    }
}
