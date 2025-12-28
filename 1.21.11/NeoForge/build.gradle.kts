plugins {
    id("fuzs.multiloader.multiloader-convention-plugins-neoforge")
}

dependencies {
    modCompileOnly(libs.puzzleslib.common)
    modApi(libs.puzzleslib.neoforge)
    modCompileOnly(libs.jeiapi.common)
    modLocalRuntime(libs.jei.neoforge)
//    modCompileOnly(libs.reiapi.neoforge)
//    modCompileOnly(libs.reidefaultplugin.neoforge)
//    modLocalRuntime(libs.bundles.rei.neoforge)
}
