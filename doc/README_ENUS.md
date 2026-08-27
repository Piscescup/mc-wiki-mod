# Minecraft Wiki Mod

[Back](../README.md) | [简体中文](README_ZHCN.md)

Minecraft Wiki Mod is a client-side Fabric mod for Minecraft 26.2. It searches the Minecraft Wiki through commands and displays the result in an in-game browser powered by MCEF Modern.

## Features

- Searches articles through the MediaWiki API and selects the best matching result.
- Opens the Minecraft Wiki inside the game without requiring an external browser.
- Supports both the English and Chinese Minecraft Wiki.
- Provides command completion for 16 search categories.
- Generates GUI, command, and category translations with Fabric Datagen.
- Saves the selected Wiki language in a client-side configuration file.

## Requirements

| Dependency | Version | Required |
| --- | --- | --- |
| Minecraft | 26.2 | Yes |
| Java | 25 or newer | Yes |
| Fabric Loader | 0.19.3 or newer | Yes |
| Fabric API | 0.158.0+26.2 | Yes |
| MCEF Modern | 0.3.3+mc26.2.jcef146.0.10 | Yes |

This mod is client-side only. It works in singleplayer and on multiplayer servers without being installed on the server.

## Installation

1. Install Fabric Loader for Minecraft 26.2.
2. Place Fabric API, MCEF Modern, and this mod's JAR file in the client's `mods` directory.
3. Start Minecraft using the Fabric profile.

MCEF Modern may need to download and extract the JCEF browser runtime during its first initialization. This requires an internet connection and may take a while.

## Usage

Main command syntax:

```text
/mc-wiki <category> <query>
```

`/mcwiki` and `/wiki` are aliases of `/mc-wiki`.

Examples:

```text
/mc-wiki mobs pig
/mc-wiki blocks redstone
/wiki structures village
```

Available categories:

```text
trade
brewing
enchanting
mobs
blocks
items
biome
status_effects
crafting
smelting
smithing
structures
redstone
commands
version_history
tutorials
```

## Language Settings

Show the current Wiki language:

```text
/mc-wiki settings lang get
```

Switch to the English Wiki:

```text
/mc-wiki settings lang set en_us
```

Switch to the Chinese Wiki:

```text
/mc-wiki settings lang set zh_cn
```

`en_us` searches `minecraft.wiki`, while `zh_cn` searches `zh.minecraft.wiki`. This setting also controls the language used by the GUI and command feedback.

The client configuration is stored at:

```text
config/minecraft-wiki/mc-wiki-client.properties
```

## Development

The project uses the Gradle Wrapper and Fabric Loom, so a separate Gradle installation is not required.

Windows:

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runDatagen
.\gradlew.bat check
```

Linux or macOS:

```bash
./gradlew build
./gradlew runClient
./gradlew runDatagen
./gradlew check
```

Build artifacts are written to `build/libs`. Fabric Datagen writes the generated language files to:

```text
src/main/generated/assets/mc-wiki/lang/en_us.json
src/main/generated/assets/mc-wiki/lang/zh_cn.json
```

## How It Works

1. The client command reads the category, query, and selected Wiki language.
2. The MediaWiki API returns matching pages and their full URLs.
3. The mod selects the best matching article.
4. MCEF Modern loads the page inside a Minecraft GUI.

## License

This project is licensed under the [GNU LGPL 2.1](../LICENSE).
