plugins {
    id("java")
    id("application")
    id("jacoco")
}

group = "fr.univamu.solver"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.jetbrains:annotations:26.0.2-1")
}

application {
    mainClass = "fr.univamu.solver.Main"
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "fr.univamu.solver.Main"
    }
}


tasks.test {
    useJUnitPlatform()
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
        csv.required = true
    }
}