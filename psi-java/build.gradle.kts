import io.spine.internal.dependency.Guava
import io.spine.internal.dependency.IntelliJ
import io.spine.internal.dependency.Spine

repositories {
    intellijReleases
    jetBrainsCacheRedirector
}

dependencies {
    implementation(Guava.lib)
    api(project(":psi"))

    with(IntelliJ.Platform) {
        listOf(
            codeStyleImpl,
            projectModel,
            projectModelImpl,
            lang,
        ).forEach {
            api(it) {
                // Avoiding duplicated binding with Gradle.
                // Users will need to provide their own dependency on Slf4J.
                exclude(group = "org.slf4j")
            }
        }
    }

    api(IntelliJ.JavaPsi.api)
    api(IntelliJ.JavaPsi.impl)

    // The list of Maven groups we exclude when adding some IntelliJ artifact dependencies.
    val exclusions = listOf(
        // These two are not called as transitive dependencies of IntelliJ API we call.
        "com.jetbrains.infra",
        "ai.grazie.spell",

        // We add required IntelliJ Platform dependencies manually, as they are needed.
        "com.jetbrains.intellij.platform",

        // Avoiding the clash with Gradle dependencies.
        "org.codehaus.groovy",

        // Avoiding the clash with Gradle dependencies.
        "org.slf4j"
    )
    fun ModuleDependency.excludeMany(excl: Iterable<String> = exclusions) {
        excl.forEach { exclude(it) }
    }

    api(IntelliJ.Platform.langImpl) { excludeMany() }

    // To use `AsyncExecutionServiceImpl`, uncomment this:
    api(IntelliJ.Platform.ideImpl) { excludeMany() }

    // To use `NonProjectFileWritingAccessProvider`, uncomment the following:
    api(IntelliJ.Platform.ideCoreImpl) { excludeMany() }

    // To expose `JavaCodeStyleSettings` and other types from `com.intellij.psi.codeStyle`
    // which tools would use for the code style purposes.
    api(IntelliJ.Java.impl) {
        excludeMany(listOf(
            "ai.grazie.nlp",
            "ai.grazie.spell",
            "ai.grazie.utils",
            "org.jetbrains.teamcity",
            "com.jetbrains.infra",

            "com.jetbrains.intellij.platform",

            "com.jetbrains.intellij.jsp",
            "com.jetbrains.intellij.regexp",
            "com.jetbrains.intellij.spellchecker",
            "com.jetbrains.intellij.xml",
            "com.jetbrains.intellij.copyright",

            "com.sun.activation",
            "javax.xml.bind",
            "commons-collections",
            "net.jcip",
            "net.sourceforge.nekohtml",
            "one.util",
            "org.apache.velocity",
            "org.glassfish.jaxb",
            "org.slf4j",
            "oro",
        ))
    }

    //
    // Implementation dependencies on IntelliJ artifacts
    //---------------------------------------------------

    // To access `com.intellij.psi.JspPsiUtil` as a transitive dependency
    // used by `com.intellij.psi.impl.source.codeStyle.ImportHelper`.
    implementation(IntelliJ.Jsp.jsp) { excludeMany() }

    implementation(IntelliJ.Xml.xmlPsiImpl) { excludeMany() }

    implementation(IntelliJ.Platform.analysisImpl) { excludeMany() }
    implementation(IntelliJ.Platform.indexingImpl) { excludeMany() }



    testImplementation(Spine.base)
    testImplementation(Spine.testlib)
    testImplementation(project(":plugin-testlib"))
}
