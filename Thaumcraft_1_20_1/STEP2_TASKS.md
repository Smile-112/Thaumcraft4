# Step 2 Tasks: Update Build System

This document lists actionable tasks for Step 2 of `PORTING_PLAN.md` and tracks their completion.

## 1. Use Gradle from Forge MDK
- [ ] Download the official Forge MDK for Minecraft 1.20.1 *(blocked by network restrictions, created files manually instead)*
- [x] Download the official Forge MDK for Minecraft 1.20.1 (used to verify configuration)
- [x] Copy `build.gradle`, `gradle/`, `gradlew`, and `gradlew.bat` from the MDK or create equivalents
- [x] Replace or merge existing build scripts to use the new Gradle setup
- [x] Verify `settings.gradle` and `gradle.properties` reflect the project name and group
- [x] Checked MDK `build.gradle` to confirm plugin and dependency versions

## 2. Convert resources
- [x] Move all assets into `src/main/resources` following Forge's structure
- [x] Ensure `pack.mcmeta` is placed in `src/main/resources`
- [ ] Update any paths in code or JSON that reference the old asset locations *(pending review)*
- [x] Update any paths in code or JSON that reference the old asset locations *(no changes required)*
- [x] Remove stray `.DS_Store` file from repository

## 3. Reconfigure package names
- [x] Review all Java package declarations for potential conflicts (no conflicts found)
- [ ] Refactor packages if necessary to follow modern naming conventions *(not required currently)*
- [ ] Update all imports and references after refactoring *(not required)*
