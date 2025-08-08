
package thaumcraft.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import net.minecraft.world.InteractionHand;

public class ThaumometerOverlay implements IGuiOverlay {

    private static final ResourceLocation FRAME = new ResourceLocation(Thaumcraft.MODID, "textures/item/thaumometer_frame.png");
    private static final ResourceLocation GLASS = new ResourceLocation(Thaumcraft.MODID, "textures/item/thaumometer_glass.png");

    @Override
    public void render(GuiGraphics gg, float partialTicks, int w, int h) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return;

        var held = player.getItemInHand(InteractionHand.MAIN_HAND);
        boolean show = held != null && held.getItem().getDescriptionId().contains("thaumometer"); // naive id check
        if (!show) return;

        int size = Math.min(w,h)/3;
        int x = (w - size)/2; int y = (h - size)/2;

        RenderSystem.enableBlend();
        gg.blit(FRAME, x, y, 0,0, size,size, size,size);
        gg.blit(GLASS, x, y, 0,0, size,size, size,size);

        var hit = mc.hitResult;
        if (hit != null) {
            String name = hit.getType().toString();
            gg.drawCenteredString(mc.font, name, w/2, y + size + 8, 0xFFFFFF);
        }
    }

    @Mod.EventBusSubscriber(modid = Thaumcraft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
    public static class Registrar {
        @SubscribeEvent
        public static void onRegister(RegisterGuiOverlaysEvent e){
            e.registerAboveAll("thaumometer", new ThaumometerOverlay());
        }
    }
}
