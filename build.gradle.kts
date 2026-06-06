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
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	testCompileOnly("org.projectlombok:lombok")
	testAnnotationProcessor("org.projectlombok:lombok")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	runtimeOnly("com.h2database:h2")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
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
		System.getenv("SONAR_TOKEN")?.takeIf { it.isNotBlank() }?.let { token ->
			property("sonar.token", token)
		}
	}
}