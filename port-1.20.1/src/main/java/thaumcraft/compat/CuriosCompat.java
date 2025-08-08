package thaumcraft.compat;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypePreset;

public class CuriosCompat {
    public static void registerSlots() {
        CuriosApi.enqueueSlotType(SlotTypePreset.AMULET.getMessageBuilder().build());
        CuriosApi.enqueueSlotType(SlotTypePreset.RING.getMessageBuilder().size(2).build());
        CuriosApi.enqueueSlotType(SlotTypePreset.BELT.getMessageBuilder().build());
    }
}
