
package thaumcraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.Thaumcraft;

import java.util.*;

public class ThaumonomiconScreen extends Screen {

    private static final ResourceLocation BG1 = new ResourceLocation(Thaumcraft.MODID, "textures/gui/research_bg_fundamentals.png");
    private static final ResourceLocation BG2 = new ResourceLocation(Thaumcraft.MODID, "textures/gui/research_bg_dark.png");

    static class Node {
        final float x, y; final String id; final Component label;
        Node(String id, float x, float y, String title){ this.id=id; this.x=x; this.y=y; this.label = Component.literal(title); }
    }
    static class Edge { final int a,b; Edge(int a,int b){ this.a=a; this.b=b; } }

    private final List<Node> nodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();

    // hover/selection
    private Map<Integer,Integer> distMap = Collections.emptyMap();
    private int hoverIndex = -1;

    // camera animation
    private float camX=0, camY=0, camZoom=1f;
    private float animCamX=0, animCamY=0, animZoom=1f;
    private int animTicks=0;

    private float drawer = 0f; // 0..1

    public ThaumonomiconScreen() {
        super(Component.literal("Thaumonomicon"));
        nodes.add(new Node("thaumonomicon", -160, -40, "Thaumonomicon"));
        nodes.add(new Node("vis",            60, -20, "Vis"));
        nodes.add(new Node("research",      160,  60, "Research"));
        nodes.add(new Node("wand_caps",     -40,  80, "Wand Caps"));
        edges.add(new Edge(0,1));
        edges.add(new Edge(1,2));
        edges.add(new Edge(0,3));
        edges.add(new Edge(3,2));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        camZoom = Mth.clamp(camZoom + (float)delta*0.1f, 0.5f, 2.0f);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button==0) {
            int idx = hitNode(mouseX, mouseY);
            if (idx>=0) smoothCenterOn(nodes.get(idx));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void smoothCenterOn(Node n){
        float targetZoom = Math.min(1.15f, camZoom*1.1f);
        animCamX = n.x - (this.width/2f)/targetZoom;
        animCamY = n.y - (this.height/2f)/targetZoom;
        animZoom = targetZoom;
        animTicks = 12; // ~200ms at 60fps
    }

    private float sx(float wx){ return (wx - camX)*camZoom; }
    private float sy(float wy){ return (wy - camY)*camZoom; }
    private float wx(float sx){ return sx/camZoom + camX; }
    private float wy(float sy){ return sy/camZoom + camY; }

    private int hitNode(double mx, double my){
        float wx = wx((float)mx), wy = wy((float)my);
        for (int i=0;i<nodes.size();i++){
            Node n = nodes.get(i);
            float r = nodeRadius(n);
            if (Mth.square(wx-n.x)+Mth.square(wy-n.y) <= r*r) return i;
        }
        return -1;
    }

    private float nodeRadius(Node n){
        int tw = this.font.width(n.label);
        return Math.max(26, tw*0.5f + 8);
    }

    private void computeHover(int mouseX, int mouseY){
        int h = hitNode(mouseX, mouseY);
        if (h != hoverIndex){
            hoverIndex = h;
            distMap = bfs(h);
        }
    }
    private Map<Integer,Integer> bfs(int start){
        if (start<0) return Collections.emptyMap();
        Map<Integer,Integer> dist = new HashMap<>();
        ArrayDeque<Integer> q = new ArrayDeque<>();
        q.add(start); dist.put(start,0);
        while(!q.isEmpty()){
            int v = q.poll();
            int dv = dist.get(v);
            for (Edge e : edges){
                int u = (e.a==v)? e.b : (e.b==v? e.a : -1);
                if (u<0) continue;
                if (!dist.containsKey(u)){
                    dist.put(u, dv+1);
                    if (dv+1<=2) q.add(u);
                }
            }
        }
        return dist;
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float pt) {
        // animate camera to target
        if (animTicks>0){
            float t = 1f - animTicks/12f;
            camX = Mth.lerp(0.25f, camX, animCamX);
            camY = Mth.lerp(0.25f, camY, animCamY);
            camZoom = Mth.lerp(0.25f, camZoom, animZoom);
            animTicks--;
        }

        computeHover(mouseX, mouseY);

        // background with visible parallax
        gg.blit(BG1, 0,0, 0,0, this.width, this.height, this.width, this.height);
        float phase = (minecraft.level.getGameTime() % 200) / 200f;
        int ox = (int)(Math.sin(phase*6.283)*18);
        int oy = (int)(Math.cos(phase*6.283)*18);
        gg.setColor(1,1,1,0.55f);
        gg.blit(BG2, ox, oy, 0,0, this.width, this.height, this.width, this.height);
        gg.setColor(1,1,1,1);

        // edges (bezier), connecting to label radius
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionColorShader);
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float dash = (minecraft.level.getGameTime()%40)/40f; // 0..1 anim
        for (Edge e : edges){
            Node a = nodes.get(e.a), b = nodes.get(e.b);
            float ax = sx(a.x), ay = sy(a.y);
            float bx = sx(b.x), by = sy(b.y);
            // direction and radius offsets
            float dax = bx-ax, day = by-ay;
            float len = Mth.sqrt(dax*dax+day*day);
            if (len < 1e-3) continue;
            float ux = dax/len, uy = day/len;
            float ar = nodeRadius(a)*camZoom; // scale radii by zoom -> screen px
            float br = nodeRadius(b)*camZoom;
            ax += ux*ar; ay += uy*ar;
            bx -= ux*br; by -= uy*br;

            // control points for bezier
            float mx = (ax+bx)/2f, my=(ay+by)/2f;
            float nx = -(by-ay), ny = (bx-ax);
            float c1x = ax + (bx-ax)*0.2f + nx*0.08f;
            float c1y = ay + (by-ay)*0.2f + ny*0.08f;
            float c2x = bx - (bx-ax)*0.2f + nx*0.08f;
            float c2y = by - (by-ay)*0.2f + ny*0.08f;

            int d = Math.min(distMap.getOrDefault(e.a, 99), distMap.getOrDefault(e.b, 99));
            float alpha = d==0?1f : (d==1?0.55f:0.18f);
            int col = ((int)(alpha*255)<<24) | 0xFFD780;

            sampleBezierAsThickLine(buf, ax,ay,c1x,c1y,c2x,c2y,bx,by, 2.6f, col);
        }
        tess.end();

        // nodes: только текст и мягкая подложка
        for (int i=0;i<nodes.size();i++){
            Node n = nodes.get(i);
            int tw = this.font.width(n.label);
            int th = this.font.lineHeight;
            int x = (int)sx(n.x) - tw/2 - 6;
            int y = (int)sy(n.y) - th/2 - 4;
            int w = tw + 12, h = th + 8;
            int alpha = (i==hoverIndex)? 0x80FFFFFF : 0x40FFFFFF;
            gg.fill(x, y, x+w, y+h, (alpha<<24) | 0x202230);
            gg.drawString(this.font, n.label, x + 6, y + 4, 0xFFFFFF, false);
        }

        // left drawer: open if mouse at left edge OR inside the drawer region
        int maxW = Math.min(180, (int)(this.width*0.35f));
        int currentW = (int)(maxW * drawer);
        boolean wantOpen = (mouseX < 8) || (mouseX < currentW);
        float target = wantOpen ? 1f : 0f;
        drawer += (target - drawer) * 0.25f;
        currentW = (int)(maxW * drawer);

        if (currentW > 2){
            gg.fill(0,0, currentW, this.height, 0xB0202230);
            int y = 20;
            gg.drawString(this.font, Component.literal("Categories"), 12, 6, 0xFFFFFF, false);
            for (int i=0;i<4;i++){
                int yy = y + i*28;
                gg.fill(8, yy, currentW-8, yy+24, 0x40FFFFFF);
            }
        }

        super.render(gg, mouseX, mouseY, pt);
    }

