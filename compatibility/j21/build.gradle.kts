repositories {
    mavenCentral()
    maven("https://repo.nexomc.com/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.momirealms.net/releases/")
}

dependencies {
    compileOnly(project(":api"))
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("com.nexomc:nexo:0.5.0-dev.8")
    compileOnly("net.momirealms:craft-engine-core:0.0.13")
    compileOnly("net.momirealms:craft-engine-bukkit:0.0.13")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}