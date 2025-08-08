package thaumcraft.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import thaumcraft.Thaumcraft;

public class NetworkHandler {
    public static final String PROTOCOL_VERSION = "1";

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(Thaumcraft.MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );
    private static int id = 0;

    public static void init() {
        CHANNEL.registerMessage(
            id++,
            ScanRequestPacket.class,
            ScanRequestPacket::encode,
            ScanRequestPacket::decode,
            ScanRequestPacket::handle
        );
    }

    public static SimpleChannel channel() { return CHANNEL; }
}
