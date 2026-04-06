package cz.maxtechnik.dif.util;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
public enum CameraMonitorState implements StringRepresentable{
    NO_SIGNAL("no_signal"),
    INACTIVE("inactive"),
    ACTIVE("active");
    private final String name;
    CameraMonitorState(String name){this.name=name;}
    @Override public @NotNull String getSerializedName(){return this.name;}
}