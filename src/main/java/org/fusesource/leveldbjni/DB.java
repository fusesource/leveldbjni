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

import org.fusesource.hawtjni.runtime.*;

import java.io.File;
import java.io.IOException;

import static org.fusesource.hawtjni.runtime.ArgFlag.*;

/**
 * The DB object provides the main interface to acessing LevelDB
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class DB extends NativeObject {

    static final Library LIBRARY = new Library("leveldbjni", DB.class);

    @JniClass(name="leveldb::DB", flags={ClassFlag.CPP})
    private static class DBJNI {
        static { DB.LIBRARY.load(); }

        @JniMethod(flags={MethodFlag.CPP_DELETE})
        static final native void delete(
                @JniArg(cast="leveldb::DB *") long ptr);

        @JniMethod(copy="leveldb::Status", accessor = "leveldb::DB::Open")
        static final native long Open(
                @JniArg(cast="leveldb::Options *", flags={ArgFlag.BY_VALUE}) long options,
                @JniArg(cast="const char*") String path,
                @JniArg(cast="leveldb::DB**") long[] ptr);

        @JniMethod(copy="leveldb::Status", flags={MethodFlag.CPP})
        static final native long Put(
                @JniArg(cast="leveldb::DB *") long ptr,
                @JniArg(cast="const leveldb::WriteOptions *", flags={ArgFlag.BY_VALUE}) long options,
                @JniArg(cast="const leveldb::Slice *", flags={ArgFlag.BY_VALUE}) long key,
                @JniArg(cast="const leveldb::Slice *", flags={ArgFlag.BY_VALUE}) long value
                );

        @JniMethod(copy="leveldb::Status", flags={MethodFlag.CPP})
        static final native long Delete(
                @JniArg(cast="leveldb::DB *") long ptr,
                @JniArg(cast="const leveldb::WriteOptions *", flags={ArgFlag.BY_VALUE}) long options,
                @JniArg(cast="const leveldb::Slice *", flags={ArgFlag.BY_VALUE}) long key
                );

        @JniMethod(copy="leveldb::Status", flags={MethodFlag.CPP})
        static final native long Write(
                @JniArg(cast="leveldb::DB *") long ptr,
                @JniArg(cast="const leveldb::WriteOptions *", flags={ArgFlag.BY_VALUE}) long options,
                @JniArg(cast="leveldb::WriteBatch *") long updates
                );

        @JniMethod(copy="leveldb::Status", flags={MethodFlag.CPP})
        static final native long Get(
                @JniArg(cast="leveldb::DB *") long ptr,
                @JniArg(cast="const leveldb::ReadOptions *", flags={ArgFlag.BY_VALUE}) long options,
                @JniArg(cast="const leveldb::Slice *", flags={ArgFlag.BY_VALUE}) long key,
                @JniArg(cast="std::string *") long value
                );
    }

        @Override
    public void delete() {
        assertAllocated();
        DBJNI.delete(ptr);
        ptr = 0;
    }

    private DB(long ptr) {
        super(ptr);
    }

    public static class DBException extends IOException {
        private final boolean notFound;

        DBException(String s, boolean notFound) {
            super(s);
            this.notFound = notFound;
        }

        public boolean isNotFound() {
            return notFound;
        }
    }

    static private void checkStatus(long s) throws DBException {
        Status status = new Status(s);
        try {
            if( !status.isOk() ) {
                throw new DBException(status.toString(), status.isNotFound());
            }
        } finally {
            status.delete();
        }
    }

    public static DB open(Options options, File path) throws IOException, DBException {
        long rc[] = new long[1];
        checkStatus(DBJNI.Open(options.pointer(), path.getCanonicalPath(), rc));
        return new DB(rc[0]);
    }

    public void put(WriteOptions options, byte[] key, byte[] value) throws DBException {
        NativeBuffer keyBuffer = new NativeBuffer(key);
        try {
            NativeBuffer valueBuffer = new NativeBuffer(value);
            try {
                put(options, keyBuffer, valueBuffer);
            } finally {
                valueBuffer.delete();
            }
        } finally {
            keyBuffer.delete();
        }
    }

    private void put(WriteOptions options, NativeBuffer keyBuffer, NativeBuffer valueBuffer) throws DBException {
        Slice keySlice = new Slice(keyBuffer);
        try {
            Slice valueSlice = new Slice(valueBuffer);
            try {
                put(options, keySlice, valueSlice);
            } finally {
                valueSlice.delete();
            }
        } finally {
            keySlice.delete();
        }
    }

    private void put(WriteOptions options, Slice keySlice, Slice valueSlice) throws DBException {
        assertAllocated();
        checkStatus(DBJNI.Put(ptr, options.pointer(), keySlice.pointer(), valueSlice.pointer()));
    }

    public void delete(WriteOptions options, byte[] key) throws IOException {
        NativeBuffer keyBuffer = new NativeBuffer(key);
        try {
            delete(options, keyBuffer);
        } finally {
            keyBuffer.delete();
        }
    }

    private void delete(WriteOptions options, NativeBuffer keyBuffer) throws DBException {
        Slice keySlice = new Slice(keyBuffer);
        try {
            delete(options, keySlice);
        } finally {
            keySlice.delete();
        }
    }

    private void delete(WriteOptions options, Slice keySlice) throws DBException {
        assertAllocated();
        checkStatus(DBJNI.Delete(ptr, options.pointer(), keySlice.pointer()));
    }

    public byte[] get(ReadOptions options, byte[] key) throws DBException {
        NativeBuffer keyBuffer = new NativeBuffer(key);
        try {
            return get(options, keyBuffer);
        } finally {
            keyBuffer.delete();
        }
    }

    private byte[] get(ReadOptions options, NativeBuffer keyBuffer) throws DBException {
        Slice keySlice = new Slice(keyBuffer);
        try {
            return get(options, keySlice);
        } finally {
            keySlice.delete();
        }
    }

    private byte[] get(ReadOptions options, Slice keySlice) throws DBException {
        assertAllocated();
        StdString result = new StdString();
        try {
            checkStatus(DBJNI.Get(ptr, options.pointer(), keySlice.pointer(), result.pointer()));
            return result.toByteArray();
        } finally {
            result.delete();
        }
    }
}
