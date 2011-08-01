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

/**
 * Provides a java interface to the C++ leveldb::Options class.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class Options extends NativeObject {

    @JniClass(name="leveldb::Options", flags={ClassFlag.CPP})
    private static class OptionsJNI {
        static {
            DB.LIBRARY.load();
        }

        @JniMethod(flags={MethodFlag.CPP_NEW}, cast="leveldb::Options *")
        public static final native long create();
        @JniMethod(flags={MethodFlag.CPP_DELETE})
        public static final native void delete(@JniArg(cast="leveldb::Options *") long self);

        @JniMethod(flags = {MethodFlag.CPP, MethodFlag.GETTER})
        public static final native boolean create_if_missing(@JniArg(cast="leveldb::Options *") long self);
        @JniMethod(flags = {MethodFlag.CPP, MethodFlag.SETTER})
        public static final native void create_if_missing(@JniArg(cast="leveldb::Options *") long self, boolean value);

        @JniMethod(flags = {MethodFlag.CPP, MethodFlag.GETTER})
        public static final native boolean error_if_exists(@JniArg(cast="leveldb::Options *") long self);
        @JniMethod(flags = {MethodFlag.CPP, MethodFlag.SETTER})
        public static final native void error_if_exists(@JniArg(cast="leveldb::Options *") long self, boolean value);

        @JniMethod(flags = {MethodFlag.CPP, MethodFlag.GETTER})
        public static final native boolean paranoid_checks(@JniArg(cast="leveldb::Options *") long self);
        @JniMethod(flags = {MethodFlag.CPP, MethodFlag.SETTER})
        public static final native void paranoid_checks(@JniArg(cast="leveldb::Options *") long self, boolean value);

        @JniMethod(flags = {MethodFlag.CPP, MethodFlag.GETTER}, cast="size_t")
        public static final native long write_buffer_size(@JniArg(cast="leveldb::Options *") long self);
        @JniMethod(flags = {MethodFlag.CPP, MethodFlag.SETTER})
        public static final native void write_buffer_size(@JniArg(cast="leveldb::Options *") long self, @JniArg(cast="size_t") long value);

        @JniMethod(flags = {MethodFlag.CPP, MethodFlag.GETTER}, cast="size_t")
        public static final native long block_size(@JniArg(cast="leveldb::Options *") long self);
        @JniMethod(flags = {MethodFlag.CPP, MethodFlag.SETTER})
        public static final native void block_size(@JniArg(cast="leveldb::Options *") long self, @JniArg(cast="size_t") long value);

        @JniMethod(flags = {MethodFlag.CPP, MethodFlag.GETTER})
        public static final native int max_open_files(@JniArg(cast="leveldb::Options *") long self);
        @JniMethod(flags = {MethodFlag.CPP, MethodFlag.SETTER})
        public static final native void max_open_files(@JniArg(cast="leveldb::Options *") long self, int value);

        @JniMethod(flags = {MethodFlag.CPP, MethodFlag.GETTER})
        public static final native int block_restart_interval(@JniArg(cast="leveldb::Options *") long self);
        @JniMethod(flags = {MethodFlag.CPP, MethodFlag.SETTER})
        public static final native void block_restart_interval(@JniArg(cast="leveldb::Options *") long self, int value);

        // TODO:
        // const Comparator* comparator;
        // Env* env;
        // Logger* info_log;
        // Cache* block_cache;
        // CompressionType compression;

    }

    public Options() {
        super(OptionsJNI.create());
    }

    public void delete() {
        assertAllocated();
        OptionsJNI.delete(self);
        self = 0;
    }

    public void setCreateIfMissing(boolean value) {
        assertAllocated();
        OptionsJNI.create_if_missing(self, value);
    }
    public boolean getCreateIfMissing() {
        assertAllocated();
        return OptionsJNI.create_if_missing(self);
    }

    public void setErrorIfExists(boolean value) {
        assertAllocated();
        OptionsJNI.error_if_exists(self, value);
    }
    public boolean getErrorIfExists() {
        assertAllocated();
        return OptionsJNI.error_if_exists(self);
    }

    public void setParanoidChecks(boolean value) {
        assertAllocated();
        OptionsJNI.paranoid_checks(self, value);
    }
    public boolean getParanoidChecks() {
        assertAllocated();
        return OptionsJNI.paranoid_checks(self);
    }

    public void setWriteBufferSize(long value) {
        assertAllocated();
        OptionsJNI.write_buffer_size(self, value);
    }
    public long getWriteBufferSize() {
        assertAllocated();
        return OptionsJNI.write_buffer_size(self);
    }

    public void setMaxOpenFiles(int value) {
        assertAllocated();
        OptionsJNI.max_open_files(self, value);
    }
    public int getMaxOpenFiles() {
        assertAllocated();
        return OptionsJNI.max_open_files(self);
    }

    public void setBlockRestartInterval(int value) {
        assertAllocated();
        OptionsJNI.block_restart_interval(self, value);
    }
    public int getBlockRestartInterval() {
        assertAllocated();
        return OptionsJNI.block_restart_interval(self);
    }

}
