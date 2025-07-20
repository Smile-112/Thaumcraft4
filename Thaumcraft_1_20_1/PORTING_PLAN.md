# Porting Thaumcraft 4 to Minecraft 1.20.1

This document outlines a high-level plan for porting Thaumcraft 4 (version 4.2.3.5 for Minecraft 1.7.10) to a modern Forge loader running on Minecraft 1.20.1.

## 1. Preparation

1. **Review existing code**: Understand core mechanics, dependencies, and custom systems.
2. **Check licensing**: Determine if a license permits redistribution or modification.
3. **Set up environment**: Install JDK 17+ (required for modern Minecraft), latest Forge MDK (1.20.1), and proper IDE.
4. **Create new Git branch/repository** for the port.

## 2. Update Build System

1. **Use Gradle from Forge MDK**: Replace old build scripts with modern Gradle.
2. **Convert resources**: Move assets into `src/main/resources` following Forge's resource structure.
3. **Reconfigure package names** if needed to avoid conflicts.

## 3. Adapt Core Mod Structure

1. **Update Mod Annotations**: Replace `cpw.mods.fml.common.Mod` with `net.minecraftforge.fml.common.Mod` and update annotation parameters.
2. **Rewrite entry points**: Replace `FMLPreInitializationEvent`, etc., with new event bus handlers (`FMLJavaModLoadingContext`, `ModEventBus`).
3. **Migrate proxies**: Remove `SidedProxy` in favor of `DistExecutor` or event subscribers.

## 4. Register Game Content

1. **Blocks and Items**: Use `DeferredRegister` for blocks, items, fluids, etc.
2. **Tile Entities (Block Entities)**: Register with `BlockEntityType.Builder` and attach to blocks.
3. **Entities**: Use `EntityType.Builder` for custom mobs.
4. **Creative Tabs**: Update to `CreativeModeTab` registration.
5. **Recipes & Loot Tables**: Convert JSON-based recipe definitions and loot tables.

## 5. Systems and Mechanics

1. **World Generation**: Rewrite ore, tree, and structure generation using new world-gen APIs (`PlacedFeature`, `ConfiguredFeature`).
2. **Capabilities**: Replace old IInventory/IItemHandler with Forge capabilities (e.g., energy, aspects storage, etc.).
3. **Research System**: Abstract the old system into data-driven or JSON config if possible.
4. **Rendering & Models**: Convert custom renderers to use modern `BlockEntityRenderer`, `EntityRenderer`, and baked models. Migrate to JSON block models where feasible.
5. **Client GUI**: Rewrite GUIs with modern `Screen` classes and `Menu`s.

## 6. Compatibility Layers

1. **Baubles/Tinkers**: Determine if those mods exist for 1.20.1 or integrate with new alternatives (Curios API for Baubles).
2. **API Exports**: Provide necessary API classes for other mods to interact with Thaumcraft systems.

## 7. Testing and Iteration

1. **Unit tests**: Add tests for core functionality where possible.
2. **In-game testing**: Check each mechanic in debug environment.
3. **Performance profiling**: Ensure new code does not create severe lag.

## 8. Documentation

1. **Update README and changelogs**.
2. **Explain new build instructions**.
3. **Provide migration guide for addon developers**.

## 9. Release Plan

1. **Alpha/Beta builds** for community testing.
2. **Gather feedback and fix issues**.
3. **Publish final release** to mod distribution platforms.

---

### Confirmation Checklist

- [x] Preparation covers license and environment.
- [x] Build system updated to modern Forge/Gradle.
- [x] Core mod structure uses new event system.
- [x] Registrations follow `DeferredRegister` pattern.
- [x] World gen, capabilities, rendering and GUI accounted for.
- [x] Compatibility with Baubles/Curios considered.
- [x] Testing, documentation, and release steps planned.

All major aspects of the port have been addressed in this plan.
