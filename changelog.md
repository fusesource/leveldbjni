# [LevelDBJNI](https://github.com/fusesource/leveldbjni)

## [leveldbjni 1.7][1_7], released 2013-05-13

* Fixes issues #27: SIGSEGV when creating iterator after DB was closed
* fixe the mac os error on osgi container
* Fix linking under mingw. Native functions not exported into dll correctly
* attach generated sources
* Add windows makefile (no autotools)

## [leveldbjni 1.6.1][1_6_1], released 2013-04-16
[1_6_1]: http://repo.fusesource.com/nexus/content/groups/public/org/fusesource/leveldbjni/leveldbjni-all/1.6.1

* Rebuilding as the uber jar for the 'leveldb-all' module did not actually include the changes in the 1.6 release.

## [leveldbjni 1.6][1_6], released 2013-02-06
[1_6]: http://repo.fusesource.com/nexus/content/groups/public/org/fusesource/leveldbjni/leveldbjni-all/1.6

* Added OSGi meta-data to the -all jar
* Optimize for when values are not found.
* Fixed Memory leak with pooled memory
* Fixed DBIterator.hasPrev() causes invalid memory access exception

## [leveldbjni 1.5][1_5], released 2012-10-31
[1_5]: http://repo.fusesource.com/nexus/content/groups/public/org/fusesource/leveldbjni/leveldbjni-all/1.5

* Switch to leveldb-api version 0.5
* Update to leveldb 1.9 code

## [leveldbjni 1.4][1_4], released 2012-10-31
[1_4]: http://repo.fusesource.com/nexus/content/groups/public/org/fusesource/leveldbjni/leveldbjni-all/1.4

* Switch to leveldb-api version 0.4
* Checking the results of autotool chain into the source tree so that folks building don't have to have the autotools installed.
* Support suspending the background compaction thread.

## [leveldbjni 1.3][1_3], released 2012-09-24
[1_3]: http://repo.fusesource.com/nexus/content/groups/public/org/fusesource/leveldbjni/leveldbjni-all/1.3

* Make Util.link work on windows too.
* Expose the CreateHardLinkW windows API call.
* Added Windows LevelDB Support
* Update to hawtjni 1.6.
* Support the db.compactRange method to force compaction of the leveldb files.
* Fixed bug need to get leveldbjni workin on the Zing JVM

## [leveldbjni 1.2][1_2], released 2012-02-27
[1_2]: http://repo.fusesource.com/nexus/content/groups/public/org/fusesource/leveldbjni/leveldbjni-all/1.2

* Document how to use the memory pools.
* Fixes issue #6 Support using a memory pool to reduce native memory allocation overhead.
* Update leveldb, hawtjni, and leveldb-api versions.
* Store the version in the factory class.
* Added a release guide.

## [leveldbjni 1.1][1_1], released 2011-09-29
[1_1]: http://repo.fusesource.com/nexus/content/groups/public/org/fusesource/leveldbjni/leveldbjni-all/1.1

* the all module needs at least one java file so that it produces a javadoc and src.zip
* Try to load the native lib when the JniDBFactory class is loaded.
* Fixes issue #1 : Bug on NativeBuffer offset
* Switch the license from CDDL to the New BSD license to match the license used in the leveldb project.
* Add the sonatype snapshot repo since that's where the leveldb-api is at currently.
* Pickup updates in the api module.
* Updating build instructions.
* implement repair and destroy.
* api updated
* Cleaner java package structure. We only need to expose one public class now since we are using the org.iq80.leveldb abstract api.
* Refactored so that the main user API is the abstract API defined in 'org.iq80.leveldb.api' package.

## [leveldbjni 1.0][1_0], released 2011-08-08
[1_0]: http://repo.fusesource.com/nexus/content/groups/public/org/fusesource/leveldbjni/leveldbjni/1.0

* Initial Release
* OS X Intel 32 and 64 bit support
* Linux Intel 32 and 64 bit support
