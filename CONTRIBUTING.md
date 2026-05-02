# Contributing

Thanks for your interest in contributing to RtRQoL.

## Scope

This mod is strictly Quality-of-Life. Contributions should fix annoyances, improve feedback, or reduce friction. They should not change how the game plays.

**In scope:**
- Bug fixes for vanilla behavior that is clearly unintended (e.g. broken UI, pathfinding hangs, counter logic errors)
- UI improvements: better readability, missing information, visual feedback
- Performance fixes that don't change observable game behavior
- Console/debug ergonomics

**Out of scope:**
- Anything that meaningfully affects game difficulty (damage, costs, spawn rates, timers, economy values)
- New gameplay mechanics or systems
- Overhauls to how existing systems work, even if you think vanilla is wrong
- Content additions (new buildings, units, etc.)

If your change would make the game noticeably easier or harder, or alter how a system fundamentally behaves, it belongs in a separate mod rather than here. Exception: if vanilla behavior in one of these areas is the result of a clear bug, a fix is in scope even if it shifts difficulty as a side effect.

## Before opening a PR

1. Check that the fix targets a real vanilla bug or a clear usability gap, not just personal preference.
2. Read through the relevant decompiled game source to understand what you're patching.
3. Build and test the mod in-game: `./build.sh`, then copy `target/RtRQoL-1.0.jar` to `<game folder>/mods/`.

## Javassist notes

Patches live in `src/main/java/rtrqol/patch/`. Each implements `ModPatch` and is registered in `QoLMod.getPatches()` under the target class's slash-separated internal name. See the existing patches for patterns and gotchas, especially around cross-patch state sharing via `System.getProperties()`.

## Feature requests

Open an issue. Include what vanilla does, why it's a problem, and what you'd expect instead. Keep the scope note above in mind, requests that affect difficulty or change core mechanics will be closed.

## Code style

Match the existing style. No new dependencies. Keep patches small and surgical; avoid rewriting large method bodies when intercepting a single call will do.
