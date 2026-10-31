## Carpet XTS Addition · Rule Documentation (English)

> All rules default to vanilla behavior. **They only take effect when explicitly enabled.**

---

## Category Reference

| Category       | Meaning                                                                                                 |
|----------------|---------------------------------------------------------------------------------------------------------|
| `XTS`          | Carpet XTS Addition rules                                                                               |
| `SURVIVAL`     | Gameplay-facing; expands what is possible in Survival mode                                              |
| `EXPERIMENTAL` | Experimental; semantics and implementation may change between versions — test on a staging server first |

---

## Rule List

### (Creative Flight) `creativeFlight`

Allows Survival mode players to fly like in Creative mode.

Equip an **elytra** and hold a **firework rocket** to take off. Fuel consumption is governed by
`creativeFlightFuelBase`. If you run out of fuel or aren't wearing an elytra, you fall immediately.

- Type: `boolean`
- Default: `false`
- Options: `false`, `true`
- Categories: `XTS`, `SURVIVAL`

---

### (Creative Flight Fuel Base) `creativeFlightFuelBase`

A firework rocket of duration (charge count) **N** provides `creativeFlightFuelBase × (2 + N)` game ticks (gt) of flight
fuel.

Examples (default base `10`):

- 1-charge rocket → 10 × 3 = **30 gt** (~1.5 s)
- 3-charge rocket → 10 × 5 = **50 gt** (~2.5 s)

- Type: `int`
- Default: `10`
- Categories: `XTS`, `SURVIVAL`

---

### (Creative Flight Fall Has Damage) `creativeFlightFallHasDamage`

> This rule is **not** about granting fall-damage immunity while flying.

Controls whether falling from height still deals fall damage under creative flight.

- `true` (default): after creative flight ends, fall damage is applied as normal
- `false`: fall damage is not dealt when flight ends — note that this does **not** grant immunity to any other source of
  damage

Best used together with `creativeFlight`.

- Type: `boolean`
- Default: `true`
- Options: `false`, `true`
- Categories: `XTS`, `SURVIVAL`

---

### (Fake Player Control) `fakePlayerControl`

> Ported from the [`player control`](https://github.com/XiaotianNB666/playercontrol) mod

Controls who is allowed to control fake players.

Two forms of input are accepted:

- **Permission level**: set to `0` / `1` / `2` / `3` / `4` / `ops` — only players whose permission level is **at least**
  the configured value may control fake players

#### When enabled

`/player <player> control` lets you take control of a fake player (real-player control has some issues).

- Type: `String`
- Default: `false`
- Options: `false`, `0`, `1`, `2`, `3`, `4`, `ops`
- Categories: `XTS`, `EXPERIMENTAL`

---
### (TP Command Collection) `tpCommandCollection`

Enable tp-related commands like `/tpa` `/home` `/sethome` `/delhome` `/back` *(Maybe completed)*

- Type：`String`
- Default：`false`
- Options：`false`，`0`，`1`，`2`，`3`，`4`，`ops`
- Categories：`XTS`，`COMMAND`

---
### (Better Armor Trims) `betterArmorTrims`
~~Make armor trims great again.~~

Piglins will not attack players wearing **gold-trimmed** armor.

- Type: `boolean`
- Default: `true`
- Options: `false`, `true`
- Categories: `XTS`, `SURVIVAL`