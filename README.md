# UnstableEnvoys

Timed envoy events for Minecraft servers: loot crates drop around the world, players race to find and open them, and rewards pay out.

Developed by UnstableMC. Forked from [AxEnvoys](https://github.com/Artillex-Studios/AxEnvoys) by Artillex Studios (MIT).

## Requirements

- Java 21
- Spigot/Paper (Folia supported)
- Optional: PlaceholderAPI, Multiverse-Core, WorldGuard, ItemsAdder, Oraxen

## Building

```
mvn clean package
```

## Commands and permissions

Commands: `/envoy`, `/envoys`, `/unstableenvoys`, `/uenvoy`.
Permissions use the `unstableenvoys.command.<sub>` format (flare, start, stop, stopall, reload, center, editor, coords, toggle, time).
PlaceholderAPI identifier: `unstableenvoys`.

## Migrating from AxEnvoys

Permissions, the PlaceholderAPI identifier and the data folder (`plugins/UnstableEnvoys`) have changed. Copy your old `envoys/`, `crates/` and `messages.yml` over and update permissions and placeholders.
