[![CI](https://github.com/mikayelgr/dmm/actions/workflows/test-build.yml/badge.svg)](https://github.com/mikayelgr/dmm/actions/workflows/test-build.yml)

# DMM - Dense Matrix Multiplication

DMM is a simple project exposes a few dense matrix multiplication functions from C++ to the JVM. The implementation is based on the [Eigen C++ library for Linear Algebra](https://eigen.tuxfamily.org/index.php?title=Main_Page) for implementing most of the multiplication functionality as efficiently as possible. I could have implemented it from scratch but existing solutions utilize the power of the modern CPUs much better using single instruction, multiple data (SIMD) and fused multiply-add (FMA) instructions. The project is supported on Linux and macOS. Support on Windows has not been tested.

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

When prompted by XCode, make sure to agree with the license agreements and follow the instructions on the screen. As a final measure, on macOS, you will additionally need to run the following command, to make sure that programs like `cmake` and `make` are globally available:

```bash
sudo launchctl config user path /opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin
```

On macOS, GUI apps like IntelliJ are launched by the `launchd` daemon, which doesn’t inherit your shell’s environment variables — including `PATH`. Running `sudo launchctl config user path ...` fixes this by defining a global PATH for all user processes, so GUI-launched apps can find tools like CMake or Homebrew binaries.

> - https://stackoverflow.com/questions/135688/setting-environment-variables-on-os-x?utm_source=chatgpt.com
> - https://unix.stackexchange.com/questions/89076/how-to-set-the-path-osx-applications-use?utm_source=chatgpt.com

## Structure

The project is structured in following directories:

- `libdmm` - Contains the original C++ to JNI bindings for 2 functions, which are **multiplication** and **equivalence checking** of two matrices, both based on the [Eigen C++ library for Linear Algebra](https://eigen.tuxfamily.org/index.php?title=Main_Page). I decided to stand on the shoulders of giants and focus on maximizing the performance and enhancing readability.
- `jvm` – Contains JNI implementation of the wrapper library for the JVM using Java, as well as tests, and benchmarks written in Java and Kotlin.
- `kmp` - Contains Kotlin Native/Multiplatform bindings implementation, as well as tests, and benchmarks written in Kotlin.

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

### Building `libdmm` Manually Using CMake

To get the dynamic library for `libdmm` manually, you can run the following commands from the root directory of this project:

```bash
cd libdmm
cmake -B build -S .
cd build && make && cd -
```

This will compile the dynamic library into a binary compatible only with your platform. You can find the generated binary under the `build` folder inside `libdmm`.

> Note that building on Windows has been tested once for now and failed due to missing dependencies. You can use your favorite Windows-native package manager in order to obtain the required libraries and binaries, in which case the library should compile in theory.

### Verifying JNI Implementation

To verify the JNI implementation, you can use the Gradle wrapper that comes with the project in the `jvm` folder. This specific folder contains a JNI implementation of the library using Java. To verify that everything functions properly, you can run the following command from the root of the project:

```bash
cd jvm
./gradlew test --console=plain
```

> Note: in case of making changes to the C++ source code, it is a good idea to run the `clean` task explicitly as well (e.g. `./gradlew clean test`) before testing anything in order to make sure that you're not running the cached library. I'm not very familiar with Gradle and tried to do my best based on my research, so I might have made some mistakes while configuring its caching.

### Verifying Kotlin Native Implementation

The project for Kotlin Native bindings has been boostrapped from the official repostiory at <https://github.com/Kotlin/kmp-native-wizard/>. To verify that the K/N bindings are installed and functioning properly, run the following commands from the root of the project:

```bash
cd kmp
./gradlew nativeTest
```

> Kotlin Native implementation, similar to the JNI implementation, builds the library automatically via Gradle tasks before C interop process happens, to make sure all libraries exist.

## Ending Notes

While I have tried to implement WASM/JS support as well as benchmarking with kotlinx-benchmark framework, I was constantly hitting some weird issues with my benchmarks and some of the files not being recognized as actual benchmark files. I believe that this might be due to the fact that I'm new to the Kotlin Native platform. While I have tried using AI tools as well, they got me nowhere.

For that reason I have included my pure Kotlin implementation of the simple matrix multiplication algorithm below, in case we manage to benchmark them sometime:

```kotlin
package com.mikayel.grigoryan

fun mul(left: Array<Array<Double>>, right: Array<Array<Double>>): Array<Array<Double>> {
    val lDims = getMatrixDimensions(left)
    val rDims = getMatrixDimensions(right)
    validateMatrices(left, lDims, right, rDims)

    val row1 = lDims[0]
    val col1 = lDims[1]
    val col2 = rDims[1]
    val product = Array(row1) { Array(col2) { 0.0 } }

    for (i in 0 until row1) {
        for (j in 0 until col2) {
            for (k in 0 until col1) {
                product[i][j] += left[i][k] * right[k][j]
            }
        }
    }

    return product
}

private fun validateMatrices(
    left: Array<Array<Double>>, lDims: Array<Int>,
    right: Array<Array<Double>>, rDims: Array<Int>,
) {
    assert(validateMatrixCols(lDims, left)) { "Left matrix is inconsistent" }
    assert(validateMatrixCols(rDims, right)) { "Right matrix is inconsistent" }
    assert(lDims[1] == rDims[0]) { "Number of columns of the left matrix != to number of rows on right matrix" }
}

private fun getMatrixDimensions(matrix: Array<Array<Double>>): Array<Int> {
    val nRows = matrix.size
    // Assuming the matrix is set to its first column's size
    val nCols = matrix.getOrNull(nRows)?.size ?: 0
    return arrayOf(nRows, nCols)
}

private fun validateMatrixCols(dims: Array<Int>, matrix: Array<Array<Double>>): Boolean {
    for (i in 0 until dims[0]) {
        if (matrix[i].size != dims[1]) {
            return false
        }
    }

    return true
}
```
