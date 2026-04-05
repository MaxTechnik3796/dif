package cz.maxtechnik.dif.util;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
public enum MonitorState implements StringRepresentable {
    NO_SIGNAL("no_signal"),   // Černá/zrnění - není nalinkovaná kamera
    INACTIVE("inactive"),     // Má link, ale nikdo se nedívá
    ACTIVE("active");         // Právě teď se někdo dívá

    private final String name;
    MonitorState(String name) { this.name = name; }
    @Override public @NotNull String getSerializedName() { return this.name; }
}