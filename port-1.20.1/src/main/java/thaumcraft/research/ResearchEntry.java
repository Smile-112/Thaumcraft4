package thaumcraft.research;
import java.util.List;
public record ResearchEntry(String id, String title, int x, int y, List<String> dependencies, boolean unlocked) {}