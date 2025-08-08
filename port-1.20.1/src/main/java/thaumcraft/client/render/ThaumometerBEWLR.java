
package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;

/** Renders thaumometer as a textured quad (uses original 16x16 icon with full alpha). */
public class ThaumometerBEWLR extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation TEX = new ResourceLocation(Thaumcraft.MODID, "textures/item/thaumometer.png");

    public ThaumometerBEWLR() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        pose.pushPose();
        // orient: face camera in GUI; slight tilt in hand handled by transforms
        float s = 1.0f;
        pose.scale(s, s, s);

        VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucent(TEX));
        // draw unit quad [-0.5..0.5] with UV 0..1
        float x1=-0.5f, y1=-0.5f, x2=0.5f, y2=0.5f, z=0.0f;
        quad(vc, pose, x1,y1,z, 0,1, 1,1, 1,0, 0,0, light, overlay);
        pose.popPose();
    }

    private static void quad(VertexConsumer vc, PoseStack ps,
                             float x1, float y1, float z,
                             float u1, float v1, float u2, float v2, float u3, float v3, float u4, float v4,
                             int light, int overlay) {
        PoseStack.Pose p = ps.last();
        vc.vertex(p.pose(), x1, y1, z).color(1,1,1,1).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(p.normal(), 0,0,1).endVertex();
        vc.vertex(p.pose(), x2(x1), y1, z).color(1,1,1,1).uv(u2, v2).overlayCoords(overlay).uv2(light).normal(p.normal(), 0,0,1).endVertex();
        vc.vertex(p.pose(), x2(x1), y2(y1), z).color(1,1,1,1).uv(u3, v3).overlayCoords(overlay).uv2(light).normal(p.normal(), 0,0,1).endVertex();
        vc.vertex(p.pose(), x1, y2(y1), z).color(1,1,1,1).uv(u4, v4).overlayCoords(overlay).uv2(light).normal(p.normal(), 0,0,1).endVertex();
    }
    private static float x2(float x1){ return -x1; }
    private static float y2(float y1){ return -y1; }
}
