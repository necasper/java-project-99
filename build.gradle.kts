plugins {
	java
	checkstyle
	id("org.springframework.boot") version "4.0.6"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.sonarqube") version "7.3.0.8198"
}

group = "hexlet.code"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyLocking {
	lockAllConfigurations()
}

tasks.withType<Test> {
	useJUnitPlatform()
}

sonar {
	properties {
		val projectKey = System.getenv("SONAR_PROJECT_KEY")?.takeIf { it.isNotBlank() }
			?: "necasper_java-project-99"
		val organization = System.getenv("SONAR_ORGANIZATION")?.takeIf { it.isNotBlank() }
			?: "necasper"
		property("sonar.projectKey", projectKey)
		property("sonar.organization", organization)
		property("sonar.host.url", "https://sonarcloud.io")
		property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
		System.getenv("SONAR_TOKEN")?.takeIf { it.isNotBlank() }?.let { token ->
			property("sonar.token", token)
			property("sonar.login", token)
		}
	}
}