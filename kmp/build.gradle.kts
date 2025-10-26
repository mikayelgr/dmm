plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
}

group = "grigoryan"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val libdmmRoot = projectDir.resolve("../libdmm")

kotlin {
    val hostOs = System.getProperty("os.name")
    val isArm64 = System.getProperty("os.arch") == "aarch64"
    val nativeTarget = when {
        hostOs == "Mac OS X" && isArm64 -> macosArm64("native")
        hostOs == "Mac OS X" && !isArm64 -> macosX64("native")
        hostOs == "Linux" && isArm64 -> linuxArm64("native")
        hostOs == "Linux" && !isArm64 -> linuxX64("native")
        else -> throw GradleException("Unsupported host OS: $hostOs")
    }

    nativeTarget.apply {
        compilations["main"].cinterops {
            val dmmbase by creating {
                defFile("src/nativeInterop/cinterop/dmmbase.def")
                // This is necessary, because the includes aren't available anywhere globally
                // so we must pass them this way, because the temporary folder created by the
                // .def file handler doesn't have access to our includes directory.
                includeDirs.allHeaders(libdmmRoot.resolve("include"))
            }
        }

        binaries {
            sharedLib {
                baseName = "dmmbasekt"
                linkerOpts(
                    "-L${libdmmRoot.resolve("build")}",
                    "-ldmmbase"
                )
            }

            staticLib {
                baseName = "dmmbasekt"
                linkerOpts(
                    "-L${libdmmRoot.resolve("build")}",
                    "-ldmmbase"
                )
            }
        }
    }
}
