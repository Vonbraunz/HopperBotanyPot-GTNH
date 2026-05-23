# Hopper Botany Pot — GTNH

A 1.7.10 backport of the Botany Pots concept, built for [GregTech: New Horizons](https://github.com/GTNewHorizons).

Place a pot, add soil, add a seed — it grows passively and automatically ejects harvests into a hopper below.

---

## Usage

- **Right-click** with a soil block to fill the pot
- **Right-click** with a seed to plant it
- **Shift + right-click** (empty hand) to remove the seed, then the soil
- Place a hopper or any inventory directly below the pot to collect output automatically

## Soil types

| Soil | Growth Speed |
|------|-------------|
| Farmland | 1.0x (fastest) |
| Mycelium | 0.8x |
| Soul Sand | 0.75x |
| Dirt / Grass | 0.5x |
| Sand | 0.4x |
| Gravel | 0.3x |

## Supported crops

- **CropsNH** — fully supported, uses each crop's own growth time
- **Pam's HarvestCraft** — works via OreDict auto-detection
- **Vanilla saplings** — all six types, drop logs and a 50% chance of the sapling back (oak also has a 10% apple chance)
- **Any OreDict seed** — automatically detected via `seedX` / `cropX` naming convention

## Configuration

`config/botanypots.cfg`

| Option | Default | Description |
|--------|---------|-------------|
| `defaultGrowthTicks` | 3600 | Base ticks for OreDict crops (3 min on farmland) |
| `defaultSaplingGrowthTicks` | 1200 | Base ticks for saplings (60 sec on farmland) |

## Recipe

```
S   S
S P S
  H
```

- **S** = Hardened Clay
- **P** = Flower Pot
- **H** = Hopper

## Compatibility

- Requires [GT:NH](https://github.com/GTNewHorizons) or any Forge 1.7.10 modpack
- Optional: CropsNH, Pam's HarvestCraft, WAILA (tooltip support built in)

## License

MIT
