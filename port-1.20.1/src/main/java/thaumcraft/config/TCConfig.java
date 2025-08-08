package thaumcraft.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class TCConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec.BooleanValue GENERATE_ASPECTS;
    public static final ForgeConfigSpec.IntValue CAP_PER_ASPECT;
    public static final ForgeConfigSpec.IntValue MAX_DEPTH;
    public static final ForgeConfigSpec.ConfigValue<String> PREFER_STRATEGY;
    public static final ForgeConfigSpec.IntValue BONUS_TOOL_INSTRUMENTUM;
    public static final ForgeConfigSpec.IntValue BONUS_SMELTING_IGNIS;
    public static final ForgeConfigSpec.IntValue BONUS_ARCANE_MAGIC;
    public static final ForgeConfigSpec.DoubleValue INFUSION_COMPLEXITY_FACTOR;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("aspects");
        GENERATE_ASPECTS = b.comment("Enable automatic aspect generation from recipes when explicit data is missing.")
                .define("generate_aspects", true);
        CAP_PER_ASPECT = b.comment("Cap per aspect value. -1 disables cap. Default 128.")
                .defineInRange("cap_per_aspect", 128, -1, Integer.MAX_VALUE);
        MAX_DEPTH = b.comment("Max recursion depth for generator to prevent cycles.")
                .defineInRange("max_depth", 8, 1, 64);
        PREFER_STRATEGY = b.comment("Recipe preference when multiple recipes exist: minimal|maximal|first")
                .define("prefer_recipe", "minimal");
        BONUS_TOOL_INSTRUMENTUM = b.comment("Additional Instrumentum added if result is a tool/weapon.")
                .defineInRange("bonus_tool_instrumentum", 1, 0, 16);
        BONUS_SMELTING_IGNIS = b.comment("Additional Ignis added for smelting/blasting/smoking/campfire outputs.")
                .defineInRange("bonus_smelting_ignis", 1, 0, 16);
        BONUS_ARCANE_MAGIC = b.comment("Additional Praecantatio for arcane crafts (when implemented).")
                .defineInRange("bonus_arcane_magic", 2, 0, 32);
        INFUSION_COMPLEXITY_FACTOR = b.comment("Praecantatio added per infusion complexity unit (when implemented).")
                .defineInRange("infusion_complexity_factor", 1.0, 0.0, 64.0);
        b.pop();
        COMMON_SPEC = b.build();
    }
}
