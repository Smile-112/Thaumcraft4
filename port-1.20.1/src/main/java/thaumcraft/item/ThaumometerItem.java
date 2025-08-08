package thaumcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;
import thaumcraft.network.NetworkHandler;
import thaumcraft.network.ScanRequestPacket;

public class ThaumometerItem extends Item {
    private static final int SCAN_TICKS_WORLD = 40;
    private static final int SCAN_TICKS_INVENTORY = 30;

    public ThaumometerItem(Properties props) {
        super(props);
        MinecraftForge.EVENT_BUS.register(new ClientHook());
    }

    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.SPYGLASS; }
    @Override public int getUseDuration(ItemStack stack) { return 72000; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @OnlyIn(Dist.CLIENT)
    static class ClientHook {
        private int holdTicks = 0;
        private boolean rmbDown = false;
        private int invHoldTicks = 0;
        private int hoveredSlotIndex = -1;

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player == null) return;
            var held = mc.player.getMainHandItem();
            if (!(held.getItem() instanceof ThaumometerItem)) { holdTicks = 0; return; }
            if (mc.screen == null && mc.player.isUsingItem()) {
                holdTicks++;
                if (holdTicks >= SCAN_TICKS_WORLD) {
                    var hit = mc.hitResult;
                    if (hit instanceof BlockHitResult bhr) {
                        BlockPos pos = bhr.getBlockPos();
                        NetworkHandler.channel().send(PacketDistributor.SERVER.noArg(),
                                new ScanRequestPacket(ScanRequestPacket.Type.BLOCK, pos, -1, -1));
                    } else if (hit != null && hit.getType() == HitResult.Type.ENTITY && mc.crosshairPickEntity != null) {
                        NetworkHandler.channel().send(PacketDistributor.SERVER.noArg(),
                                new ScanRequestPacket(ScanRequestPacket.Type.ENTITY, null, mc.crosshairPickEntity.getId(), -1));
                    }
                    holdTicks = 0;
                    mc.player.releaseUsingItem();
                }
            } else {
                holdTicks = 0;
            }
        }

        @SubscribeEvent
        public void onMouse(ScreenEvent.MouseButtonPressed.Pre event) {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player == null) return;
            var held = mc.player.getMainHandItem();
            if (!(held.getItem() instanceof ThaumometerItem)) return;
            if (event.getButton() == 1) rmbDown = true;
        }

        @SubscribeEvent
        public void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
            if (event.getButton() == 1) { rmbDown = false; invHoldTicks = 0; hoveredSlotIndex = -1; }
        }

        @SubscribeEvent
        public void onScreenRender(ScreenEvent.Render.Post event) {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player == null || mc.screen == null) return;
            var held = mc.player.getMainHandItem();
            if (!(held.getItem() instanceof ThaumometerItem)) return;
            if (!(mc.screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen)) return;
            if (!rmbDown) { invHoldTicks = 0; hoveredSlotIndex = -1; return; }

            // determine the slot under the mouse without using private isHovering
            int mouseX = (int) event.getMouseX();
            int mouseY = (int) event.getMouseY();
            hoveredSlotIndex = -1;
            // Attempt to use public getSlotUnderMouse() to identify the hovered slot; fall back to manual scan if null
            net.minecraft.world.inventory.Slot hovered = screen.getSlotUnderMouse();
            if (hovered != null) {
                hoveredSlotIndex = hovered.index;
            } else {
                // fallback: iterate over slots and compare mouse position against slot bounds (16x16)
                for (int i = 0; i < screen.getMenu().slots.size(); i++) {
                    var slot = screen.getMenu().slots.get(i);
                    int sx = slot.x;
                    int sy = slot.y;
                    // approximate slot size (16x16)
                    if (mouseX >= sx && mouseX < sx + 16 && mouseY >= sy && mouseY < sy + 16) {
                        hoveredSlotIndex = i;
                        break;
                    }
                }
            }

            if (hoveredSlotIndex >= 0) {
                invHoldTicks++;
                if (invHoldTicks >= SCAN_TICKS_INVENTORY) {
                    NetworkHandler.channel().send(PacketDistributor.SERVER.noArg(),
                            new ScanRequestPacket(ScanRequestPacket.Type.ITEM_SLOT, null, -1, hoveredSlotIndex));
                    invHoldTicks = 0;
                }
            } else {
                invHoldTicks = 0;
            }
        }
    }
}