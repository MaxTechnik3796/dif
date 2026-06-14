package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import cz.maxtechnik.dif.util.MultiblockHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

import static cz.maxtechnik.dif.DifMod.goggleTooltipFix;
/**
 * Společná controller block entity pro 3×3×3 multiblok.
 * <p>
 * Obsahuje veškerou sdílenou logiku:
 * <ul>
 *   <li>Validaci a (de)formování struktury</li>
 *   <li>Claiming/releasing cihliček</li>
 *   <li>Item inventář (vstup + výstup)</li>
 *   <li>Zpracování receptu (progress, activate/deactivate)</li>
 *   <li>Interakci hráče (vložit/vybrat item, naplnit kbelík)</li>
 *   <li>WorldlyContainer implementaci</li>
 *   <li>NBT serializaci</li>
 * </ul>
 *
 * Typový parametr {@code R} je typ receptu, který controller zpracovává.
 */
public abstract class AbstractMultiblockControllerBlockEntity<R>
		extends RandomizableContainerBlockEntity
		implements WorldlyContainer, IHaveGoggleInformation{
	// ── Konstanty ────────────────────────────────────────────────────────────
	protected static final int FORMED_REVALIDATE_PERIOD=40;
	protected static final int UNFORMED_REVALIDATE_PERIOD=20;
	protected static final int SLOT_INPUT=0;
	protected static final int SLOT_OUTPUT=1;
	private static final int[] SLOTS_ALL={SLOT_INPUT,SLOT_OUTPUT};
	// ── Inventář ─────────────────────────────────────────────────────────────
	protected final ItemStackHandler inventory=new ItemStackHandler(2){
		@Override
		protected void onContentsChanged(int slot){
			if(slot==SLOT_INPUT) cachedRecipe=null;
			setChanged();
		}
	};
	public ItemStackHandler getInventory(){
		return inventory;
	}
	// ── Stav ─────────────────────────────────────────────────────────────────
	protected int progress=0;
	protected int totalTime=0;
	public boolean forceValidation=true;
	/** Náhodný offset aby všechny controllery netickaly validaci najednou. */
	private final int tickOffset=(int)(Math.random()*UNFORMED_REVALIDATE_PERIOD);
	protected boolean isConflicted=false;
	@Nullable
	protected transient R cachedRecipe=null;
	// ── Konstruktor ──────────────────────────────────────────────────────────
	protected AbstractMultiblockControllerBlockEntity(BlockEntityType<?> type,BlockPos pos,BlockState blockState){
		super(type,pos,blockState);
	}
	// ── Abstraktní metody, které musí implementovat podtřídy ─────────────────
	/** Vzor 3×3×3 pro validaci struktury. */
	protected abstract Predicate<BlockState>[][][] getPattern();
	/** Property FACING na block state controlleru. */
	protected abstract net.minecraft.world.level.block.state.properties.DirectionProperty getFacingProperty();
	/** Property FORMED na block state controlleru. */
	protected abstract BooleanProperty getFormedProperty();
	/** Property ACTIVE na block state controlleru. */
	protected abstract BooleanProperty getActiveProperty();
	/**
	 * Vyhledá recept pro aktuální vstup.
	 * Výsledek by měl být kešován do {@link #cachedRecipe}.
	 */
	protected abstract @Nullable R findRecipe(Level level);
	/**
	 * Zkontroluje zda lze aktuálně zapsat výstup receptu {@code recipe}
	 * (slot výstup + případné fluid tanky).
	 */
	protected abstract boolean canOutput(R recipe);
	/**
	 * Dokončí recept — spotřebuje vstupy, zapíše výstupy.
	 * Volá se když {@code progress >= totalTime}.
	 */
	protected abstract void finishRecipe(R recipe);
	/**
	 * Vrátí dobu zpracování receptu v tickách.
	 */
	protected abstract int getProcessingTime(R recipe);
	/**
	 * Vrátí {@link IFluidHandler} pro capability registraci.
	 * CokeOven vrátí jeden tank, BlastSmeltery vrátí {@code combinedInOut}.
	 * {@code side} je k dispozici pokud je potřeba sided logika (většinou se ignoruje).
	 */
	public abstract @Nullable IFluidHandler getFluidCapability(@Nullable Direction side);
	/**
	 * Vrátí true, pokud jsou vstupy k dispozici pro recept (slot/fluid).
	 * Pokud ne, controller se deaktivuje.
	 */
	protected abstract boolean hasValidInput();
	/**
	 * Název multibloku pro goggle tooltip (např. {@code "◆ Coke Oven"}).
	 */
	protected abstract Component getGoggleName();
	/**
	 * Barva/styl názvu v goggle tooltipu.
	 */
	protected abstract ChatFormatting getGoggleNameColor();
	/**
	 * Přidá do tooltipu specifické řádky (item sloty, fluid tanky) — voláno
	 * pouze když je struktura zformovaná.
	 */
	protected abstract void appendFormedTooltip(List<Component> tooltip);
	/**
	 * Zkontroluje, zda block entity na dané pozici je cihlička tohoto multibloku,
	 * a pokud ano, zavolá na ni {@link AbstractMultiblockBrickBlockEntity#canBeClaimedBy}.
	 */
	protected abstract boolean brickCanBeClaimedBy(Level level,BlockPos brickPos,BlockPos controllerPos);
	/**
	 * Pokud je block entity na {@code brickPos} cihličkou tohoto multibloku,
	 * nastaví jí controller na {@code owner} (null = uvolnit).
	 */
	protected abstract void setBrickController(Level level,BlockPos brickPos,@Nullable BlockPos owner);
	// ── Server tick (sdílená logika) ──────────────────────────────────────────
	/**
	 * Zavolej z konkrétního {@code serverTick} v podtřídě:
	 * <pre>
	 * public static void serverTick(Level level, BlockPos pos, BlockState state, MyController be) {
	 *     be.tick(level, pos, state);
	 * }
	 * </pre>
	 */
	protected void tick(Level level,BlockPos pos,BlockState blockState){
		final Direction intoStructure=blockState.getValue(getFacingProperty()).getOpposite();
		final boolean wasFormed=blockState.getValue(getFormedProperty());
		final long gameTime=level.getGameTime()+tickOffset;
		final int period=wasFormed?FORMED_REVALIDATE_PERIOD:UNFORMED_REVALIDATE_PERIOD;
		final boolean shouldValidate=forceValidation||gameTime%period==0;
		boolean isFormed=wasFormed;
		if(shouldValidate){
			forceValidation=false;
			isFormed=MultiblockHelper.isValid(level,pos,intoStructure,getPattern());
			// Nová formace — zkontroluj, zda jsou cihličky volné
			if(isFormed&&!wasFormed&&!canClaimAllBricks(level,pos,intoStructure)){
				isFormed=false;
				if(!isConflicted){
					isConflicted=true;
					setChanged();
				}
			}
		}
		if(isFormed!=wasFormed){
			if(isFormed){
				claimBricks(level,pos,intoStructure,pos);
				isConflicted=false;
			}else{
				claimBricks(level,pos,intoStructure,null);
				resetProgress();
			}
			blockState=blockState
					.setValue(getFormedProperty(),isFormed)
					.setValue(getActiveProperty(),false);
			level.setBlock(pos,blockState,3);
			setChanged();
		}
		if(!isFormed) return;
		// Vstup → recept
		if(!hasValidInput()){
			resetProgressAndDeactivate(level,pos,blockState);
			return;
		}
		final R recipe=findRecipe(level);
		if(recipe==null){
			resetProgressAndDeactivate(level,pos,blockState);
			return;
		}
		totalTime=getProcessingTime(recipe);
		if(!canOutput(recipe)){
			setActive(level,pos,blockState,false);
			return;
		}
		setActive(level,pos,blockState,true);
		progress++;
		if(progress>=totalTime){
			finishRecipe(recipe);
			progress=0;
			setChanged();
		}else if(progress%10==0){
			setChanged();
		}
	}
	// ── Brick claiming ────────────────────────────────────────────────────────
	private boolean canClaimAllBricks(Level level,BlockPos controllerPos,Direction intoStructure){
		final boolean[] ok={true};
		MultiblockHelper.forEachBrick(controllerPos,intoStructure,mp->{
			if(!brickCanBeClaimedBy(level,mp,controllerPos)){
				ok[0]=false;
				return false; // přeruš iteraci
			}
			return true;
		});
		return ok[0];
	}
	private void claimBricks(Level level,BlockPos controllerPos,Direction intoStructure,@Nullable BlockPos owner){
		MultiblockHelper.forEachBrick(controllerPos,intoStructure,mp->{
			setBrickController(level,mp,owner);
			return true;
		});
	}
	// ── Progress helpers ──────────────────────────────────────────────────────
	protected void resetProgress(){
		progress=0;
		totalTime=0;
		cachedRecipe=null;
	}
	protected void resetProgressAndDeactivate(Level level,BlockPos pos,BlockState blockState){
		if(progress!=0||totalTime!=0){
			resetProgress();
			setChanged();
		}
		setActive(level,pos,blockState,false);
	}
	protected void setActive(Level level,BlockPos pos,BlockState state,boolean active){
		if(state.getValue(getActiveProperty())!=active){
			level.setBlock(pos,state.setValue(getActiveProperty(),active),3);
		}
	}
	// ── Goggle tooltip (sdílená část) ─────────────────────────────────────────
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip,boolean isPlayerSneaking){
		tooltip.add(Component.literal(goggleTooltipFix+getGoggleName().getString())
				.withStyle(getGoggleNameColor(),ChatFormatting.BOLD));
		final BlockState state=getBlockState();
		final boolean formed=state.hasProperty(getFormedProperty())&&state.getValue(getFormedProperty());
		if(!formed){
			tooltip.add(isConflicted
					?Component.literal(goggleTooltipFix+" ⚠ Structure already uses some blocks").withStyle(ChatFormatting.DARK_RED)
					:Component.literal(goggleTooltipFix+" Structure is NOT formed!").withStyle(ChatFormatting.RED));
			return true;
		}
		appendFormedTooltip(tooltip);
		if(state.getValue(getActiveProperty())&&totalTime>0){
			int pct=(int)(((double)progress/totalTime)*100.0);
			int secsLeft=Math.max(0,(totalTime-progress)/20);
			tooltip.add(Component.literal(goggleTooltipFix+" ▶ Progress: ").withStyle(ChatFormatting.GRAY)
					.append(Component.literal(pct+"% ("+secsLeft+"s left)").withStyle(ChatFormatting.GREEN)));
		}else{
			tooltip.add(Component.literal(goggleTooltipFix+" ▶ Status: ").withStyle(ChatFormatting.GRAY)
					.append(Component.literal("Idle").withStyle(ChatFormatting.YELLOW)));
		}
		return true;
	}
	// ── Tooltip utility ───────────────────────────────────────────────────────
	protected static void appendItemSlot(List<Component> tooltip,String label,ItemStack stack){
		tooltip.add(Component.literal(label).withStyle(ChatFormatting.GRAY)
				.append(Component.literal(
						stack.isEmpty()?"Empty":stack.getCount()+"x "+stack.getHoverName().getString()
				).withStyle(stack.isEmpty()?ChatFormatting.DARK_GRAY:ChatFormatting.WHITE)));
	}
	protected static void appendFluidSlot(List<Component> tooltip,String label,FluidTank tank){
		final FluidStack fluid=tank.getFluid();
		tooltip.add(Component.literal(label).withStyle(ChatFormatting.GRAY)
				.append(fluid.isEmpty()
						?Component.literal("Empty").withStyle(ChatFormatting.DARK_GRAY)
						:Component.literal(
						fluid.getAmount()+"/"+tank.getCapacity()+" mB "+fluid.getHoverName().getString()
				).withStyle(ChatFormatting.AQUA)));
	}
	// ── Interakce hráče ───────────────────────────────────────────────────────
	/**
	 * Zpracuje klik hráče na controller.
	 * <ul>
	 *   <li>Prázdný kbelík → naplní z fluid tanku (implementace v podtřídě přes {@link #tryFillBucket})</li>
	 *   <li>Item v ruce → vloží do vstupního slotu, nebo vymění</li>
	 *   <li>Prázdná ruka → vyjme výstup, pak vstup</li>
	 * </ul>
	 */
	public boolean handleInteraction(Player player,InteractionHand hand){
		if(level==null||level.isClientSide) return true;
		final ItemStack held=player.getItemInHand(hand);
		if(held.getItem()==Items.BUCKET){
			tryFillBucket(player,hand,held);
			return true;
		}
		if(!held.isEmpty()){
			insertOrSwapInput(player,hand,held);
		}else{
			extractOutput(player,hand);
		}
		setChanged();
		return true;
	}
	/**
	 * Vloží item do vstupního slotu nebo prohodí se stávajícím obsahem.
	 * Může být přepsáno pokud má multiblok jiné chování.
	 */
	protected void insertOrSwapInput(Player player,InteractionHand hand,ItemStack held){
		final ItemStack currentInput=inventory.getStackInSlot(SLOT_INPUT);
		if(currentInput.isEmpty()||ItemStack.isSameItemSameComponents(currentInput,held)){
			player.setItemInHand(hand,inventory.insertItem(SLOT_INPUT,held.copy(),false));
		}else{
			player.setItemInHand(hand,currentInput);
			inventory.setStackInSlot(SLOT_INPUT,held.copy());
		}
	}
	/**
	 * Vyjme item z výstupního slotu, pokud je prázdný zkusí vstupní.
	 */
	protected void extractOutput(Player player,InteractionHand hand){
		ItemStack out=inventory.getStackInSlot(SLOT_OUTPUT);
		if(!out.isEmpty()){
			player.setItemInHand(hand,out.copy());
			inventory.setStackInSlot(SLOT_OUTPUT,ItemStack.EMPTY);
			return;
		}
		ItemStack in=inventory.getStackInSlot(SLOT_INPUT);
		if(!in.isEmpty()){
			player.setItemInHand(hand,in.copy());
			inventory.setStackInSlot(SLOT_INPUT,ItemStack.EMPTY);
		}
	}
	/**
	 * Naplní kbelík hráče z fluid tanku.
	 * Podtřídy přepíší tuto metodu pokud mají více tanků
	 * (např. BlastSmeltery nejdřív bere z output tanku).
	 */
	protected abstract void tryFillBucket(Player player,InteractionHand hand,ItemStack heldBucket);
	// ── WorldlyContainer ──────────────────────────────────────────────────────
	@Override
	public int @NotNull [] getSlotsForFace(@NotNull Direction side){
		return SLOTS_ALL;
	}
	@Override
	public boolean canPlaceItemThroughFace(int index,@NotNull ItemStack itemStack,@org.jetbrains.annotations.Nullable Direction side){
		return index==SLOT_INPUT;
	}
	@Override
	public boolean canTakeItemThroughFace(int index,@NotNull ItemStack itemStack,@NotNull Direction side){
		return index==SLOT_OUTPUT;
	}
	@Override
	public boolean canPlaceItem(int index,@NotNull ItemStack itemStack){
		return index==SLOT_INPUT;
	}
	@Override
	public int getContainerSize(){
		return inventory.getSlots();
	}
	@Override
	public @NotNull ItemStack getItem(int index){
		return inventory.getStackInSlot(index);
	}
	@Override
	public void setItem(int index,@NotNull ItemStack itemStack){
		inventory.setStackInSlot(index,itemStack);
	}
	@Override
	public @NotNull ItemStack removeItem(int slot,int amount){
		return inventory.extractItem(slot,amount,false);
	}
	@Override
	public @NotNull ItemStack removeItemNoUpdate(int index){
		ItemStack stack=inventory.getStackInSlot(index);
		inventory.setStackInSlot(index,ItemStack.EMPTY);
		return stack;
	}
	@Override
	public boolean isEmpty(){
		for(int i=0;i<inventory.getSlots();i++){
			if(!inventory.getStackInSlot(i).isEmpty()) return false;
		}
		return true;
	}
	@Override
	protected @NotNull NonNullList<ItemStack> getItems(){
		NonNullList<ItemStack> list=NonNullList.withSize(inventory.getSlots(),ItemStack.EMPTY);
		for(int i=0;i<inventory.getSlots();i++) list.set(i,inventory.getStackInSlot(i));
		return list;
	}
	@Override
	protected void setItems(@NotNull NonNullList<ItemStack> stacks){
		for(int i=0;i<stacks.size()&&i<inventory.getSlots();i++){
			inventory.setStackInSlot(i,stacks.get(i));
		}
	}
	// ── setChanged override ───────────────────────────────────────────────────
	@Override
	public void setChanged(){
		super.setChanged();
		if(level!=null&&!level.isClientSide){
			level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
		}
	}
	// ── NBT ───────────────────────────────────────────────────────────────────
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.saveAdditional(tag,provider);
		tag.put("inventory",inventory.serializeNBT(provider));
		tag.putInt("progress",progress);
		tag.putInt("totalTime",totalTime);
		tag.putBoolean("isConflicted",isConflicted);
		saveExtraData(tag,provider);
	}
	@Override
	protected void loadAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.loadAdditional(tag,provider);
		if(tag.contains("inventory")) inventory.deserializeNBT(provider,tag.getCompound("inventory"));
		progress=tag.getInt("progress");
		totalTime=tag.getInt("totalTime");
		isConflicted=tag.getBoolean("isConflicted");
		cachedRecipe=null;
		loadExtraData(tag,provider);
	}
	/**
	 * Uloží data specifická pro daný multiblok (fluid tanky apod.).
	 * Voláno z {@link #saveAdditional}.
	 */
	protected abstract void saveExtraData(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider);
	/**
	 * Načte data specifická pro daný multiblok.
	 * Voláno z {@link #loadAdditional}.
	 */
	protected abstract void loadExtraData(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider);
	// ── Update packet ─────────────────────────────────────────────────────────
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
	@Override
	public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider){
		return saveWithFullMetadata(provider);
	}
	// ── RandomizableContainerBlockEntity (menu) ───────────────────────────────
	@Override
	public @NotNull AbstractContainerMenu createMenu(int id,@NotNull Inventory playerInventory){
		return net.minecraft.world.inventory.ChestMenu.threeRows(id,playerInventory);
	}
}