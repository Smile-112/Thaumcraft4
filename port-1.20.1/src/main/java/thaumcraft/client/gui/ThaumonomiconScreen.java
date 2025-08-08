
package thaumcraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.Thaumcraft;

import java.util.ArrayList;
import java.util.List;

/** v1: карта с зум/пан и кликом по "узлам-заглушкам". */
public class ThaumonomiconScreen extends Screen {

    private static final ResourceLocation BG1 = new ResourceLocation(Thaumcraft.MODID, "textures/gui/research_bg_fundamentals.png");
    private static final ResourceLocation BG2 = new ResourceLocation(Thaumcraft.MODID, "textures/gui/research_bg_dark.png");

    private static class Node {
        final float x, y;
        final Component label;
        Node(float x, float y, String name){ this.x=x; this.y=y; this.label = Component.literal(name); }
    }

    private final List<Node> nodes = new ArrayList<>();
    private float camX=0, camY=0, zoom=1f;
    private boolean dragging=false;
    private double lastMx, lastMy;

    public ThaumonomiconScreen() {
        super(Component.literal("Thaumonomicon"));
        // demo nodes
        nodes.add(new Node(-120, -40, "Thaumonomicon"));
        nodes.add(new Node(  60, -20, "Vis"));
        nodes.add(new Node( 120,  60, "Research"));
        nodes.add(new Node( -40,  80, "Wand Caps"));
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        float oldZoom = zoom;
        zoom = Mth.clamp(zoom + (float)delta*0.1f, 0.5f, 2.0f);
        // cursor-centered zoom
        float wx = screenToWorldX((float)mouseX);
        float wy = screenToWorldY((float)mouseY);
        camX -= (wx - screenToWorldX((float)mouseX));
        camY -= (wy - screenToWorldY((float)mouseY));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button==0) {
            dragging = true; lastMx=mouseX; lastMy=mouseY;
            int idx = hitNode(mouseX, mouseY);
            if (idx>=0) {
                centerAndZoomOn(nodes.get(idx));
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void centerAndZoomOn(Node n){
        // quick pan to center + gentle zoom-in
        float targetZoom = Math.min(1.15f, zoom*1.1f);
        // center world coords to screen center
        camX = n.x - (this.width/2f)/targetZoom;
        camY = n.y - (this.height/2f)/targetZoom;
        zoom = targetZoom;
        // TODO v2: animate smoothly
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging=false;
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragging && button==0){
            camX -= dx/zoom;
            camY -= dy/zoom;
            lastMx=mouseX; lastMy=mouseY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    private float screenToWorldX(float sx){ return camX + sx/zoom; }
    private float screenToWorldY(float sy){ return camY + sy/zoom; }

    private int hitNode(double mx, double my){
        float wx = screenToWorldX((float)mx);
        float wy = screenToWorldY((float)my);
        for (int i=0;i<nodes.size();i++){
            Node n = nodes.get(i);
            if (Mth.square(wx-n.x)+Mth.square(wy-n.y) <= 26*26) return i; // circle hit; v2 hex
        }
        return -1;
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gg);
        PoseStack ps = gg.pose();
        int w = this.width, h = this.height;

        // background parallax
        gg.blit(BG1, 0,0, 0,0, w,h, w,h);
        gg.setColor(1,1,1,0.4f);
        gg.blit(BG2, (int)(Math.sin((minecraft.level.getGameTime()%200)/200f*6.28)*8), (int)(Math.cos((minecraft.level.getGameTime()%200)/200f*6.28)*8), 0,0, w,h, w,h);
        gg.setColor(1,1,1,1);

        ps.pushPose();
        ps.translate(w/2f, h/2f, 0);
        ps.scale(zoom, zoom, 1);
        ps.translate(-w/2f, -h/2f, 0);
        ps.translate(-camX, -camY, 0);

        // edges (simple straight lines for v1)
        RenderSystem.disableDepthTest();
        for (int i=1;i<nodes.size();i++){
            Node a = nodes.get(0), b = nodes.get(i);
            gg.fill((int)a.x, (int)a.y, (int)b.x, (int)b.y, 0x60FFFFFF);
        }

        // nodes
        for (Node n : nodes){
            int x = (int)n.x; int y=(int)n.y;
            gg.fill(x-22, y-22, x+22, y+22, 0xA01E2030); // placeholder hex -> square
            gg.drawString(this.font, n.label, x- this.font.width(n.label)/2, y-4, 0xFFFFFF, false);
        }

        ps.popPose();
        super.render(gg, mouseX, mouseY, partialTick);
    }
}