    private void sampleBezierAsThickLine(BufferBuilder buf, float ax,float ay,float c1x,float c1y,float c2x,float c2y,float bx,float by, float thickness, int argb){
        int steps = 24;
        float prevx=ax, prevy=ay;
        for (int i=1;i<=steps;i++){
            float t = i/(float)steps;
            float x = bez(ax,c1x,c2x,bx,t);
            float y = bez(ay,c1y,c2y,by,t);
            drawSegment(buf, prevx,prevy,x,y, thickness, argb);
            prevx=x; prevy=y;
        }
    }
    private void drawSegment(BufferBuilder buf, float x1,float y1,float x2,float y2, float th, int argb){
        float dx=x2-x1, dy=y2-y1;
        float len = Mth.sqrt(dx*dx+dy*dy);
        if (len < 0.001f) return;
        dx/=len; dy/=len;
        float nx=-dy*th*0.5f, ny=dx*th*0.5f;

        float a = ((argb>>24)&0xFF)/255f;
        float r = ((argb>>16)&0xFF)/255f;
        float g = ((argb>>8)&0xFF)/255f;
        float b = (argb&0xFF)/255f;

        buf.vertex(x1-nx, y1-ny, 0).color(r,g,b,a).endVertex();
        buf.vertex(x1+nx, y1+ny, 0).color(r,g,b,a).endVertex();
        buf.vertex(x2+nx, y2+ny, 0).color(r,g,b,a).endVertex();
        buf.vertex(x2-nx, y2-ny, 0).color(r,g,b,a).endVertex();
    }
    private float bez(float a,float b,float c,float d,float t){
        float it=1-t;
        return it*it*it*a + 3*it*it*t*b + 3*it*t*t*c + t*t*t*d;
    }
}
