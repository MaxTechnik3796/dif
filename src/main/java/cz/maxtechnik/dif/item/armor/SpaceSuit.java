package cz.maxtechnik.dif.item.armor;
import cz.maxtechnik.dif.model.ModelSpaceBoots;
import cz.maxtechnik.dif.model.ModelSpaceChestplate;
import cz.maxtechnik.dif.model.ModelSpaceHelmet;
import cz.maxtechnik.dif.model.ModelSpaceLeggings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

public abstract class SpaceSuit extends ArmorItem {
	public SpaceSuit(Type type, Properties properties) {
		super(DifModTiers.ARMOR_MATERIAL_SPACE, type, properties);
	}
	public static class Helmet extends SpaceSuit{
		public Helmet(){
			super(Type.HELMET,new Properties());
		}
		@Override
		public void initializeClient(Consumer<IClientItemExtensions> consumer){
			consumer.accept(new IClientItemExtensions(){
				@Override
				public @NotNull HumanoidModel getHumanoidArmorModel(LivingEntity living,ItemStack stack,EquipmentSlot slot,HumanoidModel defaultModel){
					HumanoidModel armorModel=new HumanoidModel(new ModelPart(Collections.emptyList(),
							Map.of("head",new ModelSpaceHelmet(Minecraft.getInstance().getEntityModels().bakeLayer(ModelSpaceHelmet.LAYER_LOCATION)).Head,
									"hat",new ModelPart(Collections.emptyList(),Collections.emptyMap()),
									"body",new ModelPart(Collections.emptyList(),Collections.emptyMap()),
									"right_arm",new ModelPart(Collections.emptyList(),Collections.emptyMap()),
									"left_arm",new ModelPart(Collections.emptyList(),Collections.emptyMap()),
									"right_leg",new ModelPart(Collections.emptyList(),Collections.emptyMap()),
									"left_leg",new ModelPart(Collections.emptyList(),Collections.emptyMap()))));
					armorModel.crouching=living.isShiftKeyDown();
					armorModel.riding=defaultModel.riding;
					armorModel.young=living.isBaby();
					return armorModel;
				}
			});
		}
		@Override
		public String getArmorTexture(ItemStack stack,Entity entity,EquipmentSlot slot,String type){
			return texture;
		}
	}
	public static class Chestplate extends SpaceSuit{
		public Chestplate(){
			super(Type.CHESTPLATE,new Properties());
		}
		@Override
		public void initializeClient(Consumer<IClientItemExtensions> consumer){
			consumer.accept(new IClientItemExtensions(){
				@Override
				@OnlyIn(Dist.CLIENT)
				public @NotNull HumanoidModel getHumanoidArmorModel(LivingEntity living,ItemStack stack,EquipmentSlot slot,HumanoidModel defaultModel){
					HumanoidModel armorModel=new HumanoidModel(new ModelPart(Collections.emptyList(),
							Map.of("body",new ModelSpaceChestplate(Minecraft.getInstance().getEntityModels().bakeLayer(ModelSpaceChestplate.LAYER_LOCATION)).Body,
									"left_arm",new ModelSpaceChestplate(Minecraft.getInstance().getEntityModels().bakeLayer(ModelSpaceChestplate.LAYER_LOCATION)).LeftArm,
									"right_arm",new ModelSpaceChestplate(Minecraft.getInstance().getEntityModels().bakeLayer(ModelSpaceChestplate.LAYER_LOCATION)).RightArm,
									"head",new ModelPart(Collections.emptyList(),Collections.emptyMap()),
									"hat",new ModelPart(Collections.emptyList(),Collections.emptyMap()),
									"right_leg",new ModelPart(Collections.emptyList(),Collections.emptyMap()),
									"left_leg",new ModelPart(Collections.emptyList(),Collections.emptyMap()))));
					armorModel.crouching=living.isShiftKeyDown();
					armorModel.riding=defaultModel.riding;
					armorModel.young=living.isBaby();
					return armorModel;
				}
			});
		}
		@Override
		public String getArmorTexture(ItemStack stack,Entity entity,EquipmentSlot slot,String type){
			return texture;
		}
	}
	public static class Leggings extends SpaceSuit{
		public Leggings(){
			super(Type.LEGGINGS,new Properties());
		}
		@Override
		public void initializeClient(Consumer<IClientItemExtensions> consumer){
			consumer.accept(new IClientItemExtensions(){
				@Override
				@OnlyIn(Dist.CLIENT)
				public @NotNull HumanoidModel getHumanoidArmorModel(LivingEntity living,ItemStack stack,EquipmentSlot slot,HumanoidModel defaultModel){
					HumanoidModel armorModel=new HumanoidModel(new ModelPart(Collections.emptyList(),
							Map.of("left_leg",new ModelSpaceLeggings(Minecraft.getInstance().getEntityModels().bakeLayer(ModelSpaceLeggings.LAYER_LOCATION)).LeftLeg,
									"right_leg",new ModelSpaceLeggings(Minecraft.getInstance().getEntityModels().bakeLayer(ModelSpaceLeggings.LAYER_LOCATION)).RightLeg,
									"head",new ModelPart(Collections.emptyList(),Collections.emptyMap()),
									"hat",new ModelPart(Collections.emptyList(),Collections.emptyMap()),"body",new ModelPart(Collections.emptyList(),Collections.emptyMap()),"right_arm",new ModelPart(Collections.emptyList(),Collections.emptyMap()),
									"left_arm",new ModelPart(Collections.emptyList(),Collections.emptyMap()))));
					armorModel.crouching=living.isShiftKeyDown();
					armorModel.riding=defaultModel.riding;
					armorModel.young=living.isBaby();
					return armorModel;
				}
			});
		}
		@Override
		public String getArmorTexture(ItemStack stack,Entity entity,EquipmentSlot slot,String type){
			return texture;
		}
	}
	public static class Boots extends SpaceSuit{
		public Boots(){
			super(Type.BOOTS,new Properties());
		}
		@Override
		public void initializeClient(Consumer<IClientItemExtensions> consumer){
			consumer.accept(new IClientItemExtensions(){
				@Override
				@OnlyIn(Dist.CLIENT)
				public @NotNull HumanoidModel getHumanoidArmorModel(LivingEntity living,ItemStack stack,EquipmentSlot slot,HumanoidModel defaultModel){
					HumanoidModel armorModel=new HumanoidModel(new ModelPart(Collections.emptyList(),
							Map.of("left_leg",new ModelSpaceBoots(Minecraft.getInstance().getEntityModels().bakeLayer(ModelSpaceBoots.LAYER_LOCATION)).LeftLeg,
									"right_leg",new ModelSpaceBoots(Minecraft.getInstance().getEntityModels().bakeLayer(ModelSpaceBoots.LAYER_LOCATION)).RightLeg,
									"head",new ModelPart(Collections.emptyList(),Collections.emptyMap()),
									"hat",new ModelPart(Collections.emptyList(),Collections.emptyMap()),
									"body",new ModelPart(Collections.emptyList(),Collections.emptyMap()),
									"right_arm",new ModelPart(Collections.emptyList(),Collections.emptyMap()),
									"left_arm",new ModelPart(Collections.emptyList(),Collections.emptyMap()))));
					armorModel.crouching=living.isShiftKeyDown();
					armorModel.riding=defaultModel.riding;
					armorModel.young=living.isBaby();
					return armorModel;
				}
			});
		}
	}
}
