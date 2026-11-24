# Slimify

[![Build](https://github.com/GabriellMartins/Slimify/actions/workflows/build.yml/badge.svg)](https://github.com/GabriellMartins/Slimify/actions/workflows/build.yml)
[![License: GPL v3](https://img.shields.io/badge/license-GPLv3-blue.svg)](LICENSE)
![Java 17](https://img.shields.io/badge/java-17-orange)
![Minecraft 1.8](https://img.shields.io/badge/minecraft-1.8-green)

Converts Minecraft world folders to the [Slime format](https://github.com/Grinderwolf/Slime-World-Manager) automatically when the server starts. Drop the jar in `plugins/` and every folder with `world` in its name becomes a `.slime` file.

The Slime World Manager converter is bundled inside the jar, so nothing else has to be installed.

## Installation

1. Download `Slimify-<version>.jar` from the [Releases](https://github.com/GabriellMartins/Slimify/releases) page.
2. Put it in your server's `plugins/` folder. The server must run on Java 17 or newer.
3. Start the server. Worlds are converted while the server loads and the files appear in `slime_worlds/`.
4. Optional: edit `plugins/Slimify/config.yml` and run `/slimify reload`.

## Features

- Automatic conversion on server load, no commands needed
- Original world folders are never modified or deleted
- Conflict policy for existing files: `SKIP`, `OVERWRITE` or `FAIL`
- Limits by chunk count and size to keep huge worlds out
- Output is verified (slime header) before it counts as converted
- One failing world never stops the others
- Commands to convert, inspect, list and delete
- Bukkit events and a service for other plugins
- Public API that can be shaded into your own plugin

## Commands

| Command | Permission | Description |
| --- | --- | --- |
| `/slimify convert <folder> [name]` | `slimify.command.convert` | Converts one world folder |
| `/slimify convertall` | `slimify.command.convertall` | Converts every world matching the configured names |
| `/slimify list` | `slimify.command.list` | Lists the `.slime` files |
| `/slimify info <folder>` | `slimify.command.info` | Chunks, region files and size of a world folder |
| `/slimify delete <name>` | `slimify.command.delete` | Deletes a `.slime` file (the world folder stays) |
| `/slimify reload` | `slimify.command.reload` | Reloads `config.yml` |

Alias: `/slime`. All permissions default to operators, and `slimify.*` grants everything.

## Configuration

```yaml
convert-on-startup: true
output-folder: slime_worlds
match:
  - world
on-conflict: SKIP
limits:
  max-chunks: 0
  max-size-mb: 0
```

| Key | Meaning |
| --- | --- |
| `convert-on-startup` | Convert matching worlds while the server loads |
| `output-folder` | Where `.slime` files are written, relative to the server folder |
| `match` | A folder is converted when its name contains any of these texts, case-insensitive |
| `on-conflict` | What to do when the `.slime` file already exists: `SKIP`, `OVERWRITE` or `FAIL` |
| `limits` | Refuse worlds above this many chunks or megabytes. `0` means no limit |

## How it works

On server load, Slimify scans the server folder for worlds (a folder with `level.dat` and `region/`) whose name matches, and writes one file per world:

```
world/            ->  slime_worlds/world.slime
world_nether/     ->  slime_worlds/world_nether.slime
world_the_end/    ->  slime_worlds/world_the_end.slime
```

The Slime format is meant for small maps. Use `limits` or trim big worlds before converting. Conversion runs on the main thread, so very large worlds delay startup.

## Modules and packages

| Module | Purpose |
| --- | --- |
| `slimify-api` | Public API. No dependencies. |
| `slimify-importer` | Conversion engine. Bundles the Slime World Manager importer. |
| `slimify-bukkit` | The plugin. |

```
com.gabrielmartins.slimify
├── api
│   ├── Slimify                     entry point
│   ├── option                      SlimifyOptions, WorldMatcher, WorldNamer, ConflictPolicy, WorldLimits
│   ├── result                      ConversionResult, ConversionReport, ConversionStatus
│   ├── world                       WorldInspector, WorldInfo
│   ├── storage                     SlimeStorage, SlimeFile
│   ├── format                      SlimeFormat, SlimeHeader
│   ├── event                       ConversionListener
│   └── exception                   SlimifyException and subtypes
├── importer
│   ├── SlimeImporter               Slimify implementation
│   ├── pipeline                    ConversionPipeline (validate, limits, conflicts, convert, verify)
│   ├── engine                      ConversionEngine, LegacyEngine, ConsoleCapture, OutputParser
│   ├── world                       AnvilInspector, RegionReader
│   ├── storage                     FileSlimeStorage
│   ├── format                      SlimeHeaderReader
│   ├── naming                      SlimeNames
│   └── event                       ListenerRegistry
└── bukkit
    ├── SlimifyPlugin
    ├── config                      SlimifyConfig
    ├── command                     SlimifyCommand, SubCommand, FolderResolver
    │   └── sub                     Convert, ConvertAll, List, Info, Delete, Reload
    ├── event                       WorldConvertedEvent, WorldConversionFailedEvent
    ├── listener                    BukkitBridge
    ├── service                     StartupConverter, ConsoleReporter
    └── message                     Messages, Formats
```

## Using it from your own plugin

Shade `slimify-importer` into your plugin (it brings `slimify-api` with it) and nobody has to install Slimify:

Published through [JitPack](https://jitpack.io), using a Git tag as the version:

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.GabriellMartins.Slimify:slimify-importer:<tag>")
}
```

Create an instance with your own rules. The API can rename worlds, change the output folder and choose which folders are converted, while the plugin always uses the defaults from `config.yml`.

```java
Slimify slimify = SlimeImporter.create(SlimifyOptions.builder()
        .root(getServer().getWorldContainer().toPath())
        .output(getDataFolder().toPath().resolve("worlds"))
        .matcher(WorldMatcher.containing("arena"))
        .namer(WorldNamer.prefixed("minigame_"))
        .conflictPolicy(ConflictPolicy.OVERWRITE)
        .limits(WorldLimits.of(2000, 50))
        .build());

ConversionReport report = slimify.convertAll();
ConversionResult single = slimify.convert(Path.of("arena_1"), "arena_one");
List<SlimeFile> files = slimify.storage().list();
WorldInfo info = slimify.inspector().inspect(Path.of("arena_1"));

slimify.addListener(new ConversionListener() {
    @Override
    public void finished(ConversionResult result) {
        getLogger().info(result.world() + " " + result.status());
    }
});
```

If the Slimify plugin is installed, you can use its instance instead:

```java
Slimify slimify = Bukkit.getServicesManager().load(Slimify.class);
```

Bukkit events fired by the plugin: `WorldConvertedEvent` and `WorldConversionFailedEvent`, both carrying the `ConversionResult`.

When shading, relocate the bundled libraries to avoid clashing with other plugins: `com.grinderwolf`, `com.flowpowered` and `com.google`.

## Building

Requires JDK 17.

```
./gradlew build
```

The plugin jar is written to `slimify-bukkit/build/libs/Slimify-<version>.jar`.

## Compatibility

Built against the PaperSpigot 1.8.8 API. The code targets Java 17, so the server has to run on Java 17 or newer.

## Contributing

Issues and pull requests are welcome. Run `./gradlew build` before opening a pull request: it compiles everything and runs the tests.

## Credits

The conversion itself is done by the importer of [Slime World Manager](https://github.com/Grinderwolf/Slime-World-Manager) by Grinderwolf.

## License

Slimify is licensed under the GNU General Public License v3.0 because it bundles the Slime World Manager importer, which uses the same license. See [LICENSE](LICENSE).
