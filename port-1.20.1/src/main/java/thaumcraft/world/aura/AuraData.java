package thaumcraft.world.aura;

import net.minecraft.nbt.CompoundTag;
public class AuraData implements IAura {
    private int vis = 0, max = 100, flux = 0;
    public int getCurrentVis(){ return vis; } public int getMaxVis(){ return max; } public int getFlux(){ return flux; }
    public void setCurrentVis(int v){ vis = Math.max(0, Math.min(v, max)); }
    public void setMaxVis(int v){ max = Math.max(1, v); vis = Math.min(vis, max); }
    public void setFlux(int v){ flux = Math.max(0, v); }
    public CompoundTag save(){ CompoundTag t = new CompoundTag(); t.putInt("Vis",vis); t.putInt("Max",max); t.putInt("Flux",flux); return t; }
    public void load(CompoundTag t){ vis=t.getInt("Vis"); max=t.getInt("Max"); flux=t.getInt("Flux"); }
}