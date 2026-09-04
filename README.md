# Chill Zone Multi Summon

Server-side Fabric mod for Minecraft 26.2.

Command:
`/msummon <entity> <amount> [x y z]`

Examples:
- `/msummon creeper 25`
- `/msummon cow 10 ~ ~ ~`
- `/msummon skeleton 50 100 64 -200`

Features:
- Entity autocomplete from Minecraft's entity registry.
- Vanilla-style coordinate argument, including `~ ~ ~`.
- No coordinates = your current position.
- All entities spawn at the exact same position.
- Maximum 100 entities per command.
- Owner-only via LuckPerms permission:
  `chillzonemultisummon.command.msummon`

Recommended permissions:
`/lp group owner permission set chillzonemultisummon.command.msummon true`
`/lp group admin permission set chillzonemultisummon.command.msummon false`
`/lp group mod permission set chillzonemultisummon.command.msummon false`
`/lp group member permission set chillzonemultisummon.command.msummon false`
