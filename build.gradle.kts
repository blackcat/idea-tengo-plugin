import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import java.io.File

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.16.0"
    id("org.jetbrains.grammarkit") version "2023.3.0.3"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    intellijPlatform {
        create(
            providers.gradleProperty("platformType"),
            providers.gradleProperty("platformVersion")
        )

        testFramework(TestFrameworkType.Platform)
    }

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.opentest4j:opentest4j:1.3.0")
}

intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = provider { null }
        }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }

    publishing {
        // token = providers.environmentVariable("PUBLISH_TOKEN")
    }
}

val generatedSourcesPath = layout.buildDirectory.dir("generated/sources")

sourceSets {
    main {
        java {
            srcDirs(generatedSourcesPath.map { it.dir("java") })
        }
    }
}

val generateTengoParser = tasks.register<GenerateParserTask>("generateTengoParser") {
    sourceFile.set(file("src/main/grammar/Tengo.bnf"))
    targetRootOutputDir.set(generatedSourcesPath.map { it.dir("java") })
    pathToParser.set("com/github/blackcat/tengo/parser/TengoParser.java")
    pathToPsiRoot.set("com/github/blackcat/tengo/psi")
    purgeOldFiles.set(true)
}

val generateTengoLexer = tasks.register<GenerateLexerTask>("generateTengoLexer") {
    dependsOn(generateTengoParser)
    sourceFile.set(file("src/main/grammar/Tengo.flex"))
    targetOutputDir.set(generatedSourcesPath.map { it.dir("java/com/github/blackcat/tengo/lexer") })
    purgeOldFiles.set(true)
}

tasks {
    withType<JavaCompile> {
        dependsOn(generateTengoLexer, generateTengoParser)
        sourceCompatibility = "21"
        targetCompatibility = "21"
        options.encoding = "UTF-8"
    }

    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
        distributionType = Wrapper.DistributionType.BIN
    }

    test {
        useJUnit()
    }

    runIde {
        jvmArgs("-Xms2g", "-Xmx8g")
    }

    // Disable bundled plugins that aren't needed in the sandbox. The Gradle plugin in
    // 2024.2 crashes parsing Java version "25" in its compatibility matrix, which has
    // nothing to do with our plugin — so we turn it off (and a couple of others) in the
    // sandbox config directory once prepareSandbox has materialised it.
    prepareSandbox {
        doLast {
            val configDir = layout.projectDirectory
                .dir(".intellijPlatform/sandbox/tengo/IC-2024.2/config").asFile
            configDir.mkdirs()
            val disabled = listOf(
                "org.jetbrains.plugins.gradle",
                "org.jetbrains.idea.maven",
                "com.intellij.gradle",
                "Git4Idea",
                "org.jetbrains.plugins.terminal",
                // Qodana in 2024.2 references coverage-rt.jar classes that aren't on
                // the sandbox classpath, so saving any inspection profile blows up.
                "org.intellij.qodana",
            )
            File(configDir, "disabled_plugins.txt").writeText(disabled.joinToString("\n"))
        }
    }
}
