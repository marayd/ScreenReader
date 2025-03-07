plugins {
    id("java")
}

group = "dev.mryd"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.kwhat:jnativehook:2.2.2")
    implementation("net.sourceforge.tess4j:tess4j:5.14.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}


tasks.test {
    useJUnitPlatform()
}
tasks.build {
    dependsOn(tasks.jar)
}
tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "dev.mryd.Main"
        )
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
}
