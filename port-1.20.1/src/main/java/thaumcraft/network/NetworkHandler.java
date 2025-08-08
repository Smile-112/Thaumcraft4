package thaumcraft.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.SimpleChannel;
import thaumcraft.Thaumcraft;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static SimpleChannel CHANNEL;
    private static int index = 0;
    public static void init() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(Thaumcraft.MODID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );
        CHANNEL.registerMessage(
                id(),
                ScanRequestPacket.class,
                ScanRequestPacket::encode,
                ScanRequestPacket::decode,
                ScanRequestPacket::handle,
                NetworkDirection.PLAY_TO_SERVER
        );
    }
    private static int id() { return index++; }
    public static SimpleChannel channel() { return CHANNEL; }
}