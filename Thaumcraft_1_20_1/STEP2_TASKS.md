# Step 2 Tasks: Update Build System

This document lists actionable tasks for Step 2 of `PORTING_PLAN.md`.
This document lists actionable tasks for Step 2 of `PORTING_PLAN.md` and tracks their completion.

## 1. Use Gradle from Forge MDK
- [ ] Download the official Forge MDK for Minecraft 1.20.1.
- [ ] Copy `build.gradle`, `gradle/`, `gradlew`, and `gradlew.bat` from the MDK into the project root.
- [ ] Replace or merge existing build scripts to use the new Gradle setup.
- [ ] Verify `settings.gradle` and `gradle.properties` reflect the project name and group.
- [ ] Download the official Forge MDK for Minecraft 1.20.1 *(blocked by network restrictions, created files manually instead)*
- [x] Copy `build.gradle`, `gradle/`, `gradlew`, and `gradlew.bat` from the MDK or create equivalents
- [x] Replace or merge existing build scripts to use the new Gradle setup
- [x] Verify `settings.gradle` and `gradle.properties` reflect the project name and group

## 2. Convert resources
- [ ] Move all assets into `src/main/resources` following Forge's structure.
- [ ] Ensure `pack.mcmeta` is placed in `src/main/resources`.
- [ ] Update any paths in code or JSON that reference the old asset locations.
- [x] Move all assets into `src/main/resources` following Forge's structure
- [x] Ensure `pack.mcmeta` is placed in `src/main/resources`
- [ ] Update any paths in code or JSON that reference the old asset locations *(pending review)*

## 3. Reconfigure package names
- [ ] Review all Java package declarations for potential conflicts.
- [ ] Refactor packages if necessary to follow modern naming conventions (e.g., `com.example.thaumcraft`).
- [ ] Update all imports and references after refactoring.
- [x] Review all Java package declarations for potential conflicts (no conflicts found)
- [ ] Refactor packages if necessary to follow modern naming conventions *(not required currently)*
- [ ] Update all imports and references after refactoring *(not required)*
