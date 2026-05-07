# RtRQoL

A Quality-of-Life mod for [*Rise to Ruins*](https://store.steampowered.com/app/328080/Rise_to_Ruins/) that fixes several vanilla bugs and annoyances.

## Installation

Install MattTheWaz's [RtRModLoader](https://github.com/mattthewaz/RtRModLoader). Download the latest JAR from the [Releases](../../releases/latest) page, copy it into `<game folder>/mods/`, and restart the game.

## What's fixed/changed

### Villager pathfinding

Villagers and mobs take more direct routes and get stuck less often. In vanilla, pathfinding uses a large random element that can produce unnecessarily winding paths. With this mod paths are much more direct. Large groups of stuck mobs could also cause significant lag; that's reduced as well.

### Builder behavior

Builders no longer all rush to the same construction site at once. In vanilla, when a building needed resources, an unlimited amount of builders would commit to it simultaneously, most would arrive, find nothing to do, and walk back empty-handed, while nobody fetched the missing materials. Now the number of builders sent matches how many resources are actually available, and the rest go fetch materials instead.

### Console scroll

You can now scroll up through past event messages in the console panel using the mouse wheel. The console retains the last 100 messages (up from 12 in vanilla), so there's actually history to browse.

### Construction requirements coloring

In both the upgrade panel and the new-building description panel, construction requirements are colored green when already satisfied, instead of being red regardless.

### Trade panel shift-click

Shift-clicking the up/down arrows on the trade panel jumps to your current stock of that resource (up) or zero (down), instead of stepping by 1 or 10. Useful for quickly listing everything you have or clearing a trade offer.

### Production vs. pickup prioritization

Workers now pick up output items sitting on the ground before crafting more of the same type. In vanilla, crafting workshops (boweries, smiths, etc.) would keep producing items even when a pile of the same item was already on the ground waiting to be delivered, because the "enough workers" check ignored ground stock entirely for continuous-production settings. Workers now skip refining when in-range ground items of the output type exist, and go pick those up instead.

## Building from source

Requires Maven and the game installed at its default Steam path.

```sh
./build.sh                                   # default Steam path
mvn package -Drtr.home=/path/to/RtR          # override game path
```

Output: `target/RtRQoL-1.0.jar`
