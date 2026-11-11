# Minecraft Cursed Technique Plugin

Cursed Technique Realms is a Minecraft plugin for Paper 1.21.1+ that adds working Jujutsu Kaisen-inspired cursed techniques, cursed energy management, cooldowns, combat abilities, domain expansion effects, a cursed talisman item, player technique selection, permissions, particles, sounds, damage, movement, and regeneration mechanics.

## Install

1. Install Java 21.
2. Run a Paper Minecraft server compatible with Minecraft 1.21.1 or newer.
3. Build the plugin with Maven:
   ```bash
   mvn package
   ```
4. Copy the generated `target/plugin.jar` file into the server's `plugins` directory.
5. Start or restart the server.
6. Confirm that the plugin loads successfully in the server console.

## Use

Players with the `cursed.use` permission can use:

```text
/cursed help
/cursed energy
/cursed technique
/cursed technique <limitless|cleave|boogie>
/cursed blackflash
/cursed limitless
/cursed cleave
/cursed boogie
/cursed reverse
/cursed domain
```

Abilities consume cursed energy and have individual cooldowns. Players regenerate 3 cursed energy every second up to a maximum of 100 energy.

Available abilities:

- Black Flash damages and knocks back a living target in front of the player.
- Limitless projects a damaging particle-based attack in the direction the player is facing.
- Cleave damages nearby living entities and applies Slowness.
- Boogie Woogie teleports the player toward the targeted block.
- Reverse Cursed Technique restores health and applies Regeneration.
- Domain Expansion: Malevolent Shrine grants the caster temporary Resistance and Strength while applying Slowness and Weakness to nearby living entities.

Administrators with the `cursed.admin` permission can receive the plugin's talisman:

```text
/cursed give
```

The command also works through the `ct` alias.

---

More Minecraft plugin projects by EnderDevelopment: https://github.com/EnderDevelopment

Project page: https://dash.enderdevelopment.com/community/7f0026d0-6e47-4cab-aca0-c6d54f52e5b0?utm_source=github&utm_medium=readme&utm_campaign=auto_publish&utm_content=7f0026d0-6e47-4cab-aca0-c6d54f52e5b0
