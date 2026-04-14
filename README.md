# InstaSlate

[![GitHub Release](https://img.shields.io/github/v/release/SwordfishBE/InstaSlate?display_name=release&logo=github)](https://github.com/SwordfishBE/InstaSlate/releases)
[![GitHub Downloads](https://img.shields.io/github/downloads/SwordfishBE/InstaSlate/total?logo=github)](https://github.com/SwordfishBE/InstaSlate/releases)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/yur8ms3Y?logo=modrinth&logoColor=white&label=Modrinth%20downloads)](https://modrinth.com/mod/instaslatemod)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1514337?logo=curseforge&logoColor=white&label=CurseForge%20downloads)](https://www.curseforge.com/minecraft/mc-mods/instaslate)

Mining deepslate at vanilla speed just feels slow, especially once you have already worked your way up to a Netherite Pickaxe, Efficiency V, and Haste II. At that point, you want to keep the momentum going, not spend half your time waiting for dark stone to finally break.

This mod fixes exactly that. With InstaSlate installed, deepslate tears apart at stone-like speed under the right setup, and honestly, who would not want that?

InstaSlate is a Fabric mod that makes deepslate mine as fast as stone when the player uses:

- a Netherite Pickaxe
- Efficiency V
- Haste II

The mod is intentionally small and focused. If the conditions are not met, vanilla mining behavior is unchanged.

---

## ✨ Features

- Speeds up deepslate mining to stone-like speed under the correct setup
- Supports the full vanilla deepslate family, including deepslate ores
- Includes a simple config file
- Optional Mod Menu integration
- Optional Cloth Config screen on the client

---

## 🏠 Optional Client Integrations

`Mod Menu` is optional.
`Cloth Config` is also optional and is not required at runtime.

This means:

- Singleplayer with Cloth Config installed: full config screen
- Dedicated server: works normally, no Cloth Config needed
- Client without Cloth Config: the mod still works, but there is no config GUI

---

## ⚙️ Configuration

The config file is created at:

`config/instaslate.json`

Current option:

- `enabled`: enables or disables the mod behavior

---

## 📦 Installation

| Platform   | Link |
|------------|------|
| GitHub     | [Releases](https://github.com/SwordfishBE/InstaSlate/releases) |
| Modrinth | [InstaSlate](https://modrinth.com/mod/instaslatemod) |
| CurseForge | [InstaSlate](https://www.curseforge.com/minecraft/mc-mods/instaslate) |


1. Download the latest JAR from your preferred platform above.
2. Place the JAR in your server's `mods/` folder.
3. Make sure [Fabric API](https://modrinth.com/mod/fabric-api) is also installed.
4. Start Minecraft — the config file will be created automatically.

---

## 📦 Building from Source

```bash
git clone https://github.com/SwordfishBE/InstaSlate.git
cd InstaSlate
chmod +x gradlew
./gradlew build
# Output: build/libs/instaslate-<version>.jar
```

---

## 📄 License

Released under the [AGPL-3.0 License](LICENSE).
