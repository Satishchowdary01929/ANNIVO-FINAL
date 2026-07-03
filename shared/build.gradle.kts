plugins {
  alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
  jvm() // Targets the JVM, making it directly compatible as a dependency for the Android application
  
  iosX64()
  iosArm64()
  iosSimulatorArm64()

  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.kotlinx.coroutines.core)
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.test)
      }
    }
  }
}
