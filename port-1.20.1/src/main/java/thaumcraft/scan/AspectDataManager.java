package thaumcraft.scan;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads explicit aspect data from data packs and exposes lookup methods.
 * Supports three formats:
 *  A) single item  : { "item":"minecraft:dirt",   "aspects": { "Terra":1, "Ordo":1 } }
 *     single entity: { "entity":"minecraft:sheep","aspects": { "Herba":1, "Terra":1 } }
 *  B) batch map    : { "minecraft:dirt": {...}, "entity/minecraft:sheep": {...} }
 *  C) file name encodes id; file body is a plain aspect map:
 *        aspects/minecraft_dirt.json  → { "Terra":1 }  (→ "minecraft:dirt")
 *        aspects/entity_minecraft_sheep.json → { "Herba":1 } (→ "minecraft:sheep")
 */
public class AspectDataManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();

    // explicit (data driven) maps
    private static final Map<ResourceLocation, Map<String, Integer>> ITEM_ASPECTS = new HashMap<>();
    private static final Map<ResourceLocation, Map<String, Integer>> ENTITY_ASPECTS = new HashMap<>();

    public AspectDataManager() {
        super(GSON, "aspects"); // data/<modid>/aspects/*.json
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> input, ResourceManager rm, ProfilerFiller profiler) {
        ITEM_ASPECTS.clear();
        ENTITY_ASPECTS.clear();

        for (var entry : input.entrySet()) {
            ResourceLocation fileId = entry.getKey(); // thaumcraft:<path>
            JsonElement root = entry.getValue();

            try {
                if (!root.isJsonObject()) continue;
                JsonObject obj = root.getAsJsonObject();

                // Format A: single item or single entity with "aspects" object
                if (obj.has("item") && obj.has("aspects") && obj.get("aspects").isJsonObject()) {
                    ResourceLocation rl = new ResourceLocation(obj.get("item").getAsString());
                    Map<String, Integer> aspects = parseAspectMap(obj.getAsJsonObject("aspects"));
                    if (!aspects.isEmpty()) ITEM_ASPECTS.put(rl, aspects);
                    continue;
                }
                if (obj.has("entity") && obj.has("aspects") && obj.get("aspects").isJsonObject()) {
                    ResourceLocation rl = new ResourceLocation(obj.get("entity").getAsString());
                    Map<String, Integer> aspects = parseAspectMap(obj.getAsJsonObject("aspects"));
                    if (!aspects.isEmpty()) ENTITY_ASPECTS.put(rl, aspects);
                    continue;
                }

                // Detect if this whole object looks like a plain aspect map (Format C body).
                // If ALL values are primitives (numbers) → treat as aspects for id derived from filename.
                if (isPlainAspectMap(obj)) {
                    ResourceLocation target = deriveIdFromFilename(fileId);
                    Map<String, Integer> aspects = parseAspectMap(obj);
                    if (target != null && !aspects.isEmpty()) {
                        // Heuristic: filenames starting with "entity_" go to entity map
                        if (fileId.getPath().contains("/") ?
                                fileId.getPath().substring(fileId.getPath().lastIndexOf('/') + 1).startsWith("entity_")
                                : fileId.getPath().startsWith("entity_")) {
                            ENTITY_ASPECTS.put(target, aspects);
                        } else {
                            ITEM_ASPECTS.put(target, aspects);
                        }
                    }
                    continue;
                }

                // Format B: batch map where each value is an object = aspect map
                boolean handledBatch = false;
                for (var e : obj.entrySet()) {
                    String key = e.getKey();
                    JsonElement val = e.getValue();
                    if (!val.isJsonObject()) continue;

                    if (key.startsWith("entity/")) {
                        String id = key.substring("entity/".length());
                        ResourceLocation rl = new ResourceLocation(id);
                        Map<String, Integer> aspects = parseAspectMap(val.getAsJsonObject());
                        if (!aspects.isEmpty()) ENTITY_ASPECTS.put(rl, aspects);
                        handledBatch = true;
                    } else if (key.contains(":")) {
                        ResourceLocation rl = new ResourceLocation(key);
                        Map<String, Integer> aspects = parseAspectMap(val.getAsJsonObject());
                        if (!aspects.isEmpty()) ITEM_ASPECTS.put(rl, aspects);
                        handledBatch = true;
                    }
                }
                if (handledBatch) continue;

                // Otherwise: ignore gracefully (unknown format), don't crash the game.
                // You can print a debug log here if you have a logger.

            } catch (Exception ex) {
                // Never crash on bad data; just skip this file.
                // You can replace the next line with a proper logger if present.
                System.out.println("[Thaumcraft] Failed to parse aspects file " + fileId + ": " + ex);
            }
        }
    }

    // --- helpers ---

    private static boolean isPlainAspectMap(JsonObject obj) {
        // True if EVERY value is a primitive number
        for (var e : obj.entrySet()) {
            JsonElement v = e.getValue();
            if (!v.isJsonPrimitive() || !v.getAsJsonPrimitive().isNumber()) return false;
        }
        return !obj.entrySet().isEmpty();
    }

    private static Map<String, Integer> parseAspectMap(JsonObject obj) {
        Map<String, Integer> map = new HashMap<>();
        if (obj == null) return map;
        for (var e : obj.entrySet()) {
            JsonElement v = e.getValue();
            if (v.isJsonPrimitive() && v.getAsJsonPrimitive().isNumber()) {
                map.put(e.getKey(), v.getAsInt());
            }
        }
        return map;
    }

    private static ResourceLocation deriveIdFromFilename(ResourceLocation fileId) {
        // fileId like thaumcraft:aspects/<name>
        String path = fileId.getPath();
        int slash = path.lastIndexOf('/');
        String name = (slash >= 0) ? path.substring(slash + 1) : path;
        if (name.endsWith(".json")) name = name.substring(0, name.length() - 5);

        // entity_minecraft_sheep → minecraft:sheep
        if (name.startsWith("entity_")) {
            String rest = name.substring("entity_".length());
            return splitNs(rest);
        }
        // minecraft_dirt → minecraft:dirt
        return splitNs(name);
    }

    private static ResourceLocation splitNs(String s) {
        if (s.contains(":")) return new ResourceLocation(s);
        int i = s.indexOf('_');
        if (i > 0) {
            String ns = s.substring(0, i);
            String path = s.substring(i + 1);
            return new ResourceLocation(ns, path);
        }
        return new ResourceLocation("minecraft", s);
    }

    // ==== public API ====

    /** Only explicit (data JSON) aspects, no generation. */
    public static Map<String, Integer> getExplicitAspects(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Collections.emptyMap();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        Map<String, Integer> map = ITEM_ASPECTS.get(id);
        return map != null ? map : Collections.emptyMap();
    }

    /** Back-compat wrapper: without level we can only return explicit data. */
    public static Map<String, Integer> getAspects(ItemStack stack) {
        return getAspects(stack, null);
    }

    /** With ServerLevel we can fall back to generator when explicit data missing. */
    public static Map<String, Integer> getAspects(ItemStack stack, ServerLevel level) {
        Map<String, Integer> explicit = getExplicitAspects(stack);
        if (!explicit.isEmpty()) return explicit;

        if (thaumcraft.config.TCConfig.GENERATE_ASPECTS.get() && level != null) {
            return AspectGenerator.generate(level, stack);
        }
        return Collections.emptyMap();
    }

    public static void grantScanForItem(net.minecraft.server.level.ServerPlayer player, ItemStack stack) {
        var aspects = getAspects(stack, player.serverLevel());
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!aspects.isEmpty()) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Scanned: " + id + " → " + aspects), true);
        } else {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("No aspects found for item: " + id), true);
        }
    }

    public static void grantScanForBlock(net.minecraft.server.level.ServerPlayer player, BlockState state, Level level, BlockPos pos) {
        ItemStack asItem = new ItemStack(state.getBlock().asItem());
        grantScanForItem(player, asItem);
    }

    public static void grantScanForEntity(net.minecraft.server.level.ServerPlayer player, Entity e) {
        var id = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
        Map<String, Integer> m = java.util.Collections.emptyMap(); // explicit entity map not wired yet
        if (!m.isEmpty())
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Scanned entity: " + id + " → " + m), true);
        else
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("No aspects found for entity: " + id), true);
    }
}
