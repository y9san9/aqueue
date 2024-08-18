import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.vanniktech.maven.publish")
}

group = "me.y9san9.aqueue"

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)

    pom {
        name = "aqueue"
        description = "Asynchronous Queue with fine-grained control over concurrency"
        url = "https://github.com/y9san9/aqueue"

        licenses {
            license {
                name = "MIT"
                distribution = "repo"
                url = "https://github.com/y9san9/aqueue/blob/main/LICENSE.md"
            }
        }

        developers {
            developer {
                id = "y9san9"
                name = "Alex Sokol"
                email = "y9san9@gmail.com"
            }
        }

        scm {
            connection ="scm:git:ssh://github.com/y9san9/aqueue.git"
            developerConnection = "scm:git:ssh://github.com/y9san9/aqueue.git"
            url = "https://github.com/y9san9/aqueue"
        }
    }

    signAllPublications()
}
