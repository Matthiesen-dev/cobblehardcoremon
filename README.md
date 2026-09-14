# CobbleHardcoreMon

<div>
  <img src="https://mods.matthiesen.dev/badges/matthiesenCore.svg" alt="Matthiesen Core">
  <img src="https://mods.matthiesen.dev/badges/cobblemon.svg" alt="Cobblemon">
</div>

Hardcore mode for your Cobblemon, if your cobblemon faints and is not holding a Totem, it's gone. Forever.

This is a server-side mod, designed to bring a new level of challenge to your Cobblemon experience. Instead of simply being able 
to revive your fainted Cobblemon, this mod introduces a permanent consequence for their defeat. If your Cobblemon faints and is not holding a Totem, 
(By default, Totems are Minecraft's "Totem of Undying", but this can be configured in the mod's settings), it will be permanently removed from your party. 
This adds a layer of strategy and risk to your battles as well as when your adventuring in your world, making every decision count.

**W's Permadeath**: This mod was inspired by the Author of [W's Permadeath](https://modrinth.com/datapack/ws-permadeath) datapack. 
This mod is an implementation of the same concept, but as a mod, it allows for more flexibility and customization via the mod's configuration settings.

## Requirements

- [Matthiesen Core](https://modrinth.com/mod/matthiesen-core)
- [Cobblemon](https://modrinth.com/mod/cobblemon)
- [Fabric API](https://modrinth.com/mod/fabric-api) (Fabric only)
- [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port) (Fabric only)

## Docs

Documentation for this mod can be found at [mods.matthiesen.dev](https://mods.matthiesen.dev/cobblehardcoremon/)

## Version Compatibility

| Minecraft Version | Matthiesen Core Version | Cobblemon Version | Mod Version |
|-------------------|-------------------------|-------------------|-------------|
| 1.21.1            | 1.2.7+                  | 1.8.0             | 1.x.x       |

## FastStats Metrics

This mod uses [FastStats](https://faststats.dev) to collect anonymous usage statistics. This helps the developer understand
how this mod is being used and improve it over time. You can learn more about the data collected and how it is used by visiting
[FastStats: Information](https://faststats.dev/info).

You can also view the data collected by this mod on the [FastStats: CobbleHardcoreMon](https://faststats.dev/project/cobblehardcoremon) page.

To opt out of this data collection, set the `enabled` property to `false` in the `<game_directory>/config/matthiesen_core/metrics.properties` file.

## License

MIT - see `LICENSE`.
