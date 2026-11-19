# VampSlayerHelper — Fabric 26.2

Client-side Fabric mod targeting Minecraft **26.2**.

## Build on GitHub

The workflow in `.github/workflows/build.yml` builds the mod automatically on every push and pull request. You can also start it manually from **Actions → Build Fabric Mod → Run workflow**.

After a successful run, download the artifact named `VampSlayerHelper-fabric-26.2`. The playable mod JAR is in that artifact. Do not put the `-sources.jar` file into the Minecraft mods folder.

## Local build

Requirements: JDK 25 and Gradle 9.6.1.

```bash
gradle clean build
```

The mod JAR is generated in `build/libs/`.

## In-game setup

1. Install Fabric Loader for Minecraft 26.2 and a matching Fabric API.
2. Put `vampslayerhelper-1.0.0.jar` into `.minecraft/mods`.
3. Start the game once. The configuration file `config/vampslayerhelper.json` is generated.
4. Assign the Toggle and Sound-Debug keys in Controls → Key Binds → Miscellaneous.

There is no server whitelist or Hypixel-specific block in this source tree.
