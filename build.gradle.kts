plugins {
	`maven-publish`
	id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT" apply false
	id("net.fabricmc.fabric-loom-remap") version "1.17-SNAPSHOT" apply false

	// Fallen-Breath's fork supports mixed obfuscated and unobfuscated projects.
	id("com.replaymod.preprocess") version "c5abb4fb12"
}

preprocess {
	strictExtraMappings = false

	val mc12110 = createNode("1.21.10", 1_21_10, "")
	val mc12111 = createNode("1.21.11", 1_21_11, "")
	val mc2612 = createNode("26.1.2", 26_01_02, "")
	val mc262 = createNode("26.2", 26_02_00, "")

	// The source tree is based on 26.2, so links point towards older versions.
	mc262.link(mc2612, file("versions/mapping-26.1.2-26.2.txt"))
	mc2612.link(mc12111, file("versions/mapping-1.21.11-26.1.2.txt"))
	mc12111.link(mc12110, file("versions/mapping-1.21.10-1.21.11.txt"))

	for (node in getNodes()) {
		findProject(node.project)?.extensions?.extraProperties?.set("mcVersion", node.mcVersion)
	}
}

subprojects {
	tasks.withType<Jar>().configureEach {
		if (name == "sourcesJar") {
			tasks.findByName("preprocessResources")?.let {
				dependsOn(it)
			}
		}
	}
}

val versionProjects = subprojects.toList()



tasks.register("buildAndGather") {
	group = "build"
	description = "Builds every supported Minecraft version and gathers release jars."

	versionProjects.forEach { versionProject ->
		evaluationDependsOn(versionProject.path)
		dependsOn(versionProject.tasks.named("build"))
	}

	doLast {
		val outputDirectory = layout.buildDirectory
			.dir("release")
			.get()
			.asFile

		delete(outputDirectory)
		outputDirectory.mkdirs()

		versionProjects.forEach { versionProject ->
			copy {
				from(versionProject.layout.buildDirectory.dir("libs")) {
					include("*.jar")
					exclude(
						"*-dev.jar",
						"*-sources.jar",
						"*-shadow.jar"
					)
				}

				into(outputDirectory)
				duplicatesStrategy = DuplicatesStrategy.INCLUDE
			}
		}
	}
}

