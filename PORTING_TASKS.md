# Porting Tasks for Thaumcraft 4 to Forge 1.20.1

This file tracks high-level tasks for porting the mod. Update as tasks are completed.

## Task List
1. **Set up Gradle build system**
   - Add Gradle wrapper and scripts *(done)*
   - Configure Mojang + Parchment mappings
   - Add dependencies: Forge 1.20.1, Curios, EMI/JEI
2. **Base infrastructure**
   - Replace legacy registration with `DeferredRegister`
   - Update config, resource loading, and data generators
3. **Aspects and research system**
   - Port aspect definitions and assignment logic
   - Recreate research table, book, and GUI
4. **Blocks, items, and mechanisms**
   - Convert tile entities to block entities
   - Rework interaction and recipe systems (JSON/datagen)
5. **Mobs and creatures**
   - Port entity classes, AI, rendering, and loot tables
6. **World generation and structures**
   - Reimplement aura nodes, biomes, and structures with new data-driven API
7. **Rendering and HUD**
   - Update shaders, particles, and custom GUIs
8. **Networking and data persistence**
   - Replace old packet handlers with `SimpleChannel`
   - Ensure NBT/data migration
9. **Testing and optimization**
   - Run builds, launch client/server, profile performance

## Next Step
Configure `port-1.20.1/build.gradle` with Mojang + Parchment mappings and add dependencies for Forge 1.20.1, Curios, and EMI/JEI.
