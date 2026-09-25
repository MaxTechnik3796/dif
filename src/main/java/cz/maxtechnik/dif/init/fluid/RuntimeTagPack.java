package cz.maxtechnik.dif.init.fluid;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
@SuppressWarnings({"removal","deprecation"})
@EventBusSubscriber(modid=DifMod.MODID, bus=EventBusSubscriber.Bus.MOD)
public class RuntimeTagPack{
	@SubscribeEvent
	public static void onAddPackFinders(AddPackFindersEvent event){
		if(event.getPackType().equals(PackType.SERVER_DATA)){
			event.addRepositorySource(consumer->{
				PackLocationInfo info=new PackLocationInfo(
						"dif_runtime",
						Component.literal("TheDifferential Auto-Tags"),
						PackSource.BUILT_IN,
						Optional.empty()
				);
				Pack.ResourcesSupplier resourcesSupplier=new Pack.ResourcesSupplier(){
					@Override
					public @NotNull PackResources openPrimary(@NotNull PackLocationInfo info){
						return new VirtualTagPackResources(info);
					}
					@Override
					public @NotNull PackResources openFull(@NotNull PackLocationInfo info,Pack.@NotNull Metadata metadata){
						return new VirtualTagPackResources(info);
					}
				};
				Pack.Metadata metadata=new Pack.Metadata(
						Component.literal("In-RAM Tags"),
						PackCompatibility.COMPATIBLE,
						FeatureFlagSet.of(),
						List.of()
				);
				consumer.accept(new Pack(info,resourcesSupplier,metadata,new PackSelectionConfig(true,Pack.Position.TOP,false)));
			});
		}
	}
	private static class VirtualTagPackResources implements PackResources{
		private final PackLocationInfo info;
		private final Map<String,String> virtualFiles=new HashMap<>();
		public VirtualTagPackResources(PackLocationInfo info){
			this.info=info;
			this.generateTags();
		}
		private void generateTags(){
			List<String> allBuckets=new ArrayList<>();
			List<String> waterFluids=new ArrayList<>();
			for(FluidEntry entry: FluidEntry.ALL_ENTRIES){
				String sourceId=DifMod.MODID+":"+entry.name;
				String flowingId=DifMod.MODID+":flowing_"+entry.name;
				String bucketId=DifMod.MODID+":"+entry.name+"_bucket";
				allBuckets.add(bucketId);
				// 1. Tag c:fluids/<name>
				addJson("data/c/tags/fluid/"+entry.name+".json",createTagJson(List.of(sourceId,flowingId)));
				// 2. Tag c:buckets/<name>
				addJson("data/c/tags/item/buckets/"+entry.name+".json",createTagJson(List.of(bucketId)));
				if(entry.isWaterLike){
					waterFluids.add(sourceId);
					waterFluids.add(flowingId);
				}
			}
			addJson("data/c/tags/item/buckets.json",createTagJson(allBuckets));
			if(!waterFluids.isEmpty()) addJson("data/minecraft/tags/fluid/water.json",createTagJson(waterFluids));
		}
		private void addJson(String path,String json){
			virtualFiles.put(path,json);
		}
		private String createTagJson(List<String> values){
			StringBuilder builder=new StringBuilder("{\"replace\":false,\"values\":[");
			for(int i=0;i<values.size();i++){
				builder.append("\"").append(values.get(i)).append("\"");
				if(i<values.size()-1) builder.append(",");
			}
			builder.append("]}");
			return builder.toString();
		}
		@Override
		public @Nullable IoSupplier<InputStream> getResource(@NotNull PackType type,@NotNull ResourceLocation location){
			if(type==PackType.SERVER_DATA){
				String path="data/"+location.getNamespace()+"/"+location.getPath();
				String content=virtualFiles.get(path);
				if(content!=null) return ()->new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
			}
			return null;
		}
		@Override
		public void listResources(@NotNull PackType type,@NotNull String namespace,@NotNull String path,@NotNull ResourceOutput output){
			if(type==PackType.SERVER_DATA){
				String prefix="data/"+namespace+"/"+path;
				for(Map.Entry<String,String> entry: virtualFiles.entrySet()){
					if(entry.getKey().startsWith(prefix)){
						String subPath=entry.getKey().substring(("data/"+namespace+"/").length());
						output.accept(ResourceLocation.fromNamespaceAndPath(namespace,subPath),()->new ByteArrayInputStream(entry.getValue().getBytes(StandardCharsets.UTF_8)));
					}
				}
			}
		}
		@Override
		public @NotNull Set<String> getNamespaces(@NotNull PackType type){
			return type.equals(PackType.SERVER_DATA)?Set.of("c","minecraft",DifMod.MODID):Collections.emptySet();
		}
		@Override
		public @NotNull PackLocationInfo location(){
			return info;
		}
		@Override
		public @Nullable IoSupplier<InputStream> getRootResource(String @NotNull ... elements){
			return null;
		}
		@Override
		public <T> @Nullable T getMetadataSection(@NotNull MetadataSectionSerializer<T> deserializer){
			return null;
		}
		@Override
		public void close(){
		}
	}
}