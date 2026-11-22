package dev.sowik.quietworld;

import net.minecraft.core.BlockPos;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** The fake player. Either hangs in the sky watching, or stands nearby, hands over an item, and can be questioned. */
public class KosiwEntity extends PathfinderMob {
    public static final String NAME = "kosiw";

    public enum Mode { SKY, VISIT }

    private Mode mode = Mode.VISIT;
    private int life;
    private int dropAt = 120;
    private int stayTicks = 1500;
    private boolean dropped;

    public KosiwEntity(EntityType<? extends KosiwEntity> type, Level level) {
        super(type, level);
        this.setCustomName(Component.literal(NAME));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    public void setMode(Mode newMode) {
        this.mode = newMode;
        if (newMode == Mode.SKY) {
            this.setNoGravity(true);
            this.stayTicks = 500 + this.random.nextInt(400);
        } else {
            this.dropAt = 100 + this.random.nextInt(120);
        }
    }

    public Mode getMode() {
        return mode;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && super.hurt(source, amount);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!this.level().isClientSide && player instanceof ServerPlayer sp && mode == Mode.VISIT) {
            Dialogue.showMenu(sp);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        Codex.encounter(this, "mob_kosiw");

        life++;
        Player nearest = this.level().getNearestPlayer(this, 128.0D);
        if (!(nearest instanceof ServerPlayer player)) {
            if (life > 100) discard();
            return;
        }

        this.lookAt(EntityAnchorArgument.Anchor.EYES, player.getEyePosition());
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();

        if (mode == Mode.SKY) {
            this.setDeltaMovement(Vec3.ZERO);
            if (life > stayTicks) vanish(player);
            return;
        }

        if (!dropped && life >= dropAt) {
            dropped = true;
            dropGift(player);
        }
        if (life > stayTicks) vanish(player);
    }

    private void vanish(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable("multiplayer.player.left", Component.literal(NAME))
                .withStyle(ChatFormatting.YELLOW));
        discard();
    }

    private void dropGift(ServerPlayer player) {
        RandomSource rnd = this.random;
        ItemStack stack = null;
        for (int attempt = 0; attempt < 4 && stack == null; attempt++) {
            switch (rnd.nextInt(4)) {
                case 0:
                    stack = new ItemStack(Items.DIAMOND, 1 + rnd.nextInt(4));
                    break;
                case 1:
                    stack = new ItemStack(Items.COOKED_BEEF, 2);
                    break;
                case 2:
                    stack = Corruption.makeBook(rnd);
                    break;
                default:
                    stack = coordinatePaper(player);
                    break;
            }
        }
        if (stack == null) {
            stack = new ItemStack(Items.COOKED_BEEF, 2);
        }

        ItemEntity item = new ItemEntity(this.level(), this.getX(), this.getY() + 0.8D, this.getZ(), stack);
        item.setDeltaMovement((player.getX() - this.getX()) * 0.04D, 0.2D, (player.getZ() - this.getZ()) * 0.04D);
        item.setDefaultPickUpDelay();
        this.level().addFreshEntity(item);

        Scheduler.later(10, () -> Dialogue.say(player, "take it."));
    }

    /** A paper whose name is the coordinates of a freshly built ritual site. Null if no site could be built. */
    private ItemStack coordinatePaper(ServerPlayer player) {
        BlockPos pos = Structures.spawnNear(player, 130, 260, false);
        if (pos == null) return null;
        ItemStack paper = new ItemStack(Items.PAPER);
        paper.setHoverName(Component.literal("X: " + pos.getX() + "  Y: " + pos.getY() + "  Z: " + pos.getZ()));
        return paper;
    }
}
