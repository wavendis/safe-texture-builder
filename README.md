# Texture Builder (Safe)

A client-side Fabric mod for Minecraft **26.3** that randomly switches between your hotbar block slots after each placement, so walls, floors and paths come out textured instead of flat.

Unlike most random-slot mods, it never switches slots in the same tick as a placement and never switches more than once per tick. To the server it looks like you pressing 1–9 by hand, which avoids kicks from "bad packets" checks on servers that allow builder mods.

> Some servers ban random-placement mods in their rules no matter how they behave. Check the rules before using it.

## Features

- Picks a random hotbar slot after every block you place
- Per-slot **on/off** toggle
- Per-slot **prominence** slider (0–100) to control how often each block appears
- In-game config screen showing what's in each slot
- Settings saved between sessions
- Client-only: works on any server, nothing needed server-side

## Commands

| Command | Description |
|---|---|
| `/texturebuildersafe config` | Opens the config screen. |

## Controls

| Key | Default | Description |
|---|---|---|
| Toggle Texture Builder | `V` | Turns the mod on or off. Shows ON/OFF above the hotbar. Rebind it under *Options → Controls → Key Binds → Texture Builder*. |

## Config screen

| Column | Description |
|---|---|
| **Slot** | Hotbar slot 1–9. |
| **Item** | Icon and name of what's currently in that slot. |
| **Use** | `ON` includes the slot in the random pick, `OFF` skips it. |
| **Prominence** | Weight from 0 to 100. A slot's chance is its weight divided by the total weight of all usable slots. 0 means never. |

At the bottom, **Mod: ON/OFF** does the same as the toggle key and **Done** closes the screen.

Slots that are empty or hold something that isn't a block are always skipped.

**Example:** stone at 100, cobblestone at 50 and andesite at 50 gives roughly 50% stone, 25% cobblestone and 25% andesite.

Settings are saved to `.minecraft/config/tbsafe.properties` as soon as you change them.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) 0.20.1+ for Minecraft 26.3.
2. Put [Fabric API](https://modrinth.com/mod/fabric-api) in your `mods` folder.
3. Put `texturebuilder-safe-<version>.jar` in your `mods` folder.
4. Remove any other random-slot or builder mod so the two don't fight.

## How it works

1. You place a block with the held slot as normal.
2. On the **next** client tick, the mod picks one usable slot using the weights and selects it.
3. Minecraft sends that slot change at the start of the following tick. Holding right-click places at most once every 4 ticks, so a slot change and a placement never share a tick.

## Building

Requires **JDK 25**.

```bash
./gradlew build
```

The jar is written to `build/libs/`. To launch a dev client:

```bash
./gradlew runClient
```

## License

[MIT](LICENSE)
