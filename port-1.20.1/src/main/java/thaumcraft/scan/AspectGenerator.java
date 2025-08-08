package thaumcraft.scan;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.crafting.*;
import net.minecraft.core.registries.BuiltInRegistries;

import thaumcraft.config.TCConfig;

import java.util.*;

public class AspectGenerator {

    public enum Strategy { MINIMAL, MAXIMAL, FIRST;
        public static Strategy parse(String s) {
            if (s == null) return MINIMAL;
            return switch (s.toLowerCase(Locale.ROOT)) {
                case "maximal" -> MAXIMAL;
                case "first" -> FIRST;
                default -> MINIMAL;
            };
        }
    }

    private static final Map<ResourceLocation, Map<String, Integer>> CACHE = new HashMap<>();

    public static Map<String, Integer> generate(ServerLevel level, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Map.of();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (CACHE.containsKey(id)) return CACHE.get(id);

        Strategy pref = Strategy.parse(TCConfig.PREFER_STRATEGY.get());
        int maxDepth = TCConfig.MAX_DEPTH.get();
        boolean capEnabled = TCConfig.CAP_PER_ASPECT.get() >= 0;
        int cap = TCConfig.CAP_PER_ASPECT.get();

        Set<ResourceLocation> visiting = new HashSet<>();
        Map<String, Integer> result = generateFor(level, stack, pref, maxDepth, visiting);

        if (capEnabled && cap >= 0) result = capMap(result, cap);
        CACHE.put(id, result);
        return result;
    }

    private static Map<String, Integer> generateFor(ServerLevel level, ItemStack out, Strategy pref, int depth, Set<ResourceLocation> visiting) {
        if (depth <= 0) return Map.of();
        ResourceLocation outId = BuiltInRegistries.ITEM.getKey(out.getItem());
        if (!visiting.add(outId)) return Map.of(); // cycle guard

        List<Map<String, Integer>> candidates = new ArrayList<>();

        RecipeManager rm = level.getRecipeManager();

        // Crafting recipes
        for (CraftingRecipe r : rm.getAllRecipesFor(RecipeType.CRAFTING)) {
            ItemStack res = r.getResultItem(level.registryAccess());
            if (!ItemStack.isSameItemSameTags(res, new ItemStack(out.getItem()))) continue;
            Map<String, Integer> m = sumIngredients(level, r.getIngredients(), pref, depth - 1, visiting);
            m = addToolBonusIfApplicable(m, out);
            candidates.add(m);
        }

        // Cooking-like recipes
        candidates.addAll(generateFromCooking(level, out, pref, depth, visiting, RecipeType.SMELTING));
        candidates.addAll(generateFromCooking(level, out, pref, depth, visiting, RecipeType.BLASTING));
        candidates.addAll(generateFromCooking(level, out, pref, depth, visiting, RecipeType.SMOKING));
        candidates.addAll(generateFromCooking(level, out, pref, depth, visiting, RecipeType.CAMPFIRE_COOKING));

        // TODO: Arcane/Infusion when implemented → add bonuses based on config

        // Pick candidate per strategy
        Map<String, Integer> chosen = switch (pref) {
            case FIRST -> candidates.stream().findFirst().orElse(Map.of());
            case MAXIMAL -> candidates.stream().max(Comparator.comparingInt(AspectGenerator::sumAll)).orElse(Map.of());
            case MINIMAL -> candidates.stream().min(Comparator.comparingInt(AspectGenerator::sumAll)).orElse(Map.of());
        };

        visiting.remove(outId);
        return chosen;
    }

