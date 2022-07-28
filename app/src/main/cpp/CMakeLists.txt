# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

cmake_minimum_required(VERSION 3.4.1)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
find_library(log-lib  log)

add_library(giftranscode  SHARED  GifTranscoder.cpp)
target_compile_options(giftranscode PUBLIC -Wall -Wno-unused-parameter -Wno-switch)
include_directories(gif/)

add_library(gif STATIC
        gif/dgif_lib.c  gif/egif_lib.c  gif/gifalloc.c  gif/gif_err.c  gif/gif_hash.c
        gif/openbsd-reallocarray.c  gif/quantize.c)
target_compile_options(gif PRIVATE -Wno-format -Wno-sign-compare -Wno-unused-parameter)
target_compile_definitions(gif PRIVATE -DHAVE_CONFIG_H)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries(giftranscode
        gif
        ${log-lib})