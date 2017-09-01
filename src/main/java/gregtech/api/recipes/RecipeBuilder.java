package gregtech.api.recipes;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.type.FluidMaterial;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ValidationResult;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static gregtech.api.GTValues.W;

/**
 * @see Recipe
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class RecipeBuilder<T extends Recipe, R extends RecipeBuilder<T, R>> {

	protected RecipeMap<T, R> recipeMap;

	protected List<ItemStack> inputs = new ArrayList<>(0);
	protected List<ItemStack> outputs = new ArrayList<>(0);
	protected TObjectIntMap<ItemStack> chancedOutputs = new TObjectIntHashMap<>(0);

	protected List<FluidStack> fluidInputs = new ArrayList<>(0);
	protected List<FluidStack> fluidOutputs = new ArrayList<>(0);

	protected int duration, EUt;

	protected boolean hidden = false;

	protected boolean canBeBuffered = true;

	protected boolean needsEmptyOutput = false;

	protected boolean optimized = true;

	protected RecipeBuilder() {
		this.inputs = new ArrayList<>(0);
		this.outputs = new ArrayList<>(0);
		this.chancedOutputs = new TObjectIntHashMap<>(0);

		this.fluidInputs = new ArrayList<>(0);
		this.fluidOutputs = new ArrayList<>(0);
	}

	protected RecipeBuilder(T recipe, RecipeMap<T, R> recipeMap) {
		this.recipeMap = recipeMap;
		this.inputs = GTUtility.copyStackList(recipe.getInputs());
		this.outputs = GTUtility.copyStackList(recipe.getOutputs());

		this.chancedOutputs = new TObjectIntHashMap<>();
		recipe.getChancedOutputs().forEachEntry((key, value) -> {
			chancedOutputs.put(key.copy(), value);
			return true;
		});

		this.fluidInputs = GTUtility.copyFluidList(recipe.getFluidInputs());
		this.fluidOutputs = GTUtility.copyFluidList(recipe.getFluidOutputs());

		this.duration = recipe.getDuration();
		this.EUt = recipe.getEUt();
		this.hidden = recipe.isHidden();
		this.canBeBuffered = recipe.canBeBuffered();
		this.needsEmptyOutput = recipe.needsEmptyOutput();
	}

	protected RecipeBuilder(RecipeBuilder<T, R> recipeBuilder) {
		this.recipeMap = recipeBuilder.recipeMap;
		this.inputs = GTUtility.copyStackList(recipeBuilder.getInputs());
		this.outputs = GTUtility.copyStackList(recipeBuilder.getOutputs());

		this.chancedOutputs = new TObjectIntHashMap<>();
		recipeBuilder.getChancedOutputs().forEachEntry((key, value) -> {
			chancedOutputs.put(key.copy(), value);
			return true;
		});

		this.fluidInputs = GTUtility.copyFluidList(recipeBuilder.getFluidInputs());
		this.fluidOutputs = GTUtility.copyFluidList(recipeBuilder.getFluidOutputs());
		this.duration = recipeBuilder.duration;
		this.EUt = recipeBuilder.EUt;
		this.hidden = recipeBuilder.hidden;
		this.canBeBuffered = recipeBuilder.canBeBuffered;
		this.needsEmptyOutput = recipeBuilder.needsEmptyOutput;
		this.optimized = recipeBuilder.optimized;
	}

	public R inputs(ItemStack... inputs) {
		Collections.addAll(this.inputs, inputs);
		return getThis();
	}

	public R inputs(Collection<ItemStack> inputs) {
        this.inputs.addAll(inputs);
        return getThis();
    }

	public R outputs(ItemStack... outputs) {
		Collections.addAll(this.outputs, outputs);
		return getThis();
	}

    public R outputs(Collection<ItemStack> outputs) {
		this.outputs.addAll(outputs);
        return getThis();
    }

	public R fluidInputs(FluidStack... inputs) {
		Collections.addAll(this.fluidInputs, inputs);
		return getThis();
	}

    public R fluidInputs(Collection<FluidStack> inputs) {
		this.fluidInputs.addAll(inputs);
        return getThis();
    }

	public R fluidOutputs(FluidStack... outputs) {
		Collections.addAll(this.fluidOutputs, outputs);
		return getThis();
	}

    public R fluidOutputs(Collection<FluidStack> outputs) {
		this.fluidOutputs.addAll(outputs);
        return getThis();
    }

	public R chancedOutput(ItemStack stack, int chance) {
		if (stack == null) {
			return getThis();
		}

		this.chancedOutputs.put(stack, chance);
		return getThis();
	}

	public R duration(int duration) {
		this.duration = duration;
		return getThis();
	}

	public R EUt(int EUt) {
		this.EUt = EUt;
		return getThis();
	}

	public R hidden() {
		this.hidden = true;
		return getThis();
	}

	public R cannotBeBuffered() {
		this.canBeBuffered = false;
		return getThis();
	}

	public R nonOptimized() {
		this.optimized = false;
		return getThis();
	}

	public R needsEmptyOutput() {
		this.needsEmptyOutput = true;
		return getThis();
	}

	public R setRecipeMap(RecipeMap<T, R> recipeMap) {
		this.recipeMap = recipeMap;
		return getThis();
	}

	public R fromRecipe(T recipe) {
		this.inputs = new ArrayList<>(recipe.getInputs());
		this.outputs = new ArrayList<>(recipe.getOutputs());
		this.chancedOutputs = new TObjectIntHashMap<>(recipe.getChancedOutputs());
		this.fluidInputs = new ArrayList<>(recipe.getFluidInputs());
		this.fluidOutputs = new ArrayList<>(recipe.getFluidOutputs());

		this.duration = recipe.getDuration();
		this.EUt = recipe.getEUt();
		this.hidden = recipe.isHidden();
		this.canBeBuffered = recipe.canBeBuffered();
		this.needsEmptyOutput = recipe.needsEmptyOutput();
		return getThis();
	}

	public abstract R copy();

	// To get rid of "unchecked cast" warning
	protected abstract R getThis();

	protected EnumValidationResult finalizeAndValidate() {
		inputs.replaceAll(OreDictUnifier::getUnificated);
		outputs.replaceAll(OreDictUnifier::getUnificated);

		TObjectIntMap<ItemStack> newMap = new TObjectIntHashMap<>();
		TObjectIntIterator<ItemStack> iterator = chancedOutputs.iterator();
		while (iterator.hasNext()) {
			newMap.put(OreDictUnifier.getUnificated(iterator.key()), iterator.value());
		}
		chancedOutputs = newMap;

		for (ItemStack stack : inputs) {
			if (Items.FEATHER.getDamage(stack) != W) {
				for (int j = 0; j < outputs.size(); j++) {
					if (ItemStack.areItemStacksEqual(stack, outputs.get(j))) {
						if (stack.stackSize >= outputs.get(j).stackSize) {
							stack.stackSize -= outputs.get(j).stackSize;
							outputs.remove(j);
						} else {
							outputs.get(j).stackSize -= stack.stackSize;
						}
					}
				}
			}
		}

		if (optimized && duration >= 32) {
			ArrayList<ItemStack> itemStacks = new ArrayList<>();
			itemStacks.addAll(inputs);
			itemStacks.addAll(outputs);

			for (byte i = (byte) Math.min(64, duration / 16); i > 1; i--)
				if (duration / i >= 16) {
					boolean temp = true;
					for (int j = 0, k = itemStacks.size(); temp && j < k; j++)
						if (itemStacks.get(j).stackSize % i != 0) temp = false;
					for (int j = 0; temp && j < fluidInputs.size(); j++)
						if (fluidInputs.get(j).amount % i != 0) temp = false;
					for (int j = 0; temp && j < fluidOutputs.size(); j++)
						if (fluidOutputs.get(j).amount % i != 0) temp = false;
					if (temp) {
						for (ItemStack stack : itemStacks) stack.stackSize /= i;
						for (FluidStack fluidInput : fluidInputs) fluidInput.amount /= i;
						for (FluidStack fluidOutput : fluidOutputs) fluidOutput.amount /= i;
						duration /= i;
					}
				}
		}

		return validate();
	}

	public abstract ValidationResult<T> build();

    private EnumValidationResult validate() {
        return validate(EnumValidationResult.VALID);
    }

	protected EnumValidationResult validate(EnumValidationResult result) {

		if (recipeMap == null) {
			GTLog.logger.error("RecipeMap cannot be null", new IllegalArgumentException());
			result = EnumValidationResult.INVALID;
		}

		if (inputs.contains(null)) {
			GTLog.logger.error("Input cannot contain null ItemStacks", new IllegalArgumentException());
			result = EnumValidationResult.INVALID;
		}
		if (outputs.contains(null)) {
			GTLog.logger.error("Output cannot contain null ItemStacks", new IllegalArgumentException());
			result = EnumValidationResult.INVALID;
		}
		if (fluidInputs.contains(null)) {
			GTLog.logger.error("Fluid input cannot contain null FluidStacks", new IllegalArgumentException());
			result = EnumValidationResult.INVALID;
		}
		if (fluidOutputs.contains(null)) {
			GTLog.logger.error("Fluid output cannot contain null FluidStacks", new IllegalArgumentException());
			result = EnumValidationResult.INVALID;
		}

		for (int chance : chancedOutputs.values()) {
            if (0 >= chance || chance > 10000){
                GTLog.logger.error("Chance cannot be less or equal to 0 or more than 10000", new IllegalArgumentException());
                result = EnumValidationResult.INVALID;
            }
		}

        if (!GTUtility.isBetweenExclusive(recipeMap.getMinInputs(), recipeMap.getMaxInputs(), inputs.size())){
            GTLog.logger.error("Invalid amount of recipe inputs", new IllegalArgumentException());
            result = EnumValidationResult.INVALID;
		}
		if (!GTUtility.isBetweenExclusive(recipeMap.getMinOutputs(), recipeMap.getMaxOutputs(), outputs.size() + chancedOutputs.size())){
            GTLog.logger.error("Invalid amount of recipe outputs", new IllegalArgumentException());
            result = EnumValidationResult.INVALID;
		}
		if (!GTUtility.isBetweenExclusive(recipeMap.getMinFluidInputs(), recipeMap.getMaxFluidInputs(), fluidInputs.size())){
            GTLog.logger.error("Invalid amount of recipe fluid inputs", new IllegalArgumentException());
            result = EnumValidationResult.INVALID;
		}
		if (!GTUtility.isBetweenExclusive(recipeMap.getMinFluidOutputs(), recipeMap.getMaxFluidOutputs(), fluidOutputs.size())){
            GTLog.logger.error("Invalid amount of recipe fluid outputs", new IllegalArgumentException());
            result = EnumValidationResult.INVALID;
		}

//			Validate.isTrue(EUt > 0, "EU/t cannot be less of equal to 0");

        if (duration <= 0){
            GTLog.logger.error("Duration cannot be less or equal to 0", new IllegalArgumentException());
            result = EnumValidationResult.INVALID;
        }

		//For fakeRecipes don't do check for collisions, regular recipes do check, do not check for recipes that are not registered(i.e. created after postinit stage)
		if (!(recipeMap instanceof RecipeMap.FakeRecipeMap) &&
				recipeMap.findRecipe(null, true, Long.MAX_VALUE, this.fluidInputs.toArray(new FluidStack[0]), this.inputs.toArray(new ItemStack[0])) != null) {
			return EnumValidationResult.SKIP;
		}

		return result;
	}

	public void buildAndRegister() {
		recipeMap.addRecipe(build());
	}

	///////////////////
	//    Getters    //
	///////////////////

	public List<ItemStack> getInputs() {
		return inputs;
	}

	public List<ItemStack> getOutputs() {
		return outputs;
	}

	public TObjectIntMap<ItemStack> getChancedOutputs() {
		return chancedOutputs;
	}

	public List<FluidStack> getFluidInputs() {
		return fluidInputs;
	}

	public List<FluidStack> getFluidOutputs() {
		return fluidOutputs;
	}

	public static class DefaultRecipeBuilder extends RecipeBuilder<Recipe, DefaultRecipeBuilder> {

		public DefaultRecipeBuilder() {
		}

		public DefaultRecipeBuilder(Recipe recipe, RecipeMap<Recipe, DefaultRecipeBuilder> recipeMap) {
			super(recipe, recipeMap);
		}

		public DefaultRecipeBuilder(RecipeBuilder<Recipe, DefaultRecipeBuilder> recipeBuilder) {
			super(recipeBuilder);
		}

		@Override
		protected DefaultRecipeBuilder getThis() {
			return this;
		}

		@Override
		public DefaultRecipeBuilder copy() {
			return new DefaultRecipeBuilder(this);
		}

		public ValidationResult<Recipe> build() {
			return ValidationResult.newResult(finalizeAndValidate(),
						new Recipe(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs,
									duration, EUt, hidden, canBeBuffered, needsEmptyOutput));
		}
	}

	public static class IntCircuitRecipeBuilder extends RecipeBuilder<Recipe, IntCircuitRecipeBuilder> {

		protected int circuitMeta = -1;

		public IntCircuitRecipeBuilder() {
		}

		public IntCircuitRecipeBuilder(Recipe recipe, RecipeMap<Recipe, RecipeBuilder.IntCircuitRecipeBuilder> recipeMap) {
			super(recipe, recipeMap);
		}

		public IntCircuitRecipeBuilder(RecipeBuilder<Recipe, RecipeBuilder.IntCircuitRecipeBuilder> recipeBuilder) {
			super(recipeBuilder);
		}

		@Override
		protected IntCircuitRecipeBuilder getThis() {
			return this;
		}

		@Override
		public IntCircuitRecipeBuilder copy() {
			return new IntCircuitRecipeBuilder(this);
		}

		public IntCircuitRecipeBuilder circuitMeta(int circuitMeta) {
			this.circuitMeta = circuitMeta;
			return this;
		}

		@Override
		protected EnumValidationResult finalizeAndValidate() {
			if (circuitMeta >= 0) {
//				inputs.add(ItemList.Circuit_Integrated.getWithDamage(0, circuitMeta));
			}
			return super.finalizeAndValidate();
		}

		@Override
		protected EnumValidationResult validate(EnumValidationResult result) {
			if (circuitMeta < 0) {
			    GTLog.logger.error("Integrated Circuit Metadata cannot be less than 0", new IllegalArgumentException()); // TODO cannot be more than what?
                result = EnumValidationResult.INVALID;
			}
			return super.validate(result);
		}

		public ValidationResult<Recipe> build() {
			return ValidationResult.newResult(finalizeAndValidate(),
					new Recipe(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs,
							duration, EUt, hidden, canBeBuffered, needsEmptyOutput));
		}
	}

	public static class NotConsumableInputRecipeBuilder extends RecipeBuilder<Recipe, NotConsumableInputRecipeBuilder> {

		public NotConsumableInputRecipeBuilder() {
		}

		public NotConsumableInputRecipeBuilder(Recipe recipe, RecipeMap<Recipe, NotConsumableInputRecipeBuilder> recipeMap) {
			super(recipe, recipeMap);
		}

		public NotConsumableInputRecipeBuilder(RecipeBuilder<Recipe, NotConsumableInputRecipeBuilder> recipeBuilder) {
			super(recipeBuilder);
		}

		@Override
		protected NotConsumableInputRecipeBuilder getThis() {
			return this;
		}

		@Override
		public NotConsumableInputRecipeBuilder copy() {
			return new NotConsumableInputRecipeBuilder(this);
		}

		public NotConsumableInputRecipeBuilder notConsumable(Item item) {
			inputs.add(new ItemStack(item, 0));
			return this;
		}

		public NotConsumableInputRecipeBuilder notConsumable(ItemStack itemStack) {
			ItemStack stack = itemStack.copy();
			stack.stackSize = 0;
			inputs.add(stack);
			return this;
		}

		public NotConsumableInputRecipeBuilder notConsumable(MetaItem<?>.MetaValueItem item) {
			inputs.add(item.getStackForm(0));
			return this;
		}

		public ValidationResult<Recipe> build() {
			return ValidationResult.newResult(finalizeAndValidate(),
					new Recipe(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs,
							duration, EUt, hidden, canBeBuffered, needsEmptyOutput));
		}
	}

	public static class BlastRecipeBuilder extends RecipeBuilder<Recipe.BlastRecipe, BlastRecipeBuilder> {

		private int blastFurnaceTemp;

		public BlastRecipeBuilder() {
		}

		public BlastRecipeBuilder(Recipe.BlastRecipe recipe, RecipeMap<Recipe.BlastRecipe, BlastRecipeBuilder> recipeMap) {
			super(recipe, recipeMap);
			this.blastFurnaceTemp = recipe.getBlastFurnaceTemp();
		}

		public BlastRecipeBuilder(RecipeBuilder<Recipe.BlastRecipe, BlastRecipeBuilder> recipeBuilder) {
			super(recipeBuilder);
		}

		@Override
		protected BlastRecipeBuilder getThis() {
			return this;
		}

		@Override
		public BlastRecipeBuilder copy() {
			return new BlastRecipeBuilder(this);
		}

		public BlastRecipeBuilder blastFurnaceTemp(int blastFurnaceTemp) {
			this.blastFurnaceTemp = blastFurnaceTemp;
			return getThis();
		}

		@Override
		protected EnumValidationResult validate(EnumValidationResult result) {
			if (blastFurnaceTemp <= 0) {
                GTLog.logger.error("Blast Furnace Temperature cannot be less than or equal to 0", new IllegalArgumentException());
                result = EnumValidationResult.INVALID;
            }
			return super.validate(result);
		}

		public ValidationResult<Recipe.BlastRecipe> build() {
			return ValidationResult.newResult(finalizeAndValidate(),
					new Recipe.BlastRecipe(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs,
							duration, EUt, hidden, canBeBuffered, needsEmptyOutput, blastFurnaceTemp));
		}
	}

	public static class ArcFurnaceRecipeBuilder extends RecipeBuilder<Recipe, ArcFurnaceRecipeBuilder> {

		protected boolean simple = false;

		public ArcFurnaceRecipeBuilder() {
		}

		public ArcFurnaceRecipeBuilder(Recipe recipe, RecipeMap<Recipe, ArcFurnaceRecipeBuilder> recipeMap) {
			super(recipe, recipeMap);
		}

		public ArcFurnaceRecipeBuilder(RecipeBuilder<Recipe, ArcFurnaceRecipeBuilder> recipeBuilder) {
			super(recipeBuilder);
		}

		@Override
		protected ArcFurnaceRecipeBuilder getThis() {
			return this;
		}

		@Override
		public ArcFurnaceRecipeBuilder copy() {
			return new ArcFurnaceRecipeBuilder(this);
		}

		public ArcFurnaceRecipeBuilder simple() {
			this.simple = true;
			return getThis();
		}

		@Override
		public void buildAndRegister() {
			if (simple) {
				this.copy().buildAndRegister();
			} else {
				this.copy().fluidInputs(Materials.Oxygen.getFluid(this.duration)).buildAndRegister();

				for (FluidMaterial material : new FluidMaterial[]{Materials.Argon, Materials.Nitrogen}) {
					int plasmaAmount = (int) Math.max(1L, this.duration / (material.getMass() * 16L));

					DefaultRecipeBuilder builder = RecipeMap.PLASMA_ARC_FURNACE_RECIPES.recipeBuilder()
							.inputs(this.inputs)
							.outputs(this.outputs)
							.duration(this.duration / 16)
							.EUt(this.EUt / 3)
							.fluidInputs(material.getPlasma(plasmaAmount))
							.fluidOutputs(material.getFluid(plasmaAmount));

					builder.chancedOutputs = new TObjectIntHashMap<>(this.getChancedOutputs());

					builder.buildAndRegister();
				}
			}
		}

		public ValidationResult<Recipe> build() {
			return ValidationResult.newResult(finalizeAndValidate(),
					new Recipe(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs,
							duration, EUt, hidden, canBeBuffered, needsEmptyOutput));
		}
	}

	public static class ImplosionRecipeBuilder extends RecipeBuilder<Recipe, ImplosionRecipeBuilder> {

		protected int explosivesAmount;

		public ImplosionRecipeBuilder() {
		}

		public ImplosionRecipeBuilder(Recipe recipe, RecipeMap<Recipe, ImplosionRecipeBuilder> recipeMap) {
			super(recipe, recipeMap);
		}

		public ImplosionRecipeBuilder(RecipeBuilder<Recipe, ImplosionRecipeBuilder> recipeBuilder) {
			super(recipeBuilder);
		}

		@Override
		protected ImplosionRecipeBuilder getThis() {
			return this;
		}

		@Override
		public ImplosionRecipeBuilder copy() {
			return new ImplosionRecipeBuilder(this);
		}

		public ImplosionRecipeBuilder explosivesAmount(int explosivesAmount) {
			this.explosivesAmount = explosivesAmount;
			return this;
		}

		@Override
		public void buildAndRegister() {
			int gunpowder = explosivesAmount * 2;
			int dynamite = explosivesAmount * 4;
			int TNT = Math.max(1, explosivesAmount / 2);
			int ITNT = Math.max(1, explosivesAmount / 4);

			ItemStack input = inputs.get(0);
			if (gunpowder < 65) {
//				recipeMap.addRecipe(this.copy().inputs(input, ItemList.Block_Powderbarrel.get(gunpowder)).build());
			}
			if (dynamite < 17) {
				recipeMap.addRecipe(this.copy().inputs(input, ModHandler.IC2.getIC2Item(ItemName.dynamite, dynamite)).build());
			}
			recipeMap.addRecipe(this.copy().inputs(input, new ItemStack(Blocks.TNT, TNT)).build());
			recipeMap.addRecipe(this.copy().inputs(input, ModHandler.IC2.getIC2Item(BlockName.te, TeBlock.itnt, ITNT)).build());
		}

		@Override
		protected EnumValidationResult validate(EnumValidationResult result) {
            if (!GTUtility.isBetweenInclusive(1, 64, explosivesAmount)) {
                GTLog.logger.error("Amount of explosives should be from 1 to 64 inclusive", new IllegalArgumentException());
                result = EnumValidationResult.INVALID;
            }
			return super.validate(result);
		}

		public ValidationResult<Recipe> build() {
			return ValidationResult.newResult(finalizeAndValidate(),
					new Recipe(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs,
							duration, EUt, hidden, canBeBuffered, needsEmptyOutput));
		}
	}

	public static class UniversalDistillationRecipeBuilder extends RecipeBuilder<Recipe, UniversalDistillationRecipeBuilder> {

		protected boolean universal = false;

		public UniversalDistillationRecipeBuilder() {
		}

		public UniversalDistillationRecipeBuilder(Recipe recipe, RecipeMap<Recipe, UniversalDistillationRecipeBuilder> recipeMap) {
			super(recipe, recipeMap);
		}

		public UniversalDistillationRecipeBuilder(RecipeBuilder<Recipe, UniversalDistillationRecipeBuilder> recipeBuilder) {
			super(recipeBuilder);
		}

		@Override
		protected UniversalDistillationRecipeBuilder getThis() {
			return this;
		}

		@Override
		public UniversalDistillationRecipeBuilder copy() {
			return new UniversalDistillationRecipeBuilder(this);
		}

		public UniversalDistillationRecipeBuilder universal() {
			this.universal = true;
			return getThis();
		}

		@Override
		public void buildAndRegister() {
			if (universal) {
				IntCircuitRecipeBuilder builder = RecipeMap.DISTILLERY_RECIPES.recipeBuilder()
						.fluidInputs(this.fluidInputs.toArray(new FluidStack[0]))
						.duration(this.duration * 2)
						.EUt(this.EUt / 4);

				for (int i = 0; i < fluidOutputs.size(); i++) {
					builder.copy().circuitMeta(i).fluidOutputs(this.fluidOutputs.get(i)).buildAndRegister();
				}
			}

			super.buildAndRegister();
		}

		public ValidationResult<Recipe> build() {
			return ValidationResult.newResult(finalizeAndValidate(),
					new Recipe(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs,
							duration, EUt, hidden, canBeBuffered, needsEmptyOutput));
		}
	}

	public static class AmplifierRecipeBuilder extends RecipeBuilder<Recipe.AmplifierRecipe, AmplifierRecipeBuilder> {

		private int amplifierAmountOutputted = -1;

		public AmplifierRecipeBuilder() {}

		public AmplifierRecipeBuilder(Recipe.AmplifierRecipe recipe, RecipeMap<Recipe.AmplifierRecipe, AmplifierRecipeBuilder> recipeMap) {
			super(recipe, recipeMap);
		}

		public AmplifierRecipeBuilder(RecipeBuilder<Recipe.AmplifierRecipe, AmplifierRecipeBuilder> recipeBuilder) {
			super(recipeBuilder);
		}

		@Override
		protected AmplifierRecipeBuilder getThis() {
			return this;
		}

		@Override
		public AmplifierRecipeBuilder copy() {
			return new AmplifierRecipeBuilder(this);
		}

		public AmplifierRecipeBuilder amplifierAmountOutputted(int amplifierAmountOutputted) {
			this.amplifierAmountOutputted = amplifierAmountOutputted;
			return getThis();
		}

		@Override
		protected EnumValidationResult finalizeAndValidate() {
			if (amplifierAmountOutputted > 0) {
				this.fluidOutputs(Materials.UUAmplifier.getFluid(amplifierAmountOutputted));
			}
			return super.finalizeAndValidate();
		}

		@Override
		protected EnumValidationResult validate(EnumValidationResult result) {
            if (amplifierAmountOutputted <= 0) {
                GTLog.logger.error("Outputted Amplifier Amount cannot be less than or equal to 0", new IllegalArgumentException());
                result = EnumValidationResult.INVALID;
            }
			return super.validate(result);
		}

		public ValidationResult<Recipe.AmplifierRecipe> build() {
			return ValidationResult.newResult(finalizeAndValidate(),
					new Recipe.AmplifierRecipe(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs,
							duration, EUt, hidden, canBeBuffered, needsEmptyOutput, amplifierAmountOutputted));
		}
	}

	public static class BrewingRecipeBuilder extends RecipeBuilder<Recipe, BrewingRecipeBuilder> {

		public BrewingRecipeBuilder() {}

		public BrewingRecipeBuilder(Recipe recipe, RecipeMap<Recipe, BrewingRecipeBuilder> recipeMap) {
			super(recipe, recipeMap);
		}

		public BrewingRecipeBuilder(RecipeBuilder<Recipe, BrewingRecipeBuilder> recipeBuilder) {
			super(recipeBuilder);
		}

		@Override
		protected BrewingRecipeBuilder getThis() {
			return this;
		}

		@Override
		public BrewingRecipeBuilder copy() {
			return new BrewingRecipeBuilder(this);
		}

		@Deprecated // Use BrewingRecipeBuilder#fluidInput(Fluid)
		@Override
		public BrewingRecipeBuilder fluidInputs(@Nonnull FluidStack... inputs) {
			throw new UnsupportedOperationException("This method should not get called. Use BrewingRecipeBuilder#fluidInput(Fluid)");
		}

		@Deprecated // Use BrewingRecipeBuilder#fluidOutput(Fluid)
		@Override
		public BrewingRecipeBuilder fluidOutputs(@Nonnull FluidStack... outputs) {
			throw new UnsupportedOperationException("This method should not get called. Use BrewingRecipeBuilder#fluidOutput(Fluid)");
		}

		public BrewingRecipeBuilder fluidInput(@Nonnull Fluid input) {
			this.fluidInputs.add(new FluidStack(input, 750));
			return getThis();
		}

		public BrewingRecipeBuilder fluidOutput(@Nonnull Fluid output) {
			this.fluidOutputs.add(new FluidStack(output, 750));
			return getThis();
		}

		public ValidationResult<Recipe> build() {
			return ValidationResult.newResult(finalizeAndValidate(),
					new Recipe(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs,
							duration, EUt, hidden, canBeBuffered, needsEmptyOutput));
		}
	}

	public static class FusionRecipeBuilder extends RecipeBuilder<Recipe.FusionRecipe, FusionRecipeBuilder> {

		private int EUToStart;

		public FusionRecipeBuilder() {}

		public FusionRecipeBuilder(Recipe.FusionRecipe recipe, RecipeMap<Recipe.FusionRecipe, FusionRecipeBuilder> recipeMap) {
			super(recipe, recipeMap);
			this.EUToStart = recipe.getEUToStart();

		}

		public FusionRecipeBuilder(RecipeBuilder<Recipe.FusionRecipe, FusionRecipeBuilder> recipeBuilder) {
			super(recipeBuilder);
		}

		@Override
		protected FusionRecipeBuilder getThis() {
			return this;
		}

		@Override
		public FusionRecipeBuilder copy() {
			return new FusionRecipeBuilder(this);
		}

		public FusionRecipeBuilder EUToStart(int EUToStart) {
			this.EUToStart = EUToStart;
			return getThis();
		}

		@Override
		protected EnumValidationResult validate(EnumValidationResult result) {
            if (EUToStart <= 0) {
                GTLog.logger.error("EU to start cannot be less than or equal to 0", new IllegalArgumentException());
                result = EnumValidationResult.INVALID;
            }
            return super.validate(result);
		}

		public ValidationResult<Recipe.FusionRecipe> build() {
			return ValidationResult.newResult(finalizeAndValidate(),
					new Recipe.FusionRecipe(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs,
							duration, EUt, hidden, canBeBuffered, needsEmptyOutput, EUToStart));
		}
	}

	public static class AssemblyLineRecipeBuilder {

		private ItemStack researchItem;
		private int researchTime;

		private List<ItemStack> inputs;
		private List<FluidStack> fluidInputs;
		private ItemStack output;

		private int duration;
		private int EUt;

		private AssemblyLineRecipeBuilder() {
		}

		public AssemblyLineRecipeBuilder start() {
			return new AssemblyLineRecipeBuilder();
		}

		public AssemblyLineRecipeBuilder researchItem(ItemStack researchItem) {
			this.researchItem = researchItem;
			return this;
		}

		public AssemblyLineRecipeBuilder researchTime(int researchTime) {
			this.researchTime = researchTime;
			return this;
		}

		public AssemblyLineRecipeBuilder inputs(@Nonnull ItemStack... inputs) {
			Collections.addAll(this.inputs, inputs);
			return this;
		}

		public AssemblyLineRecipeBuilder fluidInputs(@Nonnull FluidStack... inputs) {
			Collections.addAll(this.fluidInputs, inputs);
			return this;
		}

		public AssemblyLineRecipeBuilder output(ItemStack output) {
			this.output = output;
			return this;
		}

		public AssemblyLineRecipeBuilder duration(int duration) {
			this.duration = duration;
			return this;
		}

		public AssemblyLineRecipeBuilder EUt(int EUt) {
			this.EUt = EUt;
			return this;
		}

		public ValidationResult<Recipe.AssemblyLineRecipe> build() {
			return ValidationResult.newResult(validate(),
					new Recipe.AssemblyLineRecipe(researchItem, researchTime, inputs, fluidInputs, output, duration, EUt));
		}

		protected EnumValidationResult validate() {
		    EnumValidationResult result = EnumValidationResult.VALID;

            if (inputs.contains(null)) {
                GTLog.logger.error("Input cannot contain null ItemStacks", new IllegalArgumentException());
                result = EnumValidationResult.INVALID;
            }
            if (fluidInputs.contains(null)) {
                GTLog.logger.error("Fluid input cannot contain null FluidStacks", new IllegalArgumentException());
                result = EnumValidationResult.INVALID;
            }

            if (output == null) {
                GTLog.logger.error("Output ItemStack cannot be null", new IllegalArgumentException());
                result = EnumValidationResult.INVALID;
            }
            if (researchItem == null) {
                GTLog.logger.error("Research ItemStack cannot be null", new IllegalArgumentException());
                result = EnumValidationResult.INVALID;
            }

            if (researchTime <= 0) {
                GTLog.logger.error("Research Time cannot be less or equal to 0", new IllegalArgumentException());
                result = EnumValidationResult.INVALID;
            }
            if (duration <= 0) {
                GTLog.logger.error("Duration cannot be less or equal to 0", new IllegalArgumentException());
                result = EnumValidationResult.INVALID;
            }
            if (EUt <= 0) {
                GTLog.logger.error("EUt cannot be less or equal to 0", new IllegalArgumentException());
                result = EnumValidationResult.INVALID;
            }

            if (!GTUtility.isBetweenInclusive(4, 16, inputs.size())) {
                GTLog.logger.error("Invalid amount of recipe inputs. Should be between {} and {} inclusive", 4, 16);
                GTLog.logger.error("", new IllegalArgumentException());
                result = EnumValidationResult.INVALID;
            }
            if (!GTUtility.isBetweenInclusive(0, 4, fluidInputs.size())) {
                GTLog.logger.error("Invalid amount of recipe fluid inputs. Should be between {} and {} inclusive", 0, 4);
                GTLog.logger.error("", new IllegalArgumentException());
                result = EnumValidationResult.INVALID;
            }

			return result;
		}

		public void buildAndRegister() {
			ValidationResult<Recipe.AssemblyLineRecipe> result = build();

			if (result.getType() == EnumValidationResult.VALID) {
				RecipeMap.ASSEMBLYLINE_RECIPES.add(result.getResult());
			}
		}
	}
}
