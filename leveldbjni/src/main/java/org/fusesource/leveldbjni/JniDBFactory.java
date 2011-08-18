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

import org.fusesource.leveldbjni.internal.*;
import org.iq80.leveldb.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class JniDBFactory implements DBFactory {

    public static final JniDBFactory factory = new JniDBFactory();

    public static byte[] bytes(String value) {
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

    static private class OptionsResourceHolder {

        NativeCache cache = null;
        NativeComparator comparator=null;
        NativeLogger logger=null;
        NativeOptions options;

        public void init(Options value) {

            options = new NativeOptions();
            options.blockRestartInterval(value.blockRestartInterval());
            options.blockSize(value.blockSize());
            options.createIfMissing(value.createIfMissing());
            options.errorIfExists(value.errorIfExists());
            options.maxOpenFiles(value.maxOpenFiles());
            options.paranoidChecks(value.paranoidChecks());
            options.writeBufferSize(value.writeBufferSize());

            switch(value.compressionType()) {
                case NONE:
                    options.compression(NativeCompressionType.kNoCompression);
                    break;
                case SNAPPY:
                    options.compression(NativeCompressionType.kSnappyCompression);
                    break;
            }


            if(value.cacheSize()>0 ) {
                cache = new NativeCache(value.cacheSize());
                options.cache(cache);
            }

            final DBComparator userComparator = value.comparator();
            if(userComparator!=null) {
                comparator = new NativeComparator() {
                    @Override
                    public int compare(byte[] key1, byte[] key2) {
                        return userComparator.compare(key1, key2);
                    }

                    @Override
                    public String name() {
                        return userComparator.name();
                    }
                };
                options.comparator(comparator);
            }

            final Logger userLogger = value.logger();
            if(userLogger!=null) {
                logger = new NativeLogger() {
                    @Override
                    public void log(String message) {
                        userLogger.log(message);
                    }
                };
                options.infoLog(logger);
            }

        }
        public void close() {
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
    }

    public DB open(File path, Options options) throws IOException {
        NativeDB db=null;
        OptionsResourceHolder holder = new OptionsResourceHolder();
        try {
            holder.init(options);
            db = NativeDB.open(holder.options, path);
        } finally {
            // if we could not open up the DB, then clean up the
            // other allocated native resouces..
            if(db==null) {
                holder.close();
            }
        }
        return new JniDB(db, holder.cache, holder.comparator, holder.logger);
    }

    public void destroy(File path, Options options) throws IOException {
        OptionsResourceHolder holder = new OptionsResourceHolder();
        try {
            holder.init(options);
            NativeDB.destroy(path, holder.options);
        } finally {
            holder.close();
        }
    }

    public void repair(File path, Options options) throws IOException {
        OptionsResourceHolder holder = new OptionsResourceHolder();
        try {
            holder.init(options);
            NativeDB.repair(path, holder.options);
        } finally {
            holder.close();
        }
    }

}
