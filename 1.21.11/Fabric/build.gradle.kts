plugins {
    id("fuzs.multiloader.multiloader-convention-plugins-fabric")
}

dependencies {
    modApi(libs.fabricapi.fabric)
    modApi(libs.puzzleslib.fabric)
    modCompileOnly(libs.jeiapi.common)
    modLocalRuntime(libs.jei.fabric)
//    modCompileOnly(libs.reiapi.fabric)
//    modCompileOnly(libs.reidefaultplugin.fabric)
//    modLocalRuntime(libs.bundles.rei.fabric)
}

multiloader {
    modFile {
        json {
            entrypoint(
                "jei_mod_plugin",
                "${project.group}.integration.jei.StylishEffectsJeiPlugin"
            )
//            entrypoint(
//                "rei_client",
//                "${project.group}.integration.rei.StylishEffectsReiClientPlugin"
//            )
        }
    }
}
