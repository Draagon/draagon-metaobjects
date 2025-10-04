/*
 * MetaObjects Archetype Integration Test Verification Script
 *
 * This script verifies that the generated project:
 * 1. Has all expected files and directories
 * 2. Generated domain objects from metadata
 * 3. Compiles successfully with all dependencies
 * 4. Contains proper JPA annotations and Spring configuration
 */

// Check basic project structure
// basedir is provided by Maven archetype integration test framework, but points to test directory
// The actual generated project is in the project/ subdirectory
def projectBasedir = new File(basedir, "project/test-metaobjects-generated")
assert projectBasedir.exists()

// Use projectBasedir for all subsequent file checks
basedir = projectBasedir

// Verify basic Maven project structure
assert new File(basedir, "pom.xml").exists()
assert new File(basedir, "src/main/java").exists()
assert new File(basedir, "src/main/resources").exists()
assert new File(basedir, "src/test/java").exists()

// Verify Claude Code integration
assert new File(basedir, ".claude/CLAUDE.md").exists()

// Verify metadata files exist
assert new File(basedir, "src/main/resources/metadata/application-metadata.json").exists()
assert new File(basedir, "src/main/resources/metadata/database-overlay.json").exists()

// Verify templates exist
assert new File(basedir, "src/main/resources/templates/domain-object.mustache.yaml").exists()

// Verify application files
def packageDir = "src/main/java/com/example/test/metaobjects"
assert new File(basedir, packageDir + "/Application.java").exists()

// Verify service files (in root package, not subdirectory)
assert new File(basedir, packageDir + "/UserService.java").exists()
assert new File(basedir, packageDir + "/OrderService.java").exists()

// Verify repository files (in root package, not subdirectory)
assert new File(basedir, packageDir + "/UserRepository.java").exists()
assert new File(basedir, packageDir + "/OrderRepository.java").exists()

// Verify controller files (in root package, not subdirectory)
assert new File(basedir, packageDir + "/UserController.java").exists()
assert new File(basedir, packageDir + "/OrderController.java").exists()

// Verify test files (in root package, not subdirectories)
assert new File(basedir, "src/test/java/com/example/test/metaobjects/ApplicationIntegrationTest.java").exists()
assert new File(basedir, "src/test/java/com/example/test/metaobjects/UserServiceTest.java").exists()

// Check if code generation actually worked - verify generated domain objects exist
def generatedDir = "target/generated-sources/metaobjects/com/example/test/metaobjects/domain"
assert new File(basedir, generatedDir + "/User.java").exists()
assert new File(basedir, generatedDir + "/Order.java").exists()
assert new File(basedir, generatedDir + "/OrderItem.java").exists()

// Verify generated classes contain proper JPA annotations
def userFile = new File(basedir, generatedDir + "/User.java")
def userContent = userFile.text
assert userContent.contains("@Entity")
assert userContent.contains("@Id")
assert userContent.contains("@GeneratedValue")
assert userContent.contains("@Table")
assert userContent.contains("public class User")

def orderFile = new File(basedir, generatedDir + "/Order.java")
def orderContent = orderFile.text
assert orderContent.contains("@Entity")
assert orderContent.contains("public class Order")

// Verify Spring configuration
def appFile = new File(basedir, packageDir + "/Application.java")
def appContent = appFile.text
assert appContent.contains("@SpringBootApplication")
assert appContent.contains("@EnableJpaRepositories")

// Verify JPA repository pattern (files are in root package)
def userRepoFile = new File(basedir, packageDir + "/UserRepository.java")
def userRepoContent = userRepoFile.text
assert userRepoContent.contains("extends JpaRepository<User, Long>")
assert userRepoContent.contains("Optional<User> findByUsername")

// Verify service layer uses repositories (not ObjectManagerDB, files in root package)
def userServiceFile = new File(basedir, packageDir + "/UserService.java")
def userServiceContent = userServiceFile.text
assert userServiceContent.contains("UserRepository userRepository")
assert userServiceContent.contains("userRepository.save")
assert !userServiceContent.contains("ObjectManagerDB")
assert !userServiceContent.contains("ValueMetaObject")

// Verify POM contains correct dependencies
def pomFile = new File(basedir, "pom.xml")
def pomContent = pomFile.text
assert pomContent.contains("spring-boot-starter-data-jpa")
assert pomContent.contains("metaobjects-codegen-mustache")
assert !pomContent.contains("metaobjects-omdb")  // Should not have ObjectManagerDB

// Verify application.properties exists
assert new File(basedir, "src/main/resources/application.properties").exists()

println "✅ All archetype integration test verifications passed!"
println "✅ Generated project structure is correct"
println "✅ Code generation created expected domain objects"
println "✅ JPA annotations and Spring configuration verified"
println "✅ Clean separation from ObjectManagerDB achieved"