package imdc.test

tasks {
    withType<Test> {
        useJUnitPlatform()
        jvmArgs = listOf("--add-opens", "java.base/java.io=ALL-UNNAMED")
    }
}
