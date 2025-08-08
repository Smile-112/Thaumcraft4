package thaumcraft.compat;

// Curios dependency is optional. The Curios API changed in recent versions, so
// we avoid referencing the old API directly here. When adding custom Curios slot
// types, use CuriosApi.getSlotHelper().registerSlotType(...) with SlotTypeMessage
// as appropriate for the installed Curios version.

public class CuriosCompat {
    public static void registerSlots() {
        // No slot types are registered in this port. Custom slots will need to be
        // registered via CuriosApi's new API when Curios is present.
    }
}
