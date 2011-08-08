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

import org.fusesource.hawtjni.runtime.JniArg;
import org.fusesource.hawtjni.runtime.JniClass;
import org.fusesource.hawtjni.runtime.JniMethod;
import org.fusesource.hawtjni.runtime.Library;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static org.fusesource.hawtjni.runtime.ArgFlag.*;
import static org.fusesource.hawtjni.runtime.ClassFlag.CPP;
import static org.fusesource.hawtjni.runtime.MethodFlag.*;

/**
 * The DB object provides the main interface to acessing LevelDB
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class DB extends NativeObject {

    static final Library LIBRARY = new Library("leveldbjni", DB.class);

    @JniClass(name="leveldb::DB", flags={CPP})
    static class DBJNI {
        static {
            DB.LIBRARY.load();
        }

        @JniMethod(flags={JNI, POINTER_RETURN}, cast="jobject")
        public static final native long NewGlobalRef(
                Object target);

        @JniMethod(flags={JNI}, cast="jobject")
        public static final native void DeleteGlobalRef(
                @JniArg(cast="jobject", flags={POINTER_ARG})
                long target);

        @JniMethod(flags={JNI, POINTER_RETURN}, cast="jclass")
        public static final native long GetObjectClass(
                Object target);

        @JniMethod(flags={JNI, POINTER_RETURN}, cast="jmethodID")
        public static final native long GetMethodID(
                @JniArg(cast="jclass", flags={POINTER_ARG})
                long clazz,
                String name,
                String signature);

        @JniMethod(flags={CPP_DELETE})
        static final native void delete(
                long self
                );

        @JniMethod(copy="leveldb::Status", accessor = "leveldb::DB::Open")
        static final native long Open(
                @JniArg(flags={BY_VALUE, NO_OUT}) Options options,
                @JniArg(cast="const char*") String path,
                @JniArg(cast="leveldb::DB**") long[] self);

        @JniMethod(copy="leveldb::Status", flags={CPP_METHOD})
        static final native long Put(
                long self,
                @JniArg(flags={BY_VALUE, NO_OUT}) WriteOptions options,
                @JniArg(flags={BY_VALUE, NO_OUT}) Slice key,
                @JniArg(flags={BY_VALUE, NO_OUT}) Slice value
                );

        @JniMethod(copy="leveldb::Status", flags={CPP_METHOD})
        static final native long Delete(
                long self,
                @JniArg(flags={BY_VALUE, NO_OUT}) WriteOptions options,
                @JniArg(flags={BY_VALUE, NO_OUT}) Slice key
                );

        @JniMethod(copy="leveldb::Status", flags={CPP_METHOD})
        static final native long Write(
                long self,
                @JniArg(flags={BY_VALUE}) WriteOptions options,
                @JniArg(cast="leveldb::WriteBatch *") long updates
                );

        @JniMethod(copy="leveldb::Status", flags={CPP_METHOD})
        static final native long Get(
                long self,
                @JniArg(flags={NO_OUT, BY_VALUE}) ReadOptions options,
                @JniArg(flags={BY_VALUE, NO_OUT}) Slice key,
                @JniArg(cast="std::string *") long value
                );

        @JniMethod(cast="leveldb::Iterator *", flags={CPP_METHOD})
        static final native long NewIterator(
                long self,
                @JniArg(flags={NO_OUT, BY_VALUE}) ReadOptions options
                );

        @JniMethod(cast="leveldb::Snapshot *", flags={CPP_METHOD})
        static final native long GetSnapshot(
                long self);

        @JniMethod(flags={CPP_METHOD})
        static final native void ReleaseSnapshot(
                long self,
                @JniArg(cast="const leveldb::Snapshot *") long snapshot
                );

        @JniMethod(flags={CPP_METHOD})
        static final native void GetApproximateSizes(
                long self,
                @JniArg(cast="const leveldb::Range *") long range,
                int n,
                @JniArg(cast="uint64_t*") long[] sizes
                );

        @JniMethod(flags={CPP_METHOD})
        static final native boolean GetProperty(
                long self,
                @JniArg(flags={BY_VALUE, NO_OUT}) Slice property,
                @JniArg(cast="std::string *") long value
                );

        @JniMethod(copy="leveldb::Status", accessor = "leveldb::DestroyDB")
        static final native long DestroyDB(
                @JniArg(cast="const char*") String path,
                @JniArg(flags={BY_VALUE, NO_OUT}) Options options);

        @JniMethod(copy="leveldb::Status", accessor = "leveldb::RepairDB")
        static final native long RepairDB(
                @JniArg(cast="const char*") String path,
                @JniArg(flags={BY_VALUE, NO_OUT}) Options options);
    }

    public void delete() {
        assertAllocated();
        DBJNI.delete(self);
        self = 0;
    }

    private DB(long self) {
        super(self);
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

    static void checkStatus(long s) throws DBException {
        Status status = new Status(s);
        try {
            if( !status.isOk() ) {
                throw new DBException(status.toString(), status.isNotFound());
            }
        } finally {
            status.delete();
        }
    }

    static void checkArgNotNull(Object value, String name) {
        if(value==null) {
            throw new IllegalArgumentException("The "+name+" argument cannot be null");
        }
    }

    public static DB open(Options options, File path) throws IOException, DBException {
        checkArgNotNull(options, "options");
        checkArgNotNull(path, "path");
        long rc[] = new long[1];
        try {
            checkStatus(DBJNI.Open(options, path.getCanonicalPath(), rc));
        } catch (IOException e) {
            if( rc[0]!=0 ) {
                DBJNI.delete(rc[0]);
            }
            throw e;
        }
        return new DB(rc[0]);
    }

    public void put(WriteOptions options, byte[] key, byte[] value) throws DBException {
        checkArgNotNull(options, "options");
        checkArgNotNull(key, "key");
        checkArgNotNull(value, "value");
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
        put(options, new Slice(keyBuffer), new Slice(valueBuffer));
    }

    private void put(WriteOptions options, Slice keySlice, Slice valueSlice) throws DBException {
        assertAllocated();
        checkStatus(DBJNI.Put(self, options, keySlice, valueSlice));
    }

    public void delete(WriteOptions options, byte[] key) throws DBException {
        checkArgNotNull(options, "options");
        checkArgNotNull(key, "key");
        NativeBuffer keyBuffer = new NativeBuffer(key);
        try {
            delete(options, keyBuffer);
        } finally {
            keyBuffer.delete();
        }
    }

    private void delete(WriteOptions options, NativeBuffer keyBuffer) throws DBException {
        delete(options, new Slice(keyBuffer));
    }

    private void delete(WriteOptions options, Slice keySlice) throws DBException {
        assertAllocated();
        checkStatus(DBJNI.Delete(self, options, keySlice));
    }

    public void write(WriteOptions options, WriteBatch updates) throws DBException {
        checkArgNotNull(options, "options");
        checkArgNotNull(updates, "updates");
        checkStatus(DBJNI.Write(self, options, updates.pointer()));
    }

    public byte[] get(ReadOptions options, byte[] key) throws DBException {
        checkArgNotNull(options, "options");
        checkArgNotNull(key, "key");
        NativeBuffer keyBuffer = new NativeBuffer(key);
        try {
            return get(options, keyBuffer);
        } finally {
            keyBuffer.delete();
        }
    }

    private byte[] get(ReadOptions options, NativeBuffer keyBuffer) throws DBException {
        return get(options, new Slice(keyBuffer));
    }

    private byte[] get(ReadOptions options, Slice keySlice) throws DBException {
        assertAllocated();
        StdString result = new StdString();
        try {
            checkStatus(DBJNI.Get(self, options, keySlice, result.pointer()));
            return result.toByteArray();
        } finally {
            result.delete();
        }
    }

    public Snapshot getSnapshot() {
        return new Snapshot(DBJNI.GetSnapshot(self));
    }

    public void releaseSnapshot(Snapshot snapshot) {
        checkArgNotNull(snapshot, "snapshot");
        DBJNI.ReleaseSnapshot(self, snapshot.pointer());
    }

    public Iterator iterator(ReadOptions options) {
        checkArgNotNull(options, "options");
        return new Iterator(DBJNI.NewIterator(self, options));
    }

    public static byte[] bytes(String value) {
        checkArgNotNull(value, "value");
        if( value == null) {
            return null;
        }
        try {
            return value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String asString(byte value[]) {
        if( value == null) {
            return null;
        }
        try {
            return new String(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public long[] getApproximateSizes(Range ... ranges) {
        if( ranges==null ) {
            return null;
        }

        long rc[] = new long[ranges.length];
        Range.RangeJNI structs[] = new Range.RangeJNI[ranges.length];
        if( rc.length> 0 ) {
            NativeBuffer range_array = Range.RangeJNI.arrayCreate(ranges.length);
            try {
                for(int i=0; i < ranges.length; i++) {
                    structs[i] = new Range.RangeJNI(ranges[i]);
                    structs[i].arrayWrite(range_array.pointer(), i);
                }
                DBJNI.GetApproximateSizes(self,range_array.pointer(), ranges.length, rc);
            } finally {
                for(int i=0; i < ranges.length; i++) {
                    if( structs[i] != null ) {
                        structs[i].delete();
                    }
                }
                range_array.delete();
            }
        }
        return rc;
    }

    public String getProperty(String name) throws DBException {
        checkArgNotNull(name, "name");
        NativeBuffer keyBuffer = new NativeBuffer(name.getBytes());
        try {
            byte[] property = getProperty(keyBuffer);
            if( property==null ) {
                return null;
            } else {
                return new String(property);
            }
        } finally {
            keyBuffer.delete();
        }
    }

    private byte[] getProperty(NativeBuffer nameBuffer) throws DBException {
        return getProperty(new Slice(nameBuffer));
    }

    private byte[] getProperty(Slice nameSlice) throws DBException {
        assertAllocated();
        StdString result = new StdString();
        try {
            if( DBJNI.GetProperty(self, nameSlice, result.pointer()) ) {
                return result.toByteArray();
            } else {
                return null;
            }
        } finally {
            result.delete();
        }
    }

    static public void destroy(File path, Options options) throws IOException, DBException {
        checkArgNotNull(options, "options");
        checkArgNotNull(path, "path");
        checkStatus(DBJNI.DestroyDB(path.getCanonicalPath(), options));
    }

    static public void repair(File path, Options options) throws IOException, DBException {
        checkArgNotNull(options, "options");
        checkArgNotNull(path, "path");
        checkStatus(DBJNI.RepairDB(path.getCanonicalPath(), options));
    }
}
