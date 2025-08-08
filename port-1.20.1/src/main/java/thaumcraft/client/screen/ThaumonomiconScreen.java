package thaumcraft.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import thaumcraft.research.ResearchDataManager;
import thaumcraft.research.ResearchEntry;
import java.util.List;

public class ThaumonomiconScreen extends Screen {
    private float zoom = 1.0f; private int offsetX = 0; private int offsetY = 0;
    public ThaumonomiconScreen() { super(Component.literal("Thaumonomicon")); }
    @Override public boolean mouseScrolled(double x, double y, double delta) { zoom = (float)Math.max(0.5, Math.min(2.5, zoom + delta * 0.1)); return true; }
    @Override public boolean mouseDragged(double x, double y, int button, double dx, double dy) { if (button==0){ offsetX+=dx; offsetY+=dy; return true;} return false; }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float pt) {
        this.renderBackground(g); g.drawString(this.font, "Thaumonomicon (prototype)", 12, 12, 0xFFFFFF, false);
        List<ResearchEntry> entries = ResearchDataManager.getEntries();
        int cx = this.width/2 + offsetX, cy = this.height/2 + offsetY;
        for (ResearchEntry e : entries) for (String dep : e.dependencies()) {
            ResearchEntry d = ResearchDataManager.getById(dep); if (d==null) continue;
            int x1 = cx + (int)(e.x()*zoom), y1 = cy + (int)(e.y()*zoom), x2 = cx + (int)(d.x()*zoom), y2 = cy + (int)(d.y()*zoom);
            g.fillGradient(Math.min(x1,x2), Math.min(y1,y2), Math.max(x1,x2), Math.max(y1,y2), 0x40FFFFFF, 0x40FFFFFF);
        }
        for (ResearchEntry e : entries) {
            int x = cx + (int)(e.x()*zoom), y = cy + (int)(e.y()*zoom);
            int color = e.unlocked() ? 0xFF66FF66 : 0xFFFF6666;
            g.fill(x-4, y-4, x+4, y+4, color); g.drawString(this.font, e.title(), x+6, y-4, 0xFFFFFF, false);
        }
        super.render(g, mouseX, mouseY, pt);
    }
}