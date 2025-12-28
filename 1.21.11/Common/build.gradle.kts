plugins {
    id("fuzs.multiloader.multiloader-convention-plugins-common")
}

dependencies {
    modCompileOnlyApi(libs.puzzleslib.common)
    modCompileOnly(libs.jeiapi.common)
//    modCompileOnly(libs.reiapi.common)
//    modCompileOnly(libs.reidefaultplugin.common)
}
