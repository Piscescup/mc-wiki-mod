# Minecraft Wiki Mod

[返回](../README.md) | [English](README_ENUS.md)

Minecraft Wiki Mod 是一个适用于 Minecraft 26.2 的客户端 Fabric Mod。它可以通过命令搜索 Minecraft Wiki，并使用 MCEF Modern 提供的内置浏览器在游戏 GUI 中显示查询结果。

## 功能

- 使用 MediaWiki API 搜索条目并选择匹配结果。
- 在游戏内打开 Minecraft Wiki，无需手动切换到外部浏览器。
- 支持英文 Minecraft Wiki 和中文 Minecraft Wiki。
- 支持 16 种查询分类和命令参数补全。
- GUI、命令提示和分类名称均使用 Fabric Datagen 生成的翻译文件。
- Wiki 语言设置会保存到客户端配置文件。

## 运行依赖

| 依赖 | 版本 | 必需 |
| --- | --- | --- |
| Minecraft | 26.2 | 是 |
| Java | 25 或更高版本 | 是 |
| Fabric Loader | 0.19.3 或更高版本 | 是 |
| Fabric API | 0.158.0+26.2 | 是 |
| MCEF Modern | 0.3.3+mc26.2.jcef146.0.10 | 是 |

本 Mod 仅需安装在客户端，单人游戏和多人服务器均可使用，服务器不需要安装。

## 安装

1. 安装适用于 Minecraft 26.2 的 Fabric Loader。
2. 将 Fabric API、MCEF Modern 和本 Mod 的 JAR 文件放入客户端的 `mods` 目录。
3. 使用 Fabric 配置启动 Minecraft。

MCEF Modern 首次初始化时可能需要下载和解压 JCEF 浏览器运行时，因此需要网络连接并可能等待一段时间。

## 使用

主命令格式：

```text
/mc-wiki <category> <query>
```

`/mcwiki` 和 `/wiki` 是 `/mc-wiki` 的别名。

示例：

```text
/mc-wiki mobs pig
/mc-wiki blocks redstone
/wiki structures village
```

可用的 `category`：

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

## 语言设置

查看当前 Wiki 语言：

```text
/mc-wiki settings lang get
```

切换到英文 Wiki：

```text
/mc-wiki settings lang set en_us
```

切换到中文 Wiki：

```text
/mc-wiki settings lang set zh_cn
```

`en_us` 会查询 `minecraft.wiki`，`zh_cn` 会查询 `zh.minecraft.wiki`。该设置同时控制 GUI 和命令反馈所使用的语言。

配置保存在：

```text
config/minecraft-wiki/mc-wiki-client.properties
```

## 开发构建

项目使用 Gradle Wrapper 和 Fabric Loom，无需单独安装 Gradle。

Windows：

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runDatagen
.\gradlew.bat check
```

Linux 或 macOS：

```bash
./gradlew build
./gradlew runClient
./gradlew runDatagen
./gradlew check
```

构建产物位于 `build/libs`。Fabric Datagen 生成的语言文件位于：

```text
src/main/generated/assets/mc-wiki/lang/en_us.json
src/main/generated/assets/mc-wiki/lang/zh_cn.json
```

## 工作流程

1. 客户端命令读取分类、查询内容和当前 Wiki 语言。
2. MediaWiki API 返回搜索页面及其完整 URL。
3. Mod 选择最匹配的条目。
4. MCEF Modern 在 Minecraft GUI 中加载目标页面。

## 许可证

本项目使用 [GNU LGPL 2.1](../LICENSE) 许可证。
