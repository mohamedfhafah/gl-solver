plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

repositories {
    mavenCentral()
}

javafx {
    version = "21.0.2"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass = "com.example.planning.MainApp"
}

dependencies {
    implementation(files("libs/gl-solver.jar")) // déposer le jar compilé du solver ici
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
