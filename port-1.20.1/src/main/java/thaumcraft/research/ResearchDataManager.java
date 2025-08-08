package thaumcraft.research;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import java.util.*;

public class ResearchDataManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final Map<String, ResearchEntry> ENTRIES = new HashMap<>();
    public ResearchDataManager() { super(GSON, "research"); }
    @Override protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager rm, ProfilerFiller profiler) {
        ENTRIES.clear();
        for (var e : map.entrySet()) {
            JsonObject obj = e.getValue().getAsJsonObject();
            String id = e.getKey().getPath();
            String title = obj.get("title").getAsString();
            int x = obj.get("x").getAsInt(); int y = obj.get("y").getAsInt();
            List<String> deps = new ArrayList<>();
            if (obj.has("deps")) for (var el : obj.getAsJsonArray("deps")) deps.add(el.getAsString());
            boolean unlocked = obj.has("unlocked") && obj.get("unlocked").getAsBoolean();
            ENTRIES.put(id, new ResearchEntry(id, title, x, y, deps, unlocked));
        }
    }
    public static List<ResearchEntry> getEntries(){ return new ArrayList<>(ENTRIES.values()); }
    public static ResearchEntry getById(String id){ return ENTRIES.get(id); }
}