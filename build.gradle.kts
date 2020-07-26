plugins {
    id("org.jetbrains.kotlin.multiplatform") version "1.3.72"
    id("maven-publish")
}

repositories {
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://dl.bintray.com/korlibs/korlibs") }
    maven { url = uri("https://plugins.gradle.org/m2/") }
}

group = "io.kharf.physicseditor"
version = "1.0.0"

dependencies {
    add("commonMainApi", "com.soywiz.korlibs.kbox2d:kbox2d:0.8.0")
    add("commonMainApi", "com.soywiz.korlibs.korio:korio:1.11.13")

}

kotlin {
    jvm()
    js()
    mingwX64()
    linuxX64()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}
