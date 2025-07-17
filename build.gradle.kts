plugins {
	java
	id("org.springframework.boot") version "3.4.2"
	id("io.spring.dependency-management") version "1.1.7"
	id("nu.studer.jooq") version "9.0"
}

group = "team.klover"
version = "0.0.1-SNAPSHOT"
val queryDslVersion = "5.0.0" // QueryDSL Version Setting
val jooqVersion = "3.19.14" // JOOQ Version Setting

java {
	sourceCompatibility = JavaVersion.VERSION_21 // 빌드 자바 버전
	targetCompatibility = JavaVersion.VERSION_21 //
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
//	implementation("org.springframework.boot:spring-boot-docker-compose") 앱 실행시 도커 자동 실행
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("org.springframework.boot:spring-boot-starter-reactor-netty")
	developmentOnly("io.netty:netty-all:4.1.100.Final")

	// jjwt
	implementation("io.jsonwebtoken:jjwt-api:0.12.5")
	implementation("io.jsonwebtoken:jjwt-impl:0.12.5")
	implementation("io.jsonwebtoken:jjwt-jackson:0.12.5")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.google.api-client:google-api-client:2.2.0")

	//swagger
	implementation ("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")

	// redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis")

	// aws s3
	implementation(platform("software.amazon.awssdk:bom:2.24.0"))
	implementation("software.amazon.awssdk:s3")
	implementation("com.vladmihalcea:hibernate-types-60:2.21.1")

	// elastic search
	implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")

	//언어 감지
	implementation("com.github.pemistahl:lingua:1.2.2")

	// https://mvnrepository.com/artifact/net.datafaker/datafaker repost 가데이터 밀어넣기
	implementation("net.datafaker:datafaker:2.4.2")

	// rabbitMQ
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("org.springframework.boot:spring-boot-starter-amqp")
	testImplementation("org.springframework.amqp:spring-rabbit-test")

	// MongoDB
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

	// FCM (Firebase CLoud Messaging)
	implementation("com.google.firebase:firebase-admin:9.4.3")

	implementation("org.redisson:redisson-spring-boot-starter:3.44.0")

	// QueryDSL Implementation
	implementation ("com.querydsl:querydsl-jpa:${queryDslVersion}:jakarta")
	annotationProcessor("com.querydsl:querydsl-apt:${queryDslVersion}:jakarta")
	implementation("com.querydsl:querydsl-core:${queryDslVersion}")

	annotationProcessor("jakarta.annotation:jakarta.annotation-api")
	annotationProcessor("jakarta.persistence:jakarta.persistence-api")
	compileOnly("jakarta.annotation:jakarta.annotation-api")
	compileOnly("jakarta.persistence:jakarta.persistence-api")
// https://mvnrepository.com/artifact/org.hibernate/hibernate-spatial
	implementation("org.hibernate:hibernate-spatial:7.0.5.Final")

	// JetBrains annotations for QueryDSL
	implementation("org.jetbrains:annotations:24.0.1")

	// https://mvnrepository.com/artifact/org.jooq/jooq
	implementation("org.springframework.boot:spring-boot-starter-jooq")
	jooqGenerator("org.postgresql:postgresql:42.7.3") // 예시
	jooqGenerator("org.jooq:jooq:${jooqVersion}")
	jooqGenerator("org.jooq:jooq-meta:${jooqVersion}")
	// https://mvnrepository.com/artifact/org.modelmapper.extensions/modelmapper-jooq
	implementation("org.modelmapper.extensions:modelmapper-jooq:3.2.4")
	implementation("com.github.chhsiao90:modelmapper-module-java8-datatypes:1.2.1")
	implementation("com.github.chhsiao90:modelmapper-module-jsr310:1.2.1")
}

tasks.withType<Test> {
	useJUnitPlatform()
}


jooq {
	version.set(project.findProperty("jooqVersion") as String? ?: "3.19.14")

	configurations {
		create("main") {
			jooqConfiguration.apply {
				jdbc.apply {
					driver = "org.postgresql.Driver"
					url = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/meta_db"
					user = System.getenv("DB_USER") ?: "postgres"
					password = System.getenv("DB_PASSWORD") ?: "postgres"
				}

				generator.apply {
					name = "org.jooq.codegen.DefaultGenerator"

					database.apply {
						name = "org.jooq.meta.postgres.PostgresDatabase"
						inputSchema = "public"
						includes = ".*"
						excludes = "flyway_schema_history|spatial_ref_sys"
					}

					generate.apply {
						isDaos = true
						isRecords = true
						isFluentSetters = true
						isJavaTimeTypes = true
						isDeprecated = false
						isPojos = true
						isInterfaces = true
					}

					target.apply {
						packageName = "com.jooq.project.generated"
						directory = "build/generated-src/jooq/main"
					}
				}
			}
		}
	}
}


// 생성된 JOOQ 소스를 프로젝트 소스로 인식하게 설정
sourceSets {
	named("main") {
		java {
			setSrcDirs(listOf("src/main/java","build/generated/sources/annotationProcessor/java/main","build/generated-src/jooq/main"))
		}
	}
}

// JOOQ 코드 생성이 컴파일 전에 실행되도록 설정
tasks.named<JavaCompile>("compileJava") {
	dependsOn(tasks.named("generateJooq"))
	// QueryDSL 코드 생성을 위한 컴파일러 옵션 설정
	options.compilerArgs.addAll(listOf(
			"-processor", "com.querydsl.apt.jpa.JPAAnnotationProcessor"
	))
	options.generatedSourceOutputDirectory.set(file("build/generated/sources/annotationProcessor/java/main"))
}

tasks.named<JavaCompile>("compileTestJava") {
	dependsOn(tasks.named("generateJooq"))
}
