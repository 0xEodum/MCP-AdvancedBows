# AdvancedBows

AdvancedBows is a Minecraft plugin that adds a variety of special bows with unique shooting mechanics to enhance combat and gameplay.

## Features

- Seven unique special bows, each with distinct abilities and effects
- Command system for easily giving bows to players
- Custom particle effects and visual feedback
- Configurable parameters for each bow type
- Charging mechanics for certain bows

## Installation

1. Download the AdvancedBows plugin JAR file
2. Place it in your server's `plugins` folder
3. Restart your server or use a plugin manager to load the plugin
4. Configure settings in the `config.yml` file if needed

## Commands

- `/givebow <player> <bow_type> [parameters...]` - Gives a special bow to a player
  - Requires the `advancedbows.give` permission (default: op)

## Available Bows

### Multishot Bow

A bow that fires multiple arrows simultaneously in a spread pattern.

- **Parameters**:
  - `arrowCount` - Number of additional arrows fired (default: 3)
  - `maxDeviation` - Maximum angle deviation for arrows in degrees (default: 5.0)
- **Usage Example**: `/givebow Player1 MULTISHOT arrowCount=5 maxDeviation=8.0`

### Homing Bow

Fires arrows that track and follow the nearest target within range.

- **Parameters**:
  - `targetType` - Type of entities to target (PLAYER or DEBUG) (default: PLAYER)
  - `range` - Maximum range to detect targets in blocks (default: 10.0)
  - `pursuit` - Tracking strength from 0.1 to 1.0 (default: 0.5)
- **Usage Example**: `/givebow Player1 HOMING targetType=PLAYER range=15.0 pursuit=0.7`

### End Bow

Fires arrows that teleport above their target before striking from above.

- **Parameters**:
  - `targetType` - Type of entities to target (PLAYER or DEBUG) (default: PLAYER)
  - `range` - Maximum range to detect targets in blocks (default: 10.0)
  - `teleportRadius` - Radius around target for teleport location (default: 3.0)
  - `teleportHeight` - Height above target to teleport (default: 5.0)
- **Usage Example**: `/givebow Player1 END targetType=PLAYER range=12.0 teleportHeight=7.0`

### Explosion Bow

Fires arrows that explode on impact. Requires charging for maximum effect.

- **Parameters**:
  - `chargeTime` - Time in seconds to fully charge the bow (default: 3.0)
  - `explosionPower` - Power of the explosion from 1.0 to 4.0 (default: 2.0)
- **Usage Example**: `/givebow Player1 EXPLOSION chargeTime=2.0 explosionPower=3.0`
- **Charging Mechanic**: Hold right-click to charge. A circular particle effect shows the charging progress.
- **Full Charge Effect**: When fully charged, arrows will explode with maximum power on impact.

### Yoimiya Bow

Inspired by the character Yoimiya, this bow accumulates charges while drawing and fires additional flaming arrows.

- **Parameters**:
  - `targetType` - Type of entities to target (PLAYER or DEBUG) (default: PLAYER)
  - `range` - Maximum range to detect targets in blocks (default: 10.0)
  - `pursuit` - Tracking strength from 0.1 to 1.0 (default: 0.7)
- **Usage Example**: `/givebow Player1 YOIMIYA targetType=PLAYER range=15.0 pursuit=0.8`
- **Charging Mechanic**: While drawing the bow, up to 4 rings appear around the player. Each ring adds an additional fire arrow.
- **Full Charge Effect**: With 4 rings (full charge), the main arrow also becomes a homing fire arrow.

### Soul Bow

A mystical bow that blinds the shooter to reveal all nearby targets and fires a soul beam through obstacles.

- **Parameters**:
  - `targetType` - Type of entities to target (PLAYER or DEBUG) (default: PLAYER)
  - `range` - Maximum range to detect targets in blocks (default: 8.0)
  - `beamLength` - Length of the soul beam in blocks (default: 20.0)
  - `fangRadius` - Radius of evoker fang spawn around target (default: 3.0)
  - `fangCount` - Number of evoker fangs to spawn (default: 8)
- **Usage Example**: `/givebow Player1 SOUL targetType=PLAYER beamLength=25.0 fangCount=12`
- **Charging Mechanic**: Hold right-click to charge. Soul flames appear in a circle while charging.
- **Full Charge Effect**: When fully charged, briefly blinds the shooter and applies glowing effect to all valid targets in range.
- **Special Effects**: On hit, reduces target health by 70%, spawns evoker fangs around the target, and summons helper vexes to attack the target.

### Cursed Bow

A dark bow that releases cursed nodes of magic instead of arrows, each applying different negative effects.

- **Parameters**:
  - `targetType` - Type of entities to target (PLAYER or DEBUG) (default: PLAYER)
  - `range` - Maximum range to detect targets in blocks (default: 10.0)
  - `chargeTime` - Time in seconds to fully charge the bow (default: 3.0)
  - `damagePercent` - Percentage of max health to deal as damage (default: 5.0)
  - `effectDuration` - Duration of debuff effects in seconds (default: 5)
- **Usage Example**: `/givebow Player1 CURSED targetType=PLAYER damagePercent=7.0`
- **Charging Mechanic**: Hold right-click to charge. Up to 8 magic nodes appear while charging.
- **Special Effects**: Each node applies a different negative effect (Poison, Slowness, Mining Fatigue, Hunger, Weakness, Wither, Blindness, Levitation).

## Configuration

The plugin's `config.yml` file allows customization of the default parameters for each bow type:

```yaml
bows:
  multishot:
    default_arrow_count: 3
    default_max_deviation: 5.0
    
  homing:
    default_target_type: PLAYER
    default_range: 10.0
    default_pursuit: 0.5
    
  end:
    default_target_type: PLAYER
    default_range: 10.0
    default_teleport_radius: 3.0
    default_teleport_height: 5.0
    
  yoimiya:
    default_target_type: PLAYER
    default_range: 10.0
    default_pursuit: 0.7
    
  explosion:
    default_charge_time: 3.0
    default_explosion_power: 2.0
    
  soul:
    default_target_type: PLAYER
    default_range: 8.0
    default_beam_length: 20.0
    default_fang_radius: 3.0
    default_fang_count: 8
    
  cursed:
    default_target_type: PLAYER
    default_range: 10.0
    default_charge_time: 3.0
    default_damage_percent: 5.0
    default_effect_duration: 5
```

## Tips & Tricks

- For DEBUG targeting, iron golems will be targeted instead of players
- Some bows like Soul and Cursed bows remove the arrow entity entirely and replace it with special projectiles
- The particle effects provide visual feedback on charging progress

## Permissions

- `advancedbows.give` - Allows use of the `/givebow` command (default: op)

## Compatibility

- Requires Minecraft 1.16 or higher
- Works with Spigot, Paper, and most Bukkit-based servers
