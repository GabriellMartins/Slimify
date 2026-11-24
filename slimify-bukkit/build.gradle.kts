plugins {
    `java-library`
    alias(libs.plugins.shadow)
}

dependencies {
    compileOnly(libs.paper.api)
    implementation(project(":slimify-api"))
    implementation(project(":slimify-importer"))

    testImplementation(libs.paper.api)
}

tasks {
    processResources {
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filesMatching("plugin.yml") { expand(props) }
    }

    shadowJar {
        archiveBaseName = "Slimify"
        archiveClassifier = ""
        val base = "com.gabrielmartins.slimify.libs"
        relocate("com.google", "$base.google")
        relocate("org.checkerframework", "$base.checkerframework")
        relocate("javax.annotation", "$base.javax.annotation")
        relocate("com.grinderwolf", "$base.swm")
        relocate("com.flowpowered", "$base.flowpowered")
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }

    jar {
        archiveClassifier = "plain"
    }

    build {
        dependsOn(shadowJar)
    }
}
