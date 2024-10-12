plugins {
    kotlin("jvm") version "2.0.20"
    id("org.graalvm.buildtools.native") version "0.10.3"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val asyncerVersion: String by project
val commonsCliVersion: String by project

dependencies {
    implementation("io.asyncer:r2dbc-mysql:$asyncerVersion")
    implementation("commons-cli:commons-cli:$commonsCliVersion")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
graalvmNative {
    binaries {
        named("main") {
            imageName.set("sql-csv")
            mainClass.set("com.github.igorhakk.sqlcsv.MainKt")
            buildArgs.add("-O4")
            buildArgs.add("-H:-ReduceImplicitExceptionStackTraceInformation")
        }
        named("test") {
            buildArgs.add("-O0")
        }
    }
    binaries.all {
        buildArgs.add("--verbose")
        buildArgs.add("--initialize-at-run-time=sun.net.dns.ResolverConfigurationImpl")
    }
}