    private static List<Map<String,Integer>> generateFromCooking(ServerLevel level, ItemStack out, Strategy pref, int depth, Set<ResourceLocation> visiting, RecipeType<? extends AbstractCookingRecipe> type) {
        List<Map<String,Integer>> list = new ArrayList<>();
        RecipeManager rm = level.getRecipeManager();
        for (AbstractCookingRecipe r : rm.getAllRecipesFor(type)) {
            ItemStack res = r.getResultItem(level.registryAccess());
            if (!ItemStack.isSameItemSameTags(res, new ItemStack(out.getItem()))) continue;
            Map<String, Integer> m = sumIngredients(level, Arrays.asList(r.getIngredients().toArray(new net.minecraft.world.item.crafting.Ingredient[0])), pref, depth - 1, visiting);
            // add Ignis bonus for cooking
            m = add(m, "Ignis", TCConfig.BONUS_SMELTING_IGNIS.get());
            list.add(m);
        }
        return list;
    }

    private static Map<String, Integer> sumIngredients(ServerLevel level, List<net.minecraft.world.item.crafting.Ingredient> ings, Strategy pref, int depth, Set<ResourceLocation> visiting) {
        Map<String, Integer> acc = new HashMap<>();
        for (net.minecraft.world.item.crafting.Ingredient ing : ings) {
            if (ing.isEmpty()) continue;
            ItemStack[] opts = ing.getItems();
            Map<String, Integer> best = Map.of();
            if (opts.length == 0) continue;

            if (pref == Strategy.FIRST) {
                best = aspectsFor(level, opts[0], pref, depth, visiting);
            } else {
                // choose minimal or maximal
                for (ItemStack opt : opts) {
                    Map<String, Integer> cur = aspectsFor(level, opt, pref, depth, visiting);
                    if (best.isEmpty()) best = cur;
                    else {
                        int cmp = Integer.compare(sumAll(cur), sumAll(best));
                        if ((pref == Strategy.MINIMAL && cmp < 0) || (pref == Strategy.MAXIMAL && cmp > 0)) {
                            best = cur;
                        }
                    }
                }
            }
            acc = merge(acc, best);
        }
        return acc;
    }

    private static Map<String, Integer> aspectsFor(ServerLevel level, ItemStack stack, Strategy pref, int depth, Set<ResourceLocation> visiting) {
        // 1) explicit data first
        Map<String, Integer> explicit = AspectDataManager.getExplicitAspects(stack);
        if (explicit != null && !explicit.isEmpty()) return explicit;
        // 2) cache or generate
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (CACHE.containsKey(id)) return CACHE.get(id);
        Map<String, Integer> gen = generateFor(level, stack, pref, depth, visiting);
        CACHE.put(id, gen);
        return gen;
    }

    private static boolean isTool(ItemStack out) {
        Item i = out.getItem();
        return (out.getMaxDamage() > 0)
                || i instanceof SwordItem || i instanceof AxeItem || i instanceof PickaxeItem
                || i instanceof ShovelItem || i instanceof HoeItem
                || i instanceof BowItem || i instanceof CrossbowItem || i instanceof TridentItem;
    }

    private static Map<String,Integer> addToolBonusIfApplicable(Map<String,Integer> map, ItemStack out) {
        if (!isTool(out)) return map;
        int bonus = TCConfig.BONUS_TOOL_INSTRUMENTUM.get();
        if (bonus <= 0) return map;
        return add(map, "Instrumentum", bonus);
    }

    private static int sumAll(Map<String,Integer> m) {
        int s = 0; for (int v : m.values()) s += v; return s;
    }

    private static Map<String,Integer> merge(Map<String,Integer> a, Map<String,Integer> b) {
        if (a.isEmpty()) return new HashMap<>(b);
        Map<String,Integer> r = new HashMap<>(a);
        for (var e : b.entrySet()) r.merge(e.getKey(), e.getValue(), Integer::sum);
        return r;
    }

    private static Map<String,Integer> add(Map<String,Integer> a, String key, int v) {
        if (v <= 0) return a;
        Map<String,Integer> r = new HashMap<>(a);
        r.merge(key, v, Integer::sum);
        return r;
    }

    private static Map<String,Integer> capMap(Map<String,Integer> a, int cap) {
        if (cap < 0) return a;
        Map<String,Integer> r = new HashMap<>();
        for (var e : a.entrySet()) r.put(e.getKey(), Math.min(cap, e.getValue()));
        return r;
    }
}
