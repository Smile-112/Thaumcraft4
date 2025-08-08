package thaumcraft.world.aura;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import javax.annotation.Nonnull; import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;

public class AuraProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final ResourceLocation KEY = new ResourceLocation(Thaumcraft.MODID, "aura");
    public static final Capability<IAura> AURA = CapabilityManager.get(new CapabilityToken<>(){});
    private final AuraData backend = new AuraData();
    private final LazyOptional<IAura> optional = LazyOptional.of(() -> backend);
    @Nonnull @Override public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) { return cap == AURA ? optional.cast() : LazyOptional.empty(); }
    @Override public CompoundTag serializeNBT(){ return backend.save(); }
    @Override public void deserializeNBT(CompoundTag nbt){ backend.load(nbt); }
}