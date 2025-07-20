package com.azanor.thaumcraft.item;

import com.azanor.thaumcraft.Thaumcraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;

/**
 * Basic implementation of the Thaumonomicon item.
 * <p>
 * This is a minimal port of {@code thaumcraft.common.items.relics.ItemThaumonomicon}
 * from the original 1.7.10 code base. Most of the original functionality such as
 * research unlocking and GUI interaction has not yet been implemented.
 */
public class ThaumonomiconItem extends Item {
    public ThaumonomiconItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            // TODO: open Thaumonomicon GUI when implemented
            player.displayClientMessage(Component.literal("Thaumonomicon used"), true);
        }
        Thaumcraft.LOGGER.debug("Thaumonomicon used by {}", player.getName().getString());
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
