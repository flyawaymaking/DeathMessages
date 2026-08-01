# DeathMessages

DeathMessages is a lightweight Minecraft 1.20+ (Paper) plugin that enhances player death messages. It displays custom death messages, death coordinates, and adds a `[✨ Back]` button for players with the `deathmessages.dback` permission.

* Русский перевод конфига расположен [ЗДЕСЬ](/src/main/resources/ru_config.yml)

## 🧩 Version Compatibility

| **Plugin version** | **Supported Paper** | **Java** |
|--------------------|---------------------|----------|
| `1.4.1`            | `1.20` – `26.2`     | 25       |
| `1.3.1`            | `1.20` – `1.21.11`  | 21       |

## Features

- Custom death messages (players, mobs, fall damage, fire, etc.)
- Displays death coordinates to the player
- `[✨ Back]` button for players with the `deathmessages.dback` permission
- Config reload via `/deathmessages reload`
- Falls back to the default Minecraft message if no custom message is found

## Configuration (config.yml)

```yaml
settings:
  # Show death coordinates to the player
  show-death-coordinates: true
  # Show the back-to-deathpoint button
  show-back-button: true

messages:
  prefix: "<gray>[💀]</gray>"
  reload:
    no-permission: "<red>You don't have permission to use this command!"
    success: "<green>DeathMessages config reloaded!"
    usage: "<yellow>Usage: /deathmessages reload"
  deathback:
    invalid: "<red>Death point not found!"
    success: "<green>You have been teleported to your death point!"
  personal-message: "<red>Death coordinates: <yellow>X: {x} Y: {y} Z: {z}"
  back-button: "<green><bold>[✨ BACK]</bold></green>"
  back-hover: "<yellow>Click to return to your death point"

# Player death messages
death-messages:
  prefix: "<gray>"
  # Death by another player
  # Available placeholders: {player}, {killer}, {killer_health}
  player:
    - "⚔️ {player} was slain by {killer} [<red>❤</red>{killer_health}]"
    - "🎯 {player} fell to {killer} [<red>❤</red>{killer_health}]"
    - "⚡ {player} was killed by {killer} [<red>❤</red>{killer_health}]"
    - "💥 {player} lost a duel against {killer} [<red>❤</red>{killer_health}]"

  # OVERWORLD - Neutral & Passive
  wolf:
    - "⚔ {player} was torn apart by a wolf"
    - "♣ {player} became prey to a wolf"
# ... other death types ...
```

Supported placeholders in messages:

- `{player}` — name of the deceased player
- `{killer}` — name of the killer (player)
- `{killer_health}` — remaining health of the killer (player)

> If no message is found for a death type, the plugin will use Minecraft's default death message.

## In-Game Example

**Public message (broadcast):**

```
💥 Player was blown up by a Creeper
```

**Private message to the player:**

```
💀 Death coordinates: X: 124 Y: 68 Z: -32
```

**Private message to the player with `deathmessages.dback` permission:**

```
💀 Death coordinates: X: 124 Y: 68 Z: -32
[✨ BACK]
```

> Clicking the button teleports the player back to their death location.

## Commands & Permissions

- `/deathmessages reload` — reloads the config  
  (permission: `deathmessages.reload`)

- `deathmessages.dback` permission — displays the `[✨ Back]` button after death

## Installation

1. Download the **latest release** from the [Releases](../../releases) section
2. Place it into your `/plugins` folder
3. Restart the server
4. Configure `config.yml` if needed
5. Use `/deathmessages reload` to apply changes without a restart

## 📄 License

This plugin is distributed under the MIT License.
