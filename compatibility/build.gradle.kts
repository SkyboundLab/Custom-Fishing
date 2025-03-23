repositories {
    maven("https://maven.enginehub.org/repo/") // worldguard worldedit
    maven("https://jitpack.io/") // itemsadder customcrops
    maven("https://repo.papermc.io/repository/maven-public/") // paper
    maven("https://mvn.lumine.io/repository/maven-public/") // mythicmobs
    maven("https://nexus.phoenixdevt.fr/repository/maven-public/") // mmoitems
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // papi
    maven("https://r.irepo.space/maven/") // neigeitems
    maven("https://repo.oraxen.com/releases/") // oraxen
    maven("https://repo.auxilor.io/repository/maven-public/") // eco
    maven("https://nexus.betonquest.org/repository/betonquest/") // betonquest
    maven("https://repo.dmulloy2.net/repository/public/") // betonquest needs packet wrapper?
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.opencollab.dev/main/") // geyser
    maven("https://repo.codemc.org/repository/maven-public/") // beauty quest
    maven("https://repo.momirealms.net/releases/")
}

dependencies {
    compileOnly(project(":api"))
    compileOnly("net.momirealms:sparrow-heart:${rootProject.properties["sparrow_heart_version"]}")
    compileOnly("dev.dejvokep:boosted-yaml:${rootProject.properties["boosted_yaml_version"]}")
    compileOnly("net.kyori:adventure-api:${rootProject.properties["adventure_bundle_version"]}") {
        exclude(module = "adventure-bom")
        exclude(module = "checker-qual")
        exclude(module = "annotations")
    }
    compileOnly("org.jetbrains:annotations:${rootProject.properties["jetbrains_annotations_version"]}")
    // papi
    compileOnly("me.clip:placeholderapi:${rootProject.properties["placeholder_api_version"]}")
    // server
    compileOnly("dev.folia:folia-api:${rootProject.properties["paper_version"]}-R0.1-SNAPSHOT")
    // vault
    compileOnly("com.github.MilkBowl:VaultAPI:${rootProject.properties["vault_version"]}")
    // season
    compileOnly("com.github.Xiao-MoMi:Custom-Crops:3.5.7")
    compileOnly(files("libs/RealisticSeasons-api.jar"))
    compileOnly(files("libs/AdvancedSeasons-API.jar"))
    // enchantment
    compileOnly(files("libs/AdvancedEnchantments-api.jar"))
    // leveler
    compileOnly(files("libs/mcMMO-api.jar"))
    compileOnly("net.Indyuce:MMOCore-API:1.12.1-SNAPSHOT")
//    compileOnly("dev.aurelium:auraskills-api-bukkit:2.2.7")
    compileOnly(files("libs/AuraSkills-2.2.7.jar"))
    compileOnly("com.github.Archy-X:AureliumSkills:Beta1.3.21")
    compileOnly("com.github.Zrips:Jobs:v5.2.2.3")
    // quest
    compileOnly(files("libs/BattlePass-4.0.6-api.jar"))
    compileOnly(files("libs/ClueScrolls-4.8.7-api.jar"))
    compileOnly(files("libs/notquests-5.17.1.jar"))
    compileOnly(files("libs/beautyquests-1.0.4.jar"))
    compileOnly("org.betonquest:betonquest:2.1.3")
    // item
    compileOnly(files("libs/zaphkiel-2.0.24.jar"))
    compileOnly(files("libs/ExecutableItems-7.24.9.29.jar"))
    compileOnly(files("libs/SCore-5.24.9.29.jar"))
    compileOnly(files("libs/SCore-5.24.9.29.jar"))
    compileOnly(files("libs/libreforge-4.73.0.jar"))
    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.6.1")
    compileOnly("net.Indyuce:MMOItems-API:6.10-SNAPSHOT")
    compileOnly("io.lumine:MythicLib-dist:1.6.2-SNAPSHOT")
    compileOnly("pers.neige.neigeitems:NeigeItems:1.17.13")
    compileOnly("com.willfp:EcoItems:5.61.0")
    compileOnly("io.th0rgal:oraxen:1.168.0")
    compileOnly("com.github.brcdev-minecraft:shopgui-api:3.0.0")
    // entity
    compileOnly("io.lumine:Mythic-Dist:5.6.2")
    // eco
    compileOnly("com.willfp:eco:6.70.1")
    compileOnly("com.willfp:EcoJobs:3.56.1")
    compileOnly("com.willfp:EcoSkills:3.46.1")
    compileOnly("com.willfp:libreforge:4.58.1")
    // wg we
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.9")
    // cache
    compileOnly("com.github.ben-manes.caffeine:caffeine:${rootProject.properties["caffeine_version"]}")
    // Geyser
    compileOnly("org.geysermc.geyser:api:2.4.2-SNAPSHOT")
    // Floodgate
    compileOnly("org.geysermc.floodgate:api:2.2.3-SNAPSHOT")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
    dependsOn(tasks.clean)
}