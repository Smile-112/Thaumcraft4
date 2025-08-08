package thaumcraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;
import thaumcraft.scan.AspectDataManager;

import java.util.function.Supplier;

public class ScanRequestPacket {
    public enum Type { BLOCK, ENTITY, ITEM_SLOT }
    private final Type type;
    private final BlockPos pos;
    private final int entityId;
    private final int slotIndex;

    public ScanRequestPacket(Type type, BlockPos pos, int entityId, int slotIndex) {
        this.type = type; this.pos = pos; this.entityId = entityId; this.slotIndex = slotIndex;
    }
    public static void encode(ScanRequestPacket pkt, FriendlyByteBuf buf) {
        buf.writeEnum(pkt.type);
        buf.writeBoolean(pkt.pos != null);
        if (pkt.pos != null) buf.writeBlockPos(pkt.pos);
        buf.writeInt(pkt.entityId);
        buf.writeInt(pkt.slotIndex);
    }
    public static ScanRequestPacket decode(FriendlyByteBuf buf) {
        Type t = buf.readEnum(Type.class);
        BlockPos p = buf.readBoolean() ? buf.readBlockPos() : null;
        int e = buf.readInt();
        int s = buf.readInt();
        return new ScanRequestPacket(t, p, e, s);
    }
    public static void handle(ScanRequestPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            Level level = player.level();
            switch (pkt.type) {
                case BLOCK -> {
                    if (pkt.pos == null) return;
                    BlockState state = level.getBlockState(pkt.pos);
                    AspectDataManager.grantScanForBlock(player, state, level, pkt.pos);
                }
                case ENTITY -> {
                    var e = level.getEntity(pkt.entityId);
                    if (e != null) AspectDataManager.grantScanForEntity(player, e);
                }
                case ITEM_SLOT -> {
                    var menu = player.containerMenu;
                    if (pkt.slotIndex >= 0 && pkt.slotIndex < menu.slots.size()) {
                        var stack = menu.getSlot(pkt.slotIndex).getItem();
                        AspectDataManager.grantScanForItem(player, stack);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}