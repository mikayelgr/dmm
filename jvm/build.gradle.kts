import java.io.File

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

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

// Root of the C++ JNI project
val nativeRootDir = file(rootProject.projectDir.resolve("../libdmm"))
val nativeBuildDir = nativeRootDir.resolve("build")

// Extend java.library.path for JNI loading
fun extendJavaLibraryPath(): String {
    val existing = System.getProperty("java.library.path") ?: ""
    val combined = listOf(existing, nativeBuildDir.absolutePath)
        .filter { it.isNotEmpty() }
        .joinToString(File.pathSeparator)
    println("Using java.library.path = $combined")
    return combined
}

val configureNative = tasks.register<Exec>("configureNative") {
    group = "build"
    description = "Configure the native CMake build system"

    workingDir = nativeRootDir
    commandLine(
        "cmake",
        "-S", ".",
        "-B", "build",
        "-DCMAKE_BUILD_TYPE=Release"
    )
    doFirst {
        println("Configuring native build system in $nativeBuildDir")
    }
}

val buildNative = tasks.register<Exec>("buildNative") {
    group = "build"
    description = "Build the native JNI library using CMake"

    dependsOn(configureNative)
    workingDir = nativeRootDir
    commandLine("cmake", "--build", "build", "--config", "Release")

    doFirst {
        println("Building native library...")
    }
    doLast {
        val builtLibFilename = nativeBuildDir.list().first {
            it.endsWith(".so") or it.endsWith(".dylib") or it.endsWith(".dll")
        }
        println("Built native library: ${file(nativeBuildDir.resolve(builtLibFilename).absolutePath)}")
    }
}

tasks.named("compileKotlin") {
    dependsOn(buildNative)
}
tasks.named("compileJava") {
    dependsOn(buildNative)
}
tasks.withType<Test> {
    dependsOn(buildNative)
    systemProperty("java.library.path", extendJavaLibraryPath())
}
tasks.withType<JavaExec> {
    dependsOn(buildNative)
    systemProperty("java.library.path", extendJavaLibraryPath())
}

tasks.named<Delete>("clean") {
    doFirst {
        println("Cleaning native build folder: ${nativeBuildDir.absolutePath}")
    }
    delete(nativeBuildDir)
}
