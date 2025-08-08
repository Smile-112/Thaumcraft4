package thaumcraft.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;
import thaumcraft.Thaumcraft;

public class NetworkHandler {
    private static SimpleChannel CHANNEL;
    private static int index = 0;
    public static void init() {
        CHANNEL = ChannelBuilder.named(new ResourceLocation(Thaumcraft.MODID, "main"))
                .networkProtocolVersion(1)
                .clientAcceptedVersions(s->true)
                .serverAcceptedVersions(s->true)
                .simpleChannel();
        CHANNEL.messageBuilder(ScanRequestPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ScanRequestPacket::decode)
                .encoder(ScanRequestPacket::encode)
                .consumerMainThread(ScanRequestPacket::handle)
                .add();
    }
    private static int id() { return index++; }
    public static SimpleChannel channel() { return CHANNEL; }
}