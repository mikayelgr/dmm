cmake_minimum_required(VERSION 3.20)
project(dmm LANGUAGES CXX C ASM)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

set(DMM_LIBRARY_NAME dmm)
set(DMM_SRC_PATH ${CMAKE_SOURCE_DIR}/src)
set(DMM_INCLUDE_DIR ${CMAKE_SOURCE_DIR}/include)
set(DMM_BUILD_JNI OFF CACHE BOOL "Build JNI bindings")

# Ensuring that the JAVA_HOME environment variable is set
if(NOT EXISTS $ENV{JAVA_HOME})
    message(FATAL_ERROR "JAVA_HOME is not set. Please export JAVA_HOME before building.")
endif()

set(JAVA_HOME $ENV{JAVA_HOME})
message(STATUS "Using JAVA_HOME = ${JAVA_HOME}")

# Explicitly set JNI include and lib paths
set(JNI_INCLUDE_DIRS ${JAVA_HOME}/include)
# Only adding the platform-specific JNI include directory for macOS
if(APPLE)
    # Because this was missing, there were errors about missing jni_md.h
    list(APPEND JNI_INCLUDE_DIRS ${JAVA_HOME}/include/darwin)
elseif(UNIX)
    list(APPEND JNI_INCLUDE_DIRS ${JAVA_HOME}/include/linux)
elseif(WIN32)
    message(FATAL_ERROR "Windows is not supported for this library.")
endif()

# Adding the Eigen library as a dependency
find_package(Eigen3 QUIET)
if(Eigen3_FOUND)
    message(STATUS "Using system Eigen: ${EIGEN3_INCLUDE_DIR}")
else()
    message(FATAL_ERROR "System Eigen not found.")
endif()

# Conditional compilation of the JNI bindings
if (DMM_BUILD_JNI)
    # JNI library: JNI bindings, depends on base
    add_library(dmm SHARED
        ${DMM_INCLUDE_DIR}/dmm.h
        ${DMM_SRC_PATH}/dmm.cpp
    )
    target_include_directories(dmm PUBLIC
        ${DMM_INCLUDE_DIR}
    ${EIGEN3_INCLUDE_DIR}
    ${JNI_INCLUDE_DIRS}
)
    target_link_libraries(dmm PRIVATE base)

    set_target_properties(dmm PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}
    RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}
)
endif()

message(STATUS "Output directory: ${CMAKE_BINARY_DIR}")
message(STATUS "JNI include dirs: ${JNI_INCLUDE_DIRS}")
