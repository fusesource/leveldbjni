# LevelDB JNI

## Description

LevelDB JNI gives you a Java interface to the 
[LevelDB](http://code.google.com/p/leveldb/) C++ library
which is a fast key-value storage library written at Google 
that provides an ordered mapping from string keys to string values.. 

## Building Prerequisites 

* GNU compiler toolchain
* [Maven 3](http://maven.apache.org/download.html)

## Building

The following worked for me on:

 * OS X Lion with X Code 4
 * Ubuntu 10.04 (32 and 64 bit)

Then download the snappy, leveldb, and leveldbjni project source code:

    wget http://snappy.googlecode.com/files/snappy-1.0.3.tar.gz
    tar -zxvf snappy-1.0.3.tar.gz
    svn checkout -r 47 http://leveldb.googlecode.com/svn/trunk/ leveldb
    git clone git://github.com/fusesource/leveldbjni.git

Compile the snappy project.  This produces a static library.    

    cd snappy-1.0.3 
    ./configure --disable-shared --with-pic
    make
    
Patch and Compile the leveldb project.  This produces a static library.    
    
    cd ../leveldb
    export LIBRARY_PATH=`cd ../snappy-1.0.3; pwd`
    export C_INCLUDE_PATH=${LIBRARY_PATH}
    export CPLUS_INCLUDE_PATH=${LIBRARY_PATH}
    patch -p 0 < ../leveldbjni/leveldb.patch
    make

Now use maven to build the leveldbjni project.    
    
    cd ../leveldbjni
    mvn clean install -P download -P native -Dleveldb=`cd ../leveldb; pwd` -Dsnappy=`cd ../snappy-1.0.3; pwd`

The above will produce:

* `target/leveldbjni-${version}.jar` : The java class file to the library.
* `target/leveldbjni-${version}-native-src.zip` : A GNU style source project which you can use to build the native library on other systems.
* `target/leveldbjni-${version}-${platform}.jar` : A jar file containing the built native library using your currently platform.

## Using as a Maven Dependency

You just nee to add the following repositories and dependencies to your Maven pom.

    <repositories>
      <repository>
        <id>fusesource.nexus.snapshot</id>
        <name>FuseSource Community Snapshot Repository</name>
        <url>http://repo.fusesource.com/nexus/content/groups/public-snapshots</url>
      </repository>
    </repositories>
    
    <dependencies>
      <dependency>
        <groupId>org.fusesource.leveldbjni</groupId>
        <artifactId>leveldbjni</artifactId>
        <version>1.1-SNAPSHOT</version>
      </dependency>

      <!-- Add one or more of the platform specific dependencies -->
      <dependency>
        <groupId>org.fusesource.leveldbjni</groupId>
        <artifactId>leveldbjni</artifactId>
        <version>1.1-SNAPSHOT</version>
        <classifier>osx</classifier>
      </dependency>
      <dependency>
        <groupId>org.fusesource.leveldbjni</groupId>
        <artifactId>leveldbjni</artifactId>
        <version>1.1-SNAPSHOT</version>
        <classifier>linux32</classifier>
      </dependency>
      <dependency>
        <groupId>org.fusesource.leveldbjni</groupId>
        <artifactId>leveldbjni</artifactId>
        <version>1.1-SNAPSHOT</version>
        <classifier>linux64</classifier>
      </dependency>
    </dependencies>

Where ${platform}

## API Usage:

Recommended Package imports:

    import org.iq80.leveldb.*;
    import static org.fusesource.leveldbjni.JniDBFactory.*;
    import java.io.*;

Opening and closing the database.

    Options options = new Options();
    options.createIfMissing(true);
    DB db = factory.open(new File("example"), options);
    try {
      // Use the db in here....
    } finally {
      // Make sure you close the db to shutdown the 
      // database and avoid resource leaks.
      db.close();
    }

Putting, Getting, and Deleting key/values.

    db.put(bytes("Tampa"), bytes("rocks"));
    String value = asString(db.get(bytes("Tampa")));
    db.delete(wo, bytes("Tampa"));

Performing Batch/Bulk/Atomic Updates.

    WriteBatch batch = db.createWriteBatch();
    try {
      batch.delete(bytes("Denver"));
      batch.put(bytes("Tampa"), bytes("green"));
      batch.put(bytes("London"), bytes("red"));

      db.write(batch);
    } finally {
      // Make sure you close the batch to avoid resource leaks.
      batch.close();
    }

Iterating key/values.

    DBIterator iterator = db.iterator();
    try {
      for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
        String key = asString(iterator.peakNext().getKey());
        String value = asString(iterator.peakNext().getValue());
        System.out.println(key+" = "+value);
      }
    } finally {
      // Make sure you close the iterator to avoid resource leaks.
      iterator.close();
    }

Working against a Snapshot view of the Database.

    ReadOptions ro = new ReadOptions();
    ro.snapshot(db.getSnapshot());
    try {
      
      // All read operations will now use the same 
      // consistent view of the data.
      ... = db.iterator(ro);
      ... = db.get(bytes("Tampa"), ro);

    } finally {
      // Make sure you close the snapshot to avoid resource leaks.
      ro.snapshot().close()
    }

Using a custom Comparator.

    DBComparator comparator = new DBComparator(){
        public int compare(byte[] key1, byte[] key2) {
            return new String(key1).compareTo(new String(key2));
        }
        public String name() {
            return "simple";
        }
        public byte[] findShortestSeparator(byte[] start, byte[] limit) {
            return start;
        }
        public byte[] findShortSuccessor(byte[] key) {
            return key;
        }
    };
    Options options = new Options();
    options.comparator(comparator);
    DB db = factory.open(new File("example"), options);
    
Disabling Compression

    Options options = new Options();
    options.compressionType(CompressionType.NONE);
    DB db = factory.open(new File("example"), options);

Configuring the Cache
    
    Options options = new Options();
    options.cacheSize(100 * 1048576); // 100MB cache
    DB db = factory.open(new File("example"), options);

Getting approximate sizes.

    long[] sizes = db.getApproximateSizes(new Range(bytes("a"), bytes("k")), new Range(bytes("k"), bytes("z")));
    System.out.println("Size: "+sizes[0]+", "+sizes[1]);
    
Getting database status.

    String stats = db.getProperty("leveldb.stats");
    System.out.println(stats);

Getting informational log messages.

    Logger logger = new Logger() {
      public void log(String message) {
        System.out.println(message);
      }
    };
    Options options = new Options();
    options.logger(logger);
    DB db = factory.open(new File("example"), options);

Destroying a database.
    
    Options options = new Options();
    factory.destroy(new File("example"), options);

Repairing a database.
    
    Options options = new Options();
    factory.repair(new File("example"), options);
    