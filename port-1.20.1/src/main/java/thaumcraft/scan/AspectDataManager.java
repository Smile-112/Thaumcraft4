package thaumcraft.scan;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.*;

public class AspectDataManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final Map<ResourceLocation, Map<String, Integer>> ITEM_ASPECTS = new HashMap<>();
    public AspectDataManager() { super(GSON, "aspects"); }
    @Override protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager rm, net.minecraft.util.profiling.ProfilerFiller profiler) {
        ITEM_ASPECTS.clear();
        for (var e : jsonMap.entrySet()) {
            JsonObject obj = e.getValue().getAsJsonObject();
            Map<String, Integer> map = new HashMap<>();
            for (var entry : obj.entrySet()) map.put(entry.getKey(), entry.getValue().getAsInt());
            ITEM_ASPECTS.put(e.getKey(), map);
        }
    }
    // Возвращает ТОЛЬКО явные (из data JSON) аспекты предмета; генератор не используется
    public static Map<String, Integer> getExplicitAspects(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Collections.emptyMap();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        Map<String, Integer> map = ITEM_ASPECTS.get(id);
        return map != null ? map : Collections.emptyMap();
    }

    // Возвращает аспекты предмета, сначала explicit; если нет — автогенерация (если включена)
    public static Map<String, Integer> getAspects(ItemStack stack, ServerLevel level) {
        Map<String, Integer> explicit = getExplicitAspects(stack);
        if (!explicit.isEmpty()) return explicit;

        if (thaumcraft.config.TCConfig.GENERATE_ASPECTS.get() && level != null) {
            return AspectGenerator.generate(level, stack);
        }
        return Collections.emptyMap();
    }
    public static void grantScanForItem(ServerPlayer player, ItemStack stack) {
        var aspects = getAspects(stack, player.serverLevel());
        if (!aspects.isEmpty())
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Scanned: " + stack.getDisplayName().getString() + " → " + aspects), true);
        else
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("No aspects found for item"), true);
    }
    public static void grantScanForBlock(ServerPlayer player, BlockState state, Level level, BlockPos pos) {
        ItemStack stack = new ItemStack(state.getBlock().asItem());
        grantScanForItem(player, stack);
    }
    public static void grantScanForEntity(ServerPlayer player, Entity e) {
        var rl = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
        var aspects = ITEM_ASPECTS.get(new ResourceLocation(rl.getNamespace(), "entity/" + rl.getPath()));
        if (aspects != null && !aspects.isEmpty())
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Scanned entity: " + rl + " → " + aspects), true);
        else
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("No aspects found for entity: " + rl), true);
    }
}