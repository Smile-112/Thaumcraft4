package thaumcraft.init;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import thaumcraft.Thaumcraft;
import thaumcraft.item.ThaumometerItem;
import thaumcraft.item.ThaumonomiconItem;

public class ModItems {
    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Thaumcraft.MODID);
    public static final RegistryObject<Item> THAUMOMETER = REGISTER.register("thaumometer", () -> new ThaumometerItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> THAUMONOMICON = REGISTER.register("thaumonomicon", () -> new ThaumonomiconItem(new Item.Properties().stacksTo(1)));
}