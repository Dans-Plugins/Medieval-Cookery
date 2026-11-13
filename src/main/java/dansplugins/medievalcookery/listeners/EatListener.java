package dansplugins.medievalcookery.listeners;

import dansplugins.medievalcookery.CustomFoodRecipe;
import dansplugins.medievalcookery.DelayedExecution;
import dansplugins.medievalcookery.MedievalCookery;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.CaveVinesPlant;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitTask;

import java.util.EnumSet;
import java.util.Set;

/**
 * Starts an eating session when a player right-clicks while holding one of the plugin's foods.
 *
 * The foods are player heads, which vanilla Minecraft does not treat as edible, so no consumption
 * event is ever fired for them: right-clicking one either does nothing or places it as a block.
 * The interaction is therefore cancelled and replaced with the eating flow, which
 * {@link DelayedExecution} completes once the player has held still long enough.
 */
public class EatListener implements Listener {

    /** Vanilla takes 32 ticks to finish eating a food item, and the custom foods take the same. */
    public static final int EATING_DURATION_TICKS = 32;

    /** A full hunger bar, at which vanilla does not let a player eat an ordinary food. */
    static final int MAX_FOOD_LEVEL = 20;

    /** How far away a mob on a player's lead can be and still be tied to a clicked fence. */
    private static final double LEAD_REACH_BLOCKS = 7.0;

    /** Every constant here must exist on the oldest supported server; see {@code api-version}. */
    private static final Set<Material> BLOCKS_THAT_PASS_ON_A_FOOD = EnumSet.of(
            Material.PUMPKIN,
            Material.TNT,
            Material.CAULDRON,
            Material.WATER_CAULDRON,
            Material.LAVA_CAULDRON,
            Material.POWDER_SNOW_CAULDRON,
            Material.BEEHIVE,
            Material.BEE_NEST,
            Material.CAMPFIRE,
            Material.SOUL_CAMPFIRE,
            Material.IRON_DOOR,
            Material.IRON_TRAPDOOR);

    /** Blocks that open a screen only for a player who can use game-master blocks. */
    private static final Set<Material> GAME_MASTER_BLOCKS = EnumSet.of(
            Material.COMMAND_BLOCK,
            Material.CHAIN_COMMAND_BLOCK,
            Material.REPEATING_COMMAND_BLOCK,
            Material.STRUCTURE_BLOCK,
            Material.JIGSAW);

    private final MedievalCookery medievalCookery;

    public EatListener(MedievalCookery medievalCookery) {
        this.medievalCookery = medievalCookery;
    }

    /**
     * Runs at HIGH priority so that a plugin denying the click at NORMAL or below has done so
     * before a meal is decided on; see {@link #isItemUseDenied}.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (!isEatingGesture(event.getAction()) || event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (isItemUseDenied(event)) {
            return;
        }
        Player player = event.getPlayer();
        if (isBlockInteractionInstead(event.getAction(), clickedType(event), player.isSneaking(),
                canUseGameMasterBlocks(player.getGameMode(), player.isOp()))
                && !passesFoodClickInItsState(clickedData(event))) {
            return;
        }

        CustomFoodRecipe recipe = medievalCookery.recipeOf(event.getItem());
        if (recipe == null) {
            return;
        }
        if (isFence(clickedType(event)) && !player.isSneaking() && isLeadingMob(player)) {
            return;
        }

        // Cancelled before the in-progress check, so that clicking again mid-meal does not place
        // the head as a block either.
        event.setCancelled(true);

        if (medievalCookery.isPlayerEating(player)) {
            return;
        }
        if (!canEat(player.getFoodLevel(), player.getGameMode())) {
            return;
        }

        medievalCookery.startPlayerEating(player, recipe.key);
        DelayedExecution delayedExecution = new DelayedExecution(medievalCookery);
        BukkitTask eatingSound = delayedExecution.PlayEatingSound(player);
        delayedExecution.ConsumeItemInMainHand(player, EATING_DURATION_TICKS, eatingSound);
    }

    /**
     * Only a right-click begins a meal. A left-click is an attack or a block break, and PHYSICAL
     * is a pressure plate or tripwire being stepped on, neither of which should cost the player
     * the food they happen to be carrying.
     */
    static boolean isEatingGesture(Action action) {
        return action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
    }

    /**
     * Reports whether another plugin has denied the use of the held item for this click.
     *
     * A protection, lobby or jail plugin usually cancels the event, which sets
     * {@code useItemInHand} to DENY, and the server then does not eat a vanilla food either.
     * {@code isCancelled()} cannot be used instead: a right-click in the air reaches every
     * listener already cancelled, because there is no block to use, while the item may still be.
     */
    static boolean isItemUseDenied(PlayerInteractEvent event) {
        return event.useItemInHand() == Event.Result.DENY;
    }

    /**
     * Decides whether a player is hungry enough to begin a meal.
     *
     * Vanilla only starts eating an ordinary food when the hunger bar is below full, or when the
     * player is in creative mode. A full player's right-click would otherwise cost them the food,
     * since the Saturation effect cannot raise a food level that is already at its maximum.
     *
     * A player in spectator mode eats at no food level. Vanilla lets a spectator use no item, but
     * they keep the inventory and hunger bar they had, so the food level alone would let them.
     */
    static boolean canEat(int foodLevel, GameMode gameMode) {
        if (gameMode == GameMode.SPECTATOR) {
            return false;
        }
        return foodLevel < MAX_FOOD_LEVEL || gameMode == GameMode.CREATIVE;
    }

