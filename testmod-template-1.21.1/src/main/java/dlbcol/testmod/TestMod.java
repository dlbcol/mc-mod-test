package dlbcol.testmod;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(TestMod.MODID)
public class TestMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "testmod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final String WB_ACTIVE_TAG = MODID + ".water_breathing_active";
    private static final String WB_MODE_TAG = MODID + ".water_breathing_mode";
    // Create a Deferred Register to hold Blocks which will all be registered under the "testmod" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "testmod" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "testmod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a new Block with the id "testmod:example_block", combining the namespace and path
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    // Creates a new BlockItem with the id "testmod:example_block", combining the namespace and path
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);

    // Creates a new food item with the id "testmod:example_id", nutrition 1 and saturation 2
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEdible().nutrition(1).saturationModifier(2f).build()));

// Ocean side-set: utility-focused gear for underwater exploration.
    // Iron-tier armor value + strong exploration effects for mid-game viability.
    public static final DeferredItem<Item> BAMBOO_RESPIRATOR_HELMET = ITEMS.register(
            "bamboo_respirator_helmet",
            () -> new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final DeferredItem<Item> CROP_COPPER_BACKTANK_CHESTPLATE = ITEMS.register(
            "crop_copper_backtank_chestplate",
            () -> new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.CHESTPLATE, new Item.Properties()));

    public static final DeferredItem<Item> STRAW_FLIPPERS = ITEMS.register(
            "straw_flippers",
            () -> new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.BOOTS, new Item.Properties()));

    // Creates a creative tab with the id "testmod:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.testmod")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(EXAMPLE_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
                output.accept(BAMBOO_RESPIRATOR_HELMET.get());
                output.accept(CROP_COPPER_BACKTANK_CHESTPLATE.get());
                output.accept(STRAW_FLIPPERS.get());
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public TestMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (TestMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        var player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        boolean hasHelmet = player.getItemBySlot(EquipmentSlot.HEAD).is(BAMBOO_RESPIRATOR_HELMET.get());
        boolean hasChest = player.getItemBySlot(EquipmentSlot.CHEST).is(CROP_COPPER_BACKTANK_CHESTPLATE.get());
        boolean hasBoots = player.getItemBySlot(EquipmentSlot.FEET).is(STRAW_FLIPPERS.get());
        boolean inWater = player.isInWaterOrBubble();
        boolean submerged = player.isUnderWater();
        var persistentData = player.getPersistentData();

        MobEffectInstance waterBreathing = player.getEffect(MobEffects.WATER_BREATHING);
        boolean waterBreathingFromArmor = waterBreathing != null && waterBreathing.isAmbient();
        boolean waterBreathingArmorActive = persistentData.getBoolean(WB_ACTIVE_TAG);
        int currentWaterBreathingMode = hasHelmet ? (hasChest ? 2 : 1) : 0;
        int waterBreathingDuration = currentWaterBreathingMode == 2 ? 2400 : 200;

        MobEffectInstance dolphinsGrace = player.getEffect(MobEffects.DOLPHINS_GRACE);
        boolean dolphinsGraceFromArmor = dolphinsGrace != null && dolphinsGrace.isAmbient();

        if (!submerged || !hasHelmet) {
            if (waterBreathingFromArmor) {
                player.removeEffect(MobEffects.WATER_BREATHING);
            }

            // Reset grant state once the player is no longer submerged.
            // This prevents refreshing while still underwater but allows a fresh grant after resurfacing.
            if (!submerged) {
                persistentData.putBoolean(WB_ACTIVE_TAG, false);
                persistentData.putInt(WB_MODE_TAG, 0);
            }
        }

        // Helmet alone: ~10 seconds of water breathing.
        // Helmet + Backtank: ~2 minutes of water breathing (temporary stand-in until tank-fill mechanic exists).
        // Flippers: continuously refreshed swim speed boost while underwater.
        if (submerged && hasHelmet && !waterBreathingArmorActive) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, waterBreathingDuration, 0, true, false, true));
            persistentData.putBoolean(WB_ACTIVE_TAG, true);
            persistentData.putInt(WB_MODE_TAG, currentWaterBreathingMode);
        }

        // If backtank is removed mid-swim, immediately reduce remaining armor breathing to helmet duration.
        // Equipping backtank again underwater will not extend/refresh the timer.
        if (submerged && hasHelmet && !hasChest && waterBreathingFromArmor && waterBreathing != null && waterBreathing.getDuration() > 200) {
            player.removeEffect(MobEffects.WATER_BREATHING);
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 200, 0, true, false, true));
            persistentData.putInt(WB_MODE_TAG, 1);
        }

        if (!inWater || !hasBoots) {
            if (dolphinsGraceFromArmor) {
                player.removeEffect(MobEffects.DOLPHINS_GRACE);
            }
        }

        if (inWater && hasBoots) {
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 50, 0, true, false, true));
        }
    }
}
