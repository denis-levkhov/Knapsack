plugins {
    id("java")
}

group = "pearacle"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly ("org.projectlombok:lombok:1.18.38")
    annotationProcessor ("org.projectlombok:lombok:1.18.38")
    testCompileOnly ("org.projectlombok:lombok:1.18.38")
    testAnnotationProcessor ("org.projectlombok:lombok:1.18.38")
    implementation("com.google.ortools:ortools-java:9.12.4544")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}