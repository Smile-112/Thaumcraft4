package thaumcraft.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import thaumcraft.Thaumcraft;

public class ThaumometerOverlay implements IGuiOverlay {

    private static final ResourceLocation FRAME = new ResourceLocation(Thaumcraft.MODID, "textures/item/thaumometer_frame.png");
    private static final ResourceLocation GLASS = new ResourceLocation(Thaumcraft.MODID, "textures/item/thaumometer_glass.png");

    @Override
    public void render(ForgeGui gui, GuiGraphics gg, float partialTick, int width, int height) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return;

        var held = player.getItemInHand(InteractionHand.MAIN_HAND);
        boolean show = false;
        if (held != null && !held.isEmpty()) {
            var key = ForgeRegistries.ITEMS.getKey(held.getItem());
            show = key != null && Thaumcraft.MODID.equals(key.getNamespace()) && "thaumometer".equals(key.getPath());
        }
        if (!show) return;

        int size = Math.min(width, height) / 3;
        int x = (width - size) / 2;
        int y = (height - size) / 2;

        RenderSystem.enableBlend();
        gg.blit(FRAME, x, y, 0, 0, size, size, size, size);
        gg.blit(GLASS, x, y, 0, 0, size, size, size, size);

        if (mc.hitResult != null) {
            String name = mc.hitResult.getType().toString();
            gg.drawCenteredString(mc.font, name, width / 2, y + size + 8, 0xFFFFFF);
        }
    }

    @Mod.EventBusSubscriber(modid = Thaumcraft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
    public static class Registrar {
        @SubscribeEvent
        public static void onRegister(RegisterGuiOverlaysEvent e) {
            e.registerAboveAll("thaumometer", new ThaumometerOverlay());
        }
    }
}
