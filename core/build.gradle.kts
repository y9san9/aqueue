plugins {
    id("kmp-library-convention")
}

version = libs.versions.aqueue.get()

dependencies {
    commonMainImplementation(libs.coroutines)
}
