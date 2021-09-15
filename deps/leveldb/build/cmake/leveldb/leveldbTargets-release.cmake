#----------------------------------------------------------------
# Generated CMake target import file for configuration "Release".
#----------------------------------------------------------------

# Commands may need to know the format version.
set(CMAKE_IMPORT_FILE_VERSION 1)

# Import target "leveldb::leveldb" for configuration "Release"
set_property(TARGET leveldb::leveldb APPEND PROPERTY IMPORTED_CONFIGURATIONS RELEASE)
set_target_properties(leveldb::leveldb PROPERTIES
  IMPORTED_LOCATION_RELEASE "${_IMPORT_PREFIX}/lib/libleveldb.1.23.0.dylib"
  IMPORTED_SONAME_RELEASE "@rpath/libleveldb.1.dylib"
  )

list(APPEND _IMPORT_CHECK_TARGETS leveldb::leveldb )
list(APPEND _IMPORT_CHECK_FILES_FOR_leveldb::leveldb "${_IMPORT_PREFIX}/lib/libleveldb.1.23.0.dylib" )

# Commands beyond this point should not need to know the version.
set(CMAKE_IMPORT_FILE_VERSION)
