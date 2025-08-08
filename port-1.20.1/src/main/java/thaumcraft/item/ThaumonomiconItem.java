package thaumcraft.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thaumcraft.client.screen.ThaumonomiconScreen;

public class ThaumonomiconItem extends Item {
    public ThaumonomiconItem(Properties props) { super(props); }
    @OnlyIn(Dist.CLIENT) private void openBook() { net.minecraft.client.Minecraft.getInstance().setScreen(new ThaumonomiconScreen()); }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) openBook();
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}