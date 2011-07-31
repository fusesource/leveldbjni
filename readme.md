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

The following worked for me on a OS X Lion system with X Code 4 Installed.

First checkout the leveldb and leveldbjni project source code:

    svn checkout http://leveldb.googlecode.com/svn/trunk/ leveldb
    git clone git://github.com/fusesource/leveldbjni.git

Compile the leveldb project.  This produces a static library.    
    
    cd leveldb
    make

Now use maven to build the leveldbjni project.    
    
    cd ../leveldbjni
    mvn install -Dleveldb=`cd ../leveldb; pwd`

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
        <version>1.0-SNAPSHOT</version>
      </dependency>
      <dependency>
        <groupId>org.fusesource.leveldbjni</groupId>
        <artifactId>leveldbjni</artifactId>
        <version>1.0-SNAPSHOT</version>
        <classifier>osx</classifier>
      </dependency>
    </dependencies>

## API Usage:

    import org.fusesource.leveldbjni.*;
    import java.io.*;
    ....
    Options options = new Options();
    options.setCreateIfMissing(true);

    WriteOptions wo = new WriteOptions();
    wo.setSync(false);

    ReadOptions ro = new ReadOptions();
    ro.setFillCache(true);
    ro.setVerifyChecksums(true);

    DB db = DB.open(options, new File("example"));

    byte [] red = "red".getBytes("UTF-8");
    byte [] blue = "blue".getBytes("UTF-8");
    byte [] green = "green".getBytes("UTF-8");
    byte [] tampa = "Tampa".getBytes("UTF-8");
    byte [] london = "London".getBytes("UTF-8");
    byte [] newyork = "New York".getBytes("UTF-8");

    db.put(wo, tampa, green);
    db.put(wo, london, red);
    db.put(wo, newyork, blue);

    byte [] result = db.get(ro, london);
    
    db.delete(wo, newyork);
    try {
        db.get(ro, newyork);
    } catch( DB.DBException expeted) {
    }
    
    db.delete();
    ro.delete();
    wo.delete();
    options.delete();
