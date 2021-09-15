# How To Release

Since levedbjni has to be build against multiple platforms, the standard maven release plugin will not work to do the release.  First off, lets 
get the sources tagged properly so we can reproduce the build on all the platforms:

## Prepping and tagging the leveldbjni source code.
    
    cd /tmp
    git clone git://github.com/fusesource/leveldbjni.git
    cd leveldbjni
    git co -b ${version}.x
    mvn -P all org.codehaus.mojo:versions-maven-plugin:1.2:set org.codehaus.mojo:versions-maven-plugin:1.2:commit -DnewVersion="${version}" 
    git commit -am "Preping for a the ${version} release"
    git tag "leveldbjni-${version}"
    git push origin "leveldbjni-${version}"
    
## Prepping and tagging the leveldb source code.

    cd /tmp
    git clone git://github.com/chirino/leveldb.git
    cd leveldb
    git apply ../leveldbjni/leveldb.patch
    git commit -am "Preping for a the leveldbjni-${version} release"
    git push origin "leveldbjni-${version}"    

## Releasing the non-platform specific artifacts

Now we are read to start doing release builds.  The first step is to release the non-platform specific artifacts
of leveldbjni.  This build should be done on a machine with an autoconfig version of at least 2.61 so that it
can generate the configure script for the gnu make based builds.

    cd /tmp
    git clone git://github.com/fusesource/leveldbjni.git
    git checkout "leveldbjni-${version}"
    mvn clean deploy -P release -P download

## Releasing the platform specific artifacts

Once this is done, now you can build the platform specific shared libraries.  These platform builds depend on
being able to access the artifacts previously deployed so you may need to close out any staged deployments before
progressing to the next step.  The first thing we need to do is download and build the snappy and leveldb 
static libraries.

To download the snappy, leveldb, and leveldbjni project source code:
    
    cd /tmp

    wget http://snappy.googlecode.com/files/snappy-1.0.5.tar.gz
    tar -zxvf snappy-1.0.5.tar.gz

    git clone git://github.com/chirino/leveldb.git
    cd leveldb
    git checkout "leveldbjni-${version}" 
    cd ..

    git clone git://github.com/fusesource/leveldbjni.git
    cd leveldbjni
    git checkout "leveldbjni-${version}" 
    cd ..
    
    export SNAPPY_HOME=`cd snappy-1.0.5; pwd`
    export LEVELDB_HOME=`cd leveldb; pwd`
    export LEVELDBJNI_HOME=`cd leveldbjni; pwd`
    export LIBRARY_PATH=${SNAPPY_HOME}
    export C_INCLUDE_PATH=${LIBRARY_PATH}
    export CPLUS_INCLUDE_PATH=${LIBRARY_PATH}

Compile the snappy project and leveldb static libs

    cd ${SNAPPY_HOME}
    ./configure --disable-shared --with-pic; make
    
    cd ${LEVELDB_HOME}
    make libleveldb.a
    
Now cd to the platform specific leveldbjni module.  For example on 32 bit linux you would cd to ${LEVELDBJNI_HOME}/leveldbjni/leveldbjni-linux32 and run a release build:

    cd ${LEVELDBJNI_HOME}/leveldbjni-$platform
    mvn clean deploy -P release -P download
    
### Linux build tips:

* Build on REHL 5.6 for best compatibility across linux versions.    

### Windows build tips:

* Build on Windows 7 using the Windows 7.1 SDK http://www.microsoft.com/en-us/download/details.aspx?id=8279

## Aggregating platform specific artifacts

Once you have released all the platform specific artifacts, then release the `leveldbjni-all` which uber jars all the previously released artifacts.

    cd ${LEVELDBJNI_HOME}/leveldbjni-all
    mvn clean deploy -P release -P download

Congrats your done.