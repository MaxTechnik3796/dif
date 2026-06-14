package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static cz.maxtechnik.dif.DifMod.goggleTooltipFix;
/**
 * Společná block entity pro "cihličkové" bloky multibloku.
 * <p>
 * <b>Klíčový design — relativní offset místo absolutní pozice:</b><br>
 * Controller se neukládá jako absolutní souřadnice (X,Y,Z), ale jako
 * <em>relativní offset</em> od této cihličky (dx, dy, dz).
 * Absolutní pozice controlleru se vždy počítá jako {@code worldPosition + offset}.<br>
 * <br>
 * Díky tomu kontrapce (Create Contraption) nemůže zneužít uložená data:
 * když kontrapce přesune cihličku na jiné místo, {@code worldPosition} se změní,
 * ale offset zůstane stejný → vypočítaná absolutní pozice controlleru bude jiná
 * a na té nové pozici žádný controller nebude → reference se automaticky zahodí.
 */
public abstract class AbstractMultiblockBrickBlockEntity extends BlockEntity implements IHaveGoggleInformation{
	/**
	 * Relativní offset od této cihličky k controlleru, nebo {@code null} pokud
	 * cihlička není součástí žádné zformované struktury.
	 */
	@Nullable
	private BlockPos controllerOffset=null;
	protected AbstractMultiblockBrickBlockEntity(BlockEntityType<?> type,BlockPos pos,BlockState blockState){
		super(type,pos,blockState);
	}
	// ── Controller reference ─────────────────────────────────────────────────
	/**
	 * Vrátí absolutní pozici controlleru vypočítanou z aktuální pozice cihličky
	 * a uloženého offsetu, nebo {@code null} pokud cihlička nemá controller.
	 */
	public @Nullable BlockPos getControllerPos(){
		if(controllerOffset==null) return null;
		return worldPosition.offset(controllerOffset);
	}
	/**
	 * Nastaví controller — ukládá se pouze offset, nikoli absolutní souřadnice.
	 *
	 * @param absoluteControllerPos absolutní pozice controlleru, nebo {@code null} pro uvolnění
	 */
	public void setControllerPos(@Nullable BlockPos absoluteControllerPos){
		BlockPos newOffset=absoluteControllerPos==null
				?null
				:absoluteControllerPos.subtract(worldPosition);
		if(posEquals(controllerOffset,newOffset)) return;
		this.controllerOffset=newOffset;
		setChanged();
		if(level!=null&&!level.isClientSide){
			level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
		}
	}
	/**
	 * Vrátí zformovaný controller na dané <em>absolutní</em> pozici, nebo {@code null}.
	 * <p>
	 * Implementace typicky:
	 * <pre>{@code
	 * if (level == null) return null;
	 * var state = level.getBlockState(pos);
	 * if (state.getValue(MyController.FORMED)
	 *         && level.getBlockEntity(pos) instanceof MyControllerBE ctrl) return ctrl;
	 * return null;
	 * }</pre>
	 */
	protected abstract @Nullable AbstractMultiblockControllerBlockEntity<?> resolveController(BlockPos absolutePos);
	/**
	 * Vrátí controller tohoto bloku, pokud je struktura aktuálně zformovaná.
	 * Absolutní pozice se počítá z offsetu a aktuální {@code worldPosition}.
	 */
	public @Nullable AbstractMultiblockControllerBlockEntity<?> getController(){
		BlockPos abs=getControllerPos();
		if(abs==null) return null;
		return resolveController(abs);
	}
	/**
	 * Vrátí true, pokud tuto cihličku může získat do vlastnictví daný controller.
	 */
	public boolean canBeClaimedBy(BlockPos claimerPos){
		BlockPos currentAbs=getControllerPos();
		if(currentAbs==null||currentAbs.equals(claimerPos)) return true;
		if(level==null) return false;
		// Stará/neplatná reference → uvolni
		if(resolveController(currentAbs)==null){
			controllerOffset=null;
			setChanged();
			return true;
		}
		return false;
	}
	/**
	 * Při odebrání cihličky upozorní controller, aby převalidoval strukturu.
	 */
	public void notifyControllerRemoved(){
		if(level==null) return;
		AbstractMultiblockControllerBlockEntity<?> ctrl=getController();
		if(ctrl!=null) ctrl.forceValidation=true;
	}
	/**
	 * Voláno vždy když se block entity dostane do světa.
	 * Ověří zda offset stále ukazuje na platný controller — pokud ne, smaže ho.
	 * Toto je hlavní ochrana proti kontrapcím: po přesunutí cihličky na nové
	 * {@code worldPosition} vede offset na jinou absolutní pozici, kde žádný
	 * zformovaný controller není.
	 */
	@Override
	public void onLoad(){
		super.onLoad();
		if(level==null||level.isClientSide||controllerOffset==null) return;
		if(resolveController(worldPosition.offset(controllerOffset))==null){
			controllerOffset=null;
			setChanged();
		}
	}
	// ── Goggle tooltip ────────────────────────────────────────────────────────
	protected abstract String getGoggleDisplayName();
	protected abstract ChatFormatting getGoggleNameColor();
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip,boolean isPlayerSneaking){
		AbstractMultiblockControllerBlockEntity<?> controller=getController();
		if(controller!=null) return controller.addToGoggleTooltip(tooltip,isPlayerSneaking);
		tooltip.add(Component.literal(goggleTooltipFix+getGoggleDisplayName())
				.withStyle(getGoggleNameColor(),ChatFormatting.BOLD));
		tooltip.add(Component.literal(goggleTooltipFix+" Structure is NOT formed!")
				.withStyle(ChatFormatting.RED));
		return true;
	}
	// ── NBT ──────────────────────────────────────────────────────────────────
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.saveAdditional(tag,provider);
		if(controllerOffset!=null){
			// Ukládáme offset (dx, dy, dz), ne absolutní souřadnice
			tag.putIntArray("controllerOffset",new int[]{
					controllerOffset.getX(),
					controllerOffset.getY(),
					controllerOffset.getZ()
			});
		}
	}
	@Override
	protected void loadAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.loadAdditional(tag,provider);
		// Podpora starého formátu (absolutní "controllerPos") pro migraci světů
		if(tag.contains("controllerOffset")){
			int[] o=tag.getIntArray("controllerOffset");
			controllerOffset=(o.length==3)?new BlockPos(o[0],o[1],o[2]):null;
		}else if(tag.contains("controllerPos")){
			// Starý formát — přepočítáme na offset hned jak máme worldPosition
			// (worldPosition je nastavena před loadAdditional, takže funguje)
			int[] c=tag.getIntArray("controllerPos");
			if(c.length==3){
				BlockPos oldAbs=new BlockPos(c[0],c[1],c[2]);
				controllerOffset=oldAbs.subtract(worldPosition);
			}else{
				controllerOffset=null;
			}
		}else{
			controllerOffset=null;
		}
		// Finální validace offsetu proběhne v onLoad() kdy je level dostupný
	}
	/**
	 * Záměrně nepíšeme nic do item stacku — middle-click, loot tabulky atd.
	 * nedostanou žádná multiblock data.
	 */
	@Override
	public void saveToItem(@NotNull ItemStack stack,@NotNull HolderLookup.Provider provider){
		// záměrně prázdné
	}
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
	@Override
	public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider){
		return saveWithFullMetadata(provider);
	}
	// ── Util ─────────────────────────────────────────────────────────────────
	private static boolean posEquals(@Nullable BlockPos a,@Nullable BlockPos b){
		if(a==null&&b==null) return true;
		if(a==null||b==null) return false;
		return a.equals(b);
	}
}