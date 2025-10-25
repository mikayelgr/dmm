# DMM - Dense Matrix Multiplication

DMM is a simple project exposes a few dense matrix multiplication functions from C++ to the JVM. The implementation is based on the [Eigen](https://eigen.tuxfamily.org/index.php?title=Main_Page) C++ library for implementing most of the multiplication functionality as efficiently as possible. I could have implemented it from scratch but existing solutions utilize the power of the modern CPUs much better using single instruction, multiple data (SIMD) and fused multiply-add (FMA) instructions.

> Note: this project has been created as a solution for the task for the JetBrains' "Graphite rendering backend support in Skiko and Compose Multiplatform" internship during 2025. This code has been authored by Mikayel Grigoryan. 

## Introduction

> This guide is only applicable to macOS, however, the project itself can be built on most systems, including macOS, Linux, and Windows. The behavior of this library has been optimized for macOS specifically.

To be able to build and test this project there are a few prerequisites. You will need to install the following packages using brew or your favorite package manager:

- CMake version >=4.1.2
- Eigen C++ version 3.4.1
- OpenJDK 21
- Git (from XCode toolchain)
- GNU Make (from XCode toolchain)
- Apple clang version 17.0.0 (from XCode toolchain)
- Apple clang++ version 17.0.0 (from XCode toolchain)

To install these packages on macOS, you must use `brew` and `xcode-select`. Just copy and paste the following command inside your terminal for the packages to be installed:

```bash
brew install cmake eigen openjdk@21
xcode-select --install
```

When prompted by XCode, make sure to agree with the license agreements and follow the instructions on the screen.

## Configuration

The `libdmm` directory contains the C++ source code of the implementation of the bridge interface. It exposes a few function from `src/dmm.cpp` using the Java Native Interface (JNI) so that the functions are accessible from any JVM language contexts. Before building, you must ensure that the `JAVA_HOME` environment variable is set. In our case, you can obtain the variable by entering:

```bash
brew info openjdk@21
```

This should return output similar to the following:
```bash
==> openjdk@21: stable 21.0.9 (bottled) [keg-only]
Development kit for the Java programming language
https://openjdk.org/
Installed
/opt/homebrew/Cellar/openjdk@21/21.0.9 (600 files, 331.2MB)
  Poured from bottle using the formulae.brew.sh API on 2025-10-25 at 17:38:53
From: https://github.com/Homebrew/homebrew-core/blob/HEAD/Formula/o/openjdk@21.rb
License: GPL-2.0-only WITH Classpath-exception-2.0
==> Dependencies
Build: autoconf ✘, pkgconf ✘
Required: freetype ✔, giflib ✔, harfbuzz ✔, jpeg-turbo ✔, libpng ✔, little-cms2 ✔
==> Requirements
Build: Xcode (on macOS) ✔
==> Caveats
For the system Java wrappers to find this JDK, symlink it with
  sudo ln -sfn /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-21.jdk

openjdk@21 is keg-only, which means it was not symlinked into /opt/homebrew,
because this is an alternate version of another formula.

If you need to have openjdk@21 first in your PATH, run:
  echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc

For compilers to find openjdk@21 you may need to set:
  export CPPFLAGS="-I/opt/homebrew/opt/openjdk@21/include"
==> Analytics
install: 21,972 (30 days), 60,964 (90 days), 211,180 (365 days)
install-on-request: 15,401 (30 days), 42,560 (90 days), 131,855 (365 days)
build-error: 75 (30 days)
```

From here, the main path for the OpenJDK 21 is going to be `/opt/homebrew/Cellar/openjdk@21/21.0.9`, and the `JAVA_HOME` environment variable will need to be set to `/opt/homebrew/Cellar/openjdk@21/21.0.9/libexec/openjdk.jdk/Contents/Home`. This setup is specific to macOS, specifically Apple silicon.

### Building

To obtain the dynamic library for your specific platform, run the build process using CMake and assemble the files using GNU Make:

```bash
cd libdmm
cmake -B ./build -S . && cd build && make && cd -
```

This will assemble the dynamic library which can be linked to our JVM/Kotlin Multiplatform library. If the build succeeds, you will be able to find a `libdmm.dylib|.so|.dll` depending on your platform in the `libdmm/build` folder.

### Verifying JNI Implementation

To verify the JNI implementation, you can use the Gradle wrapper that comes with the project in the `jvm` folder. This specific folder contains a JNI implementation of the library using Java. To verify that everything functions properly, you can run the following command from the root of the project:

```bash
cd jvm
./gradlew test
```

> Note: in case of making changes to the C++ source code, it is a good idea to run the `clean` task explicitly as well (e.g. `./gradlew clean test`) before testing anything in order to make sure that you're not running the cached library. I'm not very familiar with Gradle and tried to do my best based on my research, so I might have made some mistakes while configuring its caching.
