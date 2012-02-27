package org.fusesource.leveldbjni;

import org.iq80.leveldb.DbBenchmark;

import java.util.concurrent.Callable;

/**
 */
public class JniDbBenchmark {
    public static void main(final String[] args) throws Exception {
        System.setProperty("leveldb.factory", JniDBFactory.class.getName());
        JniDBFactory.pushMemoryPool(1024 * 512);
        try {
            DbBenchmark.main(args);
        } finally {
            JniDBFactory.popMemoryPool();
        }
    }
}
