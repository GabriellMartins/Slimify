plugins {
    `java-library`
}

dependencies {
    api(project(":slimify-api"))
    implementation(libs.swm.importer)
}
