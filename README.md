# InstaSlate

InstaSlate is a Fabric mod that makes deepslate mine as fast as stone when the player uses:

- a Netherite Pickaxe
- Efficiency V
- Haste II

The mod is intentionally small and focused. If the conditions are not met, vanilla mining behavior is unchanged.

## Features

- Speeds up deepslate mining to stone-like speed under the correct setup
- Includes a simple config file
- Optional Mod Menu integration
- Optional Cloth Config screen on the client

## Optional Client Integrations

`Mod Menu` is optional.
`Cloth Config` is also optional and is not required at runtime.

This means:

- Singleplayer with Cloth Config installed: full config screen
- Dedicated server: works normally, no Cloth Config needed
- Client without Cloth Config: the mod still works, but there is no config GUI

## Configuration

The config file is created at:

`config/instaslate.json`

Current option:

- `enabled`: enables or disables the mod behavior

## Building from Source

```bash
git clone https://github.com/SwordfishBE/InstaSlate.git
cd InstaSlate
chmod +x gradlew
./gradlew build
# Output: build/libs/instaslate-<version>.jar
```

---

## License

Released under the [AGPL-3.0 License](LICENSE).