    /**
     * Reports whether the clicked block would have claimed this right-click for itself.
     *
     * Vanilla gives a block's own interaction priority over the item being held, so opening a
     * chest while carrying food opens the chest rather than eating. Cancelling the event here
     * regardless would make every container, door and workstation unusable to a player carrying
     * a food. Sneaking suppresses the block's interaction in vanilla, and so does not stop a meal.
     * {@code canUseGameMasterBlocks} is whether the player may open a command block; see
     * {@link #canUseGameMasterBlocks}.
     */
    static boolean isBlockInteractionInstead(Action action, Material clickedBlockType, boolean sneaking,
                                             boolean canUseGameMasterBlocks) {
        return action == Action.RIGHT_CLICK_BLOCK
                && !sneaking
                && clickedBlockType != null
                && clickedBlockType.isInteractable()
                && !passesFoodClickToItem(clickedBlockType)
                && !(isGameMasterBlock(clickedBlockType) && !canUseGameMasterBlocks);
    }

    /**
     * Reports whether a block is a command, structure or jigsaw block. Vanilla opens one of these
     * only for a player who can use game-master blocks, and passes anyone else's click on to the
     * held item, which would eat a vanilla food.
     */
    static boolean isGameMasterBlock(Material blockType) {
        return GAME_MASTER_BLOCKS.contains(blockType);
    }

    /**
     * Reports whether a player may open a command, structure or jigsaw block. Vanilla requires
     * creative mode and operator permission level 2. Bukkit exposes only whether a player is an
     * operator, so an operator whose {@code ops.json} level is below 2 counts as able to.
     */
    static boolean canUseGameMasterBlocks(GameMode gameMode, boolean op) {
        return gameMode == GameMode.CREATIVE && op;
    }

    /**
     * Reports whether a block's use is limited to items a food is not, such as a lead on a fence.
     *
     * {@link Material#isInteractable()} only says a block has some use. For these the click goes
     * on to the held item, which would eat a vanilla food. Were the meal not started here, the
     * head would be offered for placement, {@link PlaceListener} would refuse it, and the click
     * would do nothing. Iron doors and trapdoors open only by redstone. Fences and candles are
     * matched by name because {@code Tag.FENCES} and {@code Tag.CANDLES} need a server.
     * A player leading a mob is the exception for a fence; see {@link #isLeadingMob}.
     */
    static boolean passesFoodClickToItem(Material blockType) {
        return BLOCKS_THAT_PASS_ON_A_FOOD.contains(blockType) || isFence(blockType) || isCandle(blockType);
    }

    /**
     * Reports whether a block is a candle, which reacts only to an empty hand, flint and steel,
     * a fire charge or another candle. A candle cake is not one: it is eaten as a cake.
     */
    static boolean isCandle(Material blockType) {
        return blockType != null
                && (blockType == Material.CANDLE || blockType.name().endsWith("_CANDLE"));
    }

    /**
     * Reports whether a block whose use depends on its state has none for a food in the state it
     * is in, so that the click goes on to the held item, which would eat a vanilla food.
     *
     * An uncharged respawn anchor takes only glowstone. A sweet berry bush below age 2 has no
     * berries to pick, and cave vines without glow berries have none either. Once charged or
     * ripe, each keeps the click: the anchor sets a spawn point or explodes, and the berries
     * are picked. {@code data} is null for a click in the air.
     */
    static boolean passesFoodClickInItsState(BlockData data) {
        if (data instanceof RespawnAnchor) {
            return ((RespawnAnchor) data).getCharges() == 0;
        }
        if (data instanceof CaveVinesPlant) {
            return !((CaveVinesPlant) data).isBerries();
        }
        if (data instanceof Ageable && data.getMaterial() == Material.SWEET_BERRY_BUSH) {
            return ((Ageable) data).getAge() < 2;
        }
        return false;
    }

    /** Reports whether a block is a fence, which a gate is not. */
    static boolean isFence(Material blockType) {
        return blockType != null && blockType.name().endsWith("_FENCE");
    }

    /**
     * Reports whether a mob is on this player's lead. Clicking a fence ties it up whatever is in
     * the player's hand, so that click is not free to become a meal.
     */
    private static boolean isLeadingMob(Player player) {
        for (Entity entity : player.getNearbyEntities(LEAD_REACH_BLOCKS, LEAD_REACH_BLOCKS, LEAD_REACH_BLOCKS)) {
            if (entity instanceof LivingEntity) {
                LivingEntity mob = (LivingEntity) entity;
                if (mob.isLeashed() && player.equals(mob.getLeashHolder())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Material clickedType(PlayerInteractEvent event) {
        return event.getClickedBlock() == null ? null : event.getClickedBlock().getType();
    }

    private static BlockData clickedData(PlayerInteractEvent event) {
        return event.getClickedBlock() == null ? null : event.getClickedBlock().getBlockData();
    }
}
