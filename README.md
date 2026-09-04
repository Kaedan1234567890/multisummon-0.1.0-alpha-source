# Chill Zone Multi Summon 0.1.0-alpha-fix3

Server-side Fabric mod for Minecraft 26.2.

## Command

```text
/multisummon <entity> <count> [x y z]
```

Examples:

```text
/multisummon minecraft:creeper 25
/multisummon minecraft:cow 10 ~ ~ ~
/multisummon minecraft:zombie 50 100 64 -20
```

- Entity names autocomplete from Minecraft's entity registry.
- Coordinates use Minecraft's normal coordinate argument, including `~` relative coordinates.
- If coordinates are omitted, entities spawn at the command source's position.
- All entities are intentionally spawned on the exact same coordinates.
- Maximum count per command: 500.

## LuckPerms permission

```text
chillzonemultisummon.command.multisummon
```

Owner-only example:

```text
/lp group owner permission set chillzonemultisummon.command.multisummon true
/lp group admin permission set chillzonemultisummon.command.multisummon false
/lp group mod permission set chillzonemultisummon.command.multisummon false
/lp group member permission set chillzonemultisummon.command.multisummon false
```

The server console is also permitted to use the command.


## Fix 3
Updated Minecraft 26.2 resource identifiers from the removed `ResourceLocation` class to `net.minecraft.resources.Identifier`.
