import org.gradle.internal.os.OperatingSystem

plugins {
    kotlin("jvm") version "2.2.20"
}

group = "com.mikayel.grigoryan"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

// ---------- Native Library Build Configuration ----------

val nativeRootDir = file("../libdmm")
val nativeBuildDir = nativeRootDir.resolve("build")

// Step 1. Configure CMake (generate Makefiles)
tasks.register<Exec>("configureCMake") {
    group = "build"
    description = "Configures CMake and generates Makefiles for the native library"

    doFirst {
        if (!nativeBuildDir.exists()) nativeBuildDir.mkdirs()
    }

    workingDir = nativeRootDir

    commandLine(
        "cmake",
        "-S", ".",
        "-B", "build",
        "-DCMAKE_BUILD_TYPE=Release",
        "-G", "Unix Makefiles"
    )
}

// Step 2. Build native code using make
tasks.register<Exec>("makeNative") {
    group = "build"
    description = "Builds the native C++ library using make"

    dependsOn("configureCMake")
    workingDir = nativeBuildDir
    commandLine("make", "-j${Runtime.getRuntime().availableProcessors()}")

    inputs.dir(nativeRootDir.resolve("src"))
    inputs.file(nativeRootDir.resolve("CMakeLists.txt"))
    outputs.dir(nativeBuildDir)
}

tasks.named("compileKotlin") {
    dependsOn("makeNative")
}

tasks.named("compileJava") {
    dependsOn("makeNative")
}

// Append our native build directory to java.library.path, because we don't want
// to install the library system-wide.
fun extendJavaLibraryPath(): String {
    val existing = System.getProperty("java.library.path") ?: ""
    val combined = listOf(existing, nativeBuildDir.absolutePath)
        .filter { it.isNotEmpty() }
        .joinToString(File.pathSeparator)
    println("Using java.library.path = $combined")
    return combined
}

tasks.withType<Test> {
    systemProperty("java.library.path", extendJavaLibraryPath())
}

tasks.withType<JavaExec> {
    systemProperty("java.library.path", extendJavaLibraryPath())
}

// ---------- Clean Native Build Directory ----------
// Ensure native CMake build folder is deleted when running `./gradlew clean`
tasks.named<Delete>("clean") {
    delete(nativeBuildDir)
    doFirst {
        println("Cleaning native build folder: ${nativeBuildDir.absolutePath}")
    }
}

