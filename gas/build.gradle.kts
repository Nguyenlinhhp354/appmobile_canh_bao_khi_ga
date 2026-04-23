// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
}
// Thêm dòng classpath này vào trong thẻ dependencies (nếu chưa có)
buildscript {
    dependencies {
        // Thêm dấu ngoặc đơn và ngoặc kép vào như thế này:
        classpath("com.google.gms:google-services:4.4.2")
    }
}