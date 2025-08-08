
package thaumcraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.Thaumcraft;

import java.util.*;
import java.util.function.Function;

/** v2: шестиугольные узлы, кривые Безье, простой левый drawer. */
public class ThaumonomiconScreen extends Screen {

    private static final ResourceLocation BG1 = new ResourceLocation(Thaumcraft.MODID, "textures/gui/research_bg_fundamentals.png");
    private static final ResourceLocation BG2 = new ResourceLocation(Thaumcraft.MODID, "textures/gui/research_bg_dark.png");

    private static class Node {
        final float x, y;
        final String id;
        final Component label;
        Node(String id, float x, float y, String title){ this.id=id; this.x=x; this.y=y; this.label = Component.literal(title); }
    }
    private static class Edge { final int a,b; Edge(int a,int b){ this.a=a; this.b=b; } }

    private final List<Node> nodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private Map<Integer,Integer> distMap = Collections.emptyMap();
    private int hoverIndex = -1;

    // camera
    private float camX=0, camY=0, zoom=1f;
    private boolean dragging=false;
    private float drawer = 0f; // 0..1

    public ThaumonomiconScreen() {
        super(Component.literal("Thaumonomicon"));

        // demo graph
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
        float oldZoom = zoom;
        zoom = Mth.clamp(zoom + (float)delta*0.1f, 0.5f, 2.0f);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button==0) {
            dragging = true;
            int idx = hitNode(mouseX, mouseY);
            if (idx>=0) centerAndZoomOn(nodes.get(idx));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging=false; return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragging && button==0){
            camX -= dx/zoom;
            camY -= dy/zoom;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    private void centerAndZoomOn(Node n){
        // simple snap: move camera so node at screen center, small zoom-in
        float targetZoom = Math.min(1.15f, zoom*1.1f);
        camX = n.x - (this.width/2f)/targetZoom;
        camY = n.y - (this.height/2f)/targetZoom;
        zoom = targetZoom;
    }

    private float sx(float wx){ return (wx - camX)*zoom; }
    private float sy(float wy){ return (wy - camY)*zoom; }
    private float wx(float sx){ return sx/zoom + camX; }
    private float wy(float sy){ return sy/zoom + camY; }

    private int hitNode(double mx, double my){
        float wx = wx((float)mx);
        float wy = wy((float)my);
        for (int i=0;i<nodes.size();i++){
            Node n = nodes.get(i);
            if (pointInHex(wx, wy, n.x, n.y, 26)) return i;
        }
        return -1;
    }

    private static boolean pointInHex(float px,float py,float cx,float cy,float r){
        // convert to local coordinates rotated by 30deg so hex is flat-top
        double ang = Math.toRadians(30);
        float x = (float)((px-cx)*Math.cos(ang) + (py-cy)*Math.sin(ang));
        float y = (float)(-(px-cx)*Math.sin(ang) + (py-cy)*Math.cos(ang));
        x = Math.abs(x); y = Math.abs(y);
        return x <= r*0.5f && (Math.sqrt(3)*0.5f*r - Math.sqrt(3)*0.5f*x) >= y;
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
        computeHover(mouseX, mouseY);

        // BG
        gg.blit(BG1, 0,0, 0,0, this.width, this.height, this.width, this.height);
        gg.setColor(1,1,1,0.4f);
        gg.blit(BG2, (int)(Math.sin((minecraft.level.getGameTime()%400)/400f*6.28)*12),
                    (int)(Math.cos((minecraft.level.getGameTime()%400)/400f*6.28)*12),
                    0,0, this.width, this.height, this.width, this.height);
        gg.setColor(1,1,1,1);

        // world transform
        PoseStack ps = gg.pose();
        ps.pushPose();
        ps.translate(0,0,0);
        ps.scale(1,1,1);
        ps.translate(0,0,0);

        // draw edges (bezier approximated)
        RenderSystem.disableDepthTest();
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionColorShader);
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (Edge e : edges){
            Node a = nodes.get(e.a), b = nodes.get(e.b);
            float ax=sx(a.x), ay=sy(a.y), bx=sx(b.x), by=sy(b.y);
            // control points biased toward center between nodes
            float mx = (ax+bx)/2f, my = (ay+by)/2f;
            float dx = (bx-ax), dy = (by-ay);
            float nx = -dy, ny = dx;
            float t = 0.15f;
            float c1x = ax + dx*t + nx*0.10f;
            float c1y = ay + dy*t + ny*0.10f;
            float c2x = bx - dx*t + nx*0.10f;
            float c2y = by - dy*t + ny*0.10f;

            int da = distMap.getOrDefault(e.a, 99);
            int db = distMap.getOrDefault(e.b, 99);
            int d = Math.min(da, db);
            float alpha = d==0?1f : (d==1?0.5f:0.15f);
            int col = ((int)(alpha*255) << 24) | 0xFFD780;

            sampleBezierAsThickLine(buf, ax,ay,c1x,c1y,c2x,c2y,bx,by, 2.5f, col);
        }
        tess.end();

        // draw nodes (hex)
        for (int i=0;i<nodes.size();i++){
            Node n = nodes.get(i);
            boolean hov = (i==hoverIndex);
            float r = hov ? 28 : 26;
            int fill = hov? 0xA0FFF3C4 : 0xA01E2030;
            drawHex(gg, sx(n.x), sy(n.y), r, fill);
            gg.drawString(this.font, n.label, (int)(sx(n.x)- this.font.width(n.label)/2f), (int)(sy(n.y)-4), 0xFFFFFF, false);
        }

        ps.popPose();

        // left drawer (simplified)
        int zone = 8;
        boolean hoverEdge = mouseX < zone;
        float target = hoverEdge? 1f : 0f;
        drawer += (target - drawer) * 0.2f;
        int barW = (int)(Math.min(180, this.width*0.35f) * drawer);
        if (barW > 2){
            gg.fill(0,0, barW, this.height, 0xB0202230);
            // slots for categories
            int y = 14;
            for (int i=0;i<6;i++){
                gg.fill(8,y, barW-8, y+24, 0x40FFFFFF);
                y += 28;
            }
            gg.drawString(this.font, Component.literal("Categories"), 12, 6, 0xFFFFFF, false);
        }

        super.render(gg, mouseX, mouseY, pt);
    }

    private void drawHex(GuiGraphics gg, float cx, float cy, float r, int argb){
        Tesselator t = Tesselator.getInstance();
        BufferBuilder b = t.getBuilder();
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionColorShader);
        b.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        float a = ((argb>>24)&0xFF)/255f;
        float cr = ((argb>>16)&0xFF)/255f;
        float cg = ((argb>>8)&0xFF)/255f;
        float cb = (argb&0xFF)/255f;
        double rot = Math.toRadians(30);
        b.vertex(cx,cy,0).color(cr,cg,cb,a).endVertex();
        for (int i=0;i<=6;i++){
            double ang = rot + i * Math.PI*2/6;
            float x = cx + (float)(r*Math.cos(ang));
            float y = cy + (float)(r*Math.sin(ang));
            b.vertex(x,y,0).color(cr,cg,cb,a).endVertex();
        }
        t.end();
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
