package cz.maxtechnik.dif.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JadernejSutr extends Item {
    public JadernejSutr(){
        super(new Properties());
    }
	@Override
	public void appendHoverText(@NotNull ItemStack itemstack,Level level,@NotNull List<Component> list,@NotNull TooltipFlag flag){
		super.appendHoverText(itemstack,level,list,flag);
		list.add(Component.literal("Co čumíš?"));
	}
}
