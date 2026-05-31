package cz.maxtechnik.dif.init.events.client;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.other.DifModComponents;
import cz.maxtechnik.dif.item.modular.v2.ModularTool;
import cz.maxtechnik.dif.item.modular.v2.ModularToolProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu; // OPRAVENÝ IMPORT
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@SuppressWarnings({"removal","deprecation"})
//@EventBusSubscriber(modid=DifMod.MODID, bus=EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public class ModularToolClientHandler{
	// 1. ZAREGISTRUJEME ŠABLONY DO PAMĚTI
	//@SubscribeEvent
	public static void registerAdditionalModels(ModelEvent.RegisterAdditional event){
		String[] tools={"pickaxe","axe","sword","shovel","hoe"};
		for(String tool: tools){
			event.register(new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"item/template_"+tool),"inventory"));
		}
	}
	// 2. DYNAMICKÉ PŘEMAPOVÁNÍ TEXTUR ZA BĚHU HRY (MODIFY BAKING RESULT)
	//@SubscribeEvent
	public static void modifyBakingResult(ModelEvent.ModifyBakingResult event){
		// FIX: Namísto .id() nebo .getId() vytvoříme ResourceLocation přímo z tvého MODID a jména registru.
		// Tohle funguje vždycky a na všech verzích NeoForge bez ohledu na to, jak zrovna přejmenovali metody v DeferredItem!
		ModelResourceLocation mainKey=new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"modular_tool"),"inventory");
		BakedModel originalModel=event.getModels().get(mainKey);
		// Vytáhneme si upečené šablony z registru přes event.getModels()
		Map<String,BakedModel> templates=new java.util.HashMap<>();
		String[] tools={"pickaxe","axe","sword","shovel","hoe"};
		for(String tool: tools){
			BakedModel model=event.getModels().get(
					new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"item/template_"+tool),"inventory"));
			if(model!=null) templates.put(tool,model);
		}
		// Získáme referenční dummy sprity pro porovnávání vrstev
		var atlas=Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
		TextureAtlasSprite dummyHandle=atlas.apply(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"item/modular/dummy_handle"));
		TextureAtlasSprite dummyBinding=atlas.apply(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"item/modular/dummy_binding"));
		TextureAtlasSprite dummyHead=atlas.apply(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"item/modular/dummy_head"));
		// Vstříkneme náš upravený kód namísto základní fialové kostky
		if(originalModel!=null){
			event.getModels().put(mainKey,
					new DynamicModularBakedModel(originalModel,templates,dummyHandle,dummyBinding,dummyHead));
		}
	}
	// ====================================================================
	// CUSTOM IMPLEMENTACE DYNAMICKÉHO MODELU
	// ====================================================================
	public static class DynamicModularBakedModel implements BakedModel{
		private final BakedModel original;
		private final Map<String,BakedModel> templates;
		private final TextureAtlasSprite dummyHandle;
		private final TextureAtlasSprite dummyBinding;
		private final TextureAtlasSprite dummyHead;
		private final ItemOverrides overrides;
		public DynamicModularBakedModel(BakedModel original,Map<String,BakedModel> templates,TextureAtlasSprite dHandle,TextureAtlasSprite dBinding,TextureAtlasSprite dHead){
			this.original=original;
			this.templates=templates;
			this.dummyHandle=dHandle;
			this.dummyBinding=dBinding;
			this.dummyHead=dHead;
			this.overrides=new ModularItemOverrides(this);
		}
		@Override
		public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state,@Nullable Direction side,@NotNull RandomSource rand){
			return original.getQuads(state,side,rand);
		}
		@Override
		public boolean useAmbientOcclusion(){
			return original.useAmbientOcclusion();
		}
		@Override
		public boolean isGui3d(){
			return original.isGui3d();
		}
		@Override
		public boolean usesBlockLight(){
			return original.usesBlockLight();
		}
		@Override
		public boolean isCustomRenderer(){
			return original.isCustomRenderer();
		}
		@Override
		public @NotNull TextureAtlasSprite getParticleIcon(){
			return original.getParticleIcon();
		}
		@Override
		public @NotNull ItemOverrides getOverrides(){
			return overrides;
		}
	}
	// ====================================================================
	// CHYTRÝ OVERRIDES RECALCULATOR S CACHEM
	// ====================================================================
	public static class ModularItemOverrides extends ItemOverrides{
		private final DynamicModularBakedModel parent;
		private final Map<String,BakedModel> cachedModels=new ConcurrentHashMap<>();
		public ModularItemOverrides(DynamicModularBakedModel parent){
			this.parent=parent;
		}
		@Override
		public BakedModel resolve(@NotNull BakedModel model,@NotNull ItemStack stack,@Nullable ClientLevel level,@Nullable LivingEntity entity,int seed){
			ModularToolProperties props=stack.get(DifModComponents.MODULAR_PROPERTIES.get());
			if(props==null||props.toolType().equals("none")) return model;
			String type=props.toolType().toLowerCase(Locale.ROOT);
			String headMat=props.headMaterial().toLowerCase(Locale.ROOT);
			String bindingMat=props.bindingMaterial().toLowerCase(Locale.ROOT);
			String handleMat=props.handleMaterial().toLowerCase(Locale.ROOT);
			// FIX: Upraveno na efektivně finální jednořádkový zápis, aby neodmlouvala lambda níže
			final boolean broken=stack.getItem() instanceof ModularTool tool&&tool.isBroken(stack);
			// Klíč do cache: zabrání generování quadů každý frame (obrovská úspora výkonu)
			String cacheKey=type+"_"+headMat+"_"+bindingMat+"_"+handleMat+"_"+broken;
			return cachedModels.computeIfAbsent(cacheKey,k->buildModel(type,headMat,bindingMat,handleMat,broken));
		}
		private BakedModel buildModel(String type,String headMat,String bindingMat,String handleMat,boolean broken){
			BakedModel template=parent.templates.get(type);
			if(template==null) return parent.original;
			var atlas=Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
			// Vytáhneme reálné textury z tvé nové složkové struktury
			TextureAtlasSprite realHandle=atlas.apply(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"item/modular/tools/"+type+"/"+handleMat+"_handle"));
			TextureAtlasSprite realBinding=atlas.apply(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"item/modular/tools/"+type+"/"+bindingMat+"_binding"));
			String headPart=broken?"head_broken":"head";
			TextureAtlasSprite realHead=atlas.apply(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"item/modular/tools/"+type+"/"+headMat+"_"+headPart));
			List<BakedQuad> originalQuads=template.getQuads(null,null,RandomSource.create(42));
			List<BakedQuad> newQuads=new java.util.ArrayList<>();
			// PROCHÁZÍME 3D VRSTVY A PŘEMAPOVÁVÁME UV SOUŘADNICE
			for(BakedQuad quad: originalQuads){
				TextureAtlasSprite source=quad.getSprite();
				TextureAtlasSprite target=null;
				// FIX: Změněno .getLocation() na moderní .contents().name() kompatibilní s 1.21.1
				if(source.contents().name().equals(parent.dummyHandle.contents().name())) target=realHandle;
				else if(source.contents().name().equals(parent.dummyBinding.contents().name())) target=realBinding;
				else if(source.contents().name().equals(parent.dummyHead.contents().name())) target=realHead;
				if(target==null){
					newQuads.add(quad);
					continue;
				}
				// Přepočet UV vrcholů z dummy textury na reálný materiálový soubor
				int[] vertices=quad.getVertices().clone();
				for(int i=0;i<4;i++){
					int offset=i*8;
					float u=Float.intBitsToFloat(vertices[offset+4]);
					float v=Float.intBitsToFloat(vertices[offset+5]);
					float relU=(u-source.getU0())/(source.getU1()-source.getU0());
					float relV=(v-source.getV0())/(source.getV1()-source.getV0());
					float newU=target.getU0()+relU*(target.getU1()-target.getU0());
					float newV=target.getV0()+relV*(target.getV1()-target.getV0());
					vertices[offset+4]=Float.floatToRawIntBits(newU);
					vertices[offset+5]=Float.floatToRawIntBits(newV);
				}
				newQuads.add(new BakedQuad(vertices,quad.getTintIndex(),quad.getDirection(),target,quad.isShade()));
			}
			// Vrátíme hotový upečený model poskládaný z tvých textur
			return new CustomSimpleModel(newQuads,template);
		}
	}
	// Pomocný obal pro quady
	private record CustomSimpleModel(List<BakedQuad> quads,BakedModel parent) implements BakedModel{
		@Override
		public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state,@Nullable Direction side,@NotNull RandomSource rand){
			return side==null?quads:Collections.emptyList();
		}
		@Override
		public boolean useAmbientOcclusion(){
			return parent.useAmbientOcclusion();
		}
		@Override
		public boolean isGui3d(){
			return parent.isGui3d();
		}
		@Override
		public boolean usesBlockLight(){
			return parent.usesBlockLight();
		}
		@Override
		public boolean isCustomRenderer(){
			return parent.isCustomRenderer();
		}
		@Override
		public @NotNull TextureAtlasSprite getParticleIcon(){
			return parent.getParticleIcon();
		}
		@Override
		public @NotNull ItemOverrides getOverrides(){
			return ItemOverrides.EMPTY;
		}
	}
}