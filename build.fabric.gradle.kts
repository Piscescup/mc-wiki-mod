import org.gradle.api.publish.maven.MavenPublication

plugins {
	java
	`maven-publish`
	id("net.fabricmc.fabric-loom")
	id("com.replaymod.preprocess")
}

fun projectProperty(name: String) = property(name).toString()

val minecraftVersion = projectProperty("minecraft_version")
val loaderVersion = projectProperty("loader_version")
val fabricApiVersion = projectProperty("fabric_api_version")
val mcefModernVersion = projectProperty("mcef_modern_version")
val modMenuVersion = projectProperty("modmenu_version")
val modVersion = projectProperty("mod_version")
val modLoader = projectProperty("mod_loader")
val mavenGroup = projectProperty("maven_group")
val archivesBaseName = projectProperty("archives_base_name")

repositories {
	maven {
		name = "DimasKama"
		url = uri("https://maven.dimaskama.net/releases")
	}
	maven {
		name = "Terraformers"
		url = uri("https://maven.terraformersmc.com/releases")
	}
}

configurations.configureEach {
	resolutionStrategy.force("net.fabricmc:fabric-loader:$loaderVersion")
}

dependencies {
	minecraft("com.mojang:minecraft:$minecraftVersion")
	implementation("net.fabricmc:fabric-loader:$loaderVersion")
	implementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
	implementation("net.dimaskama:mcef-modern:$mcefModernVersion")
	implementation("com.terraformersmc:modmenu:$modMenuVersion")
}

fabricApi {
	configureDataGeneration {
		client = true
	}
}

sourceSets.main {
	resources {
		exclude(".cache/**")

		// Older nodes already receive generated resources from preprocessResources.
		val mainProjectName = rootProject.file("versions/mainProject").readText().trim()
		if (project.name != mainProjectName) {
			setSrcDirs(srcDirs.filterNot { it == file("src/main/generated") })
		}
	}
}

group = mavenGroup
version = "v$modVersion-$modLoader-mc-$minecraftVersion"
base.archivesName.set(archivesBaseName)

tasks.processResources {
	val properties = mapOf(
		"version" to modVersion,
		"loader_dependency" to projectProperty("loader_dependency"),
		"minecraft_dependency" to projectProperty("minecraft_dependency"),
		"java_dependency" to projectProperty("java_dependency"),
	)

	inputs.properties(properties)
	filesMatching("fabric.mod.json") {
		expand(properties)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.release.set(25)
	options.compilerArgs.add("-Xlint:-processing")
}

java {
	withSourcesJar()
	sourceCompatibility = JavaVersion.VERSION_25
	targetCompatibility = JavaVersion.VERSION_25
}

tasks.jar {
	from(rootProject.file("LICENSE")) {
		rename { "${it}_$archivesBaseName" }
	}
}

publishing {
	publications {
		register<MavenPublication>("mavenJava") {
			from(components["java"])
			artifactId = archivesBaseName
			version = "$modVersion-mc$minecraftVersion"
		}
	}
}
