package io.github.dschonas04.fishr.fish;

import io.github.dschonas04.fishr.Fishr;
import io.github.dschonas04.fishr.item.BattleEquipment;
import io.github.dschonas04.fishr.item.FishrRodItem;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * Ein Fisch bemerkt eine Pose in der Nähe, schwimmt hin, sieht sich den Köder an und beißt, wenn er
 * ihm gefällt. Der Angler hat dann das Vanilla-Bissfenster zum Anschlagen; der Drill läuft gegen
 * genau diesen Fisch. Ein Fisch, dem der Köder egal ist, beachtet ihn nicht; einer, der nur halb
 * interessiert ist, prüft ihn und dreht ab und zu ab. Ohne Anschlag verliert der Fisch das
 * Interesse und schwimmt davon.
 */
final class BiteHookGoal extends Goal {
    private static final double RANGE = 12;
    private final FishrFishEntity fish;
    private FishingHook target;
    private int ticks;
    private int afterBite;
    private int cooldown;
    private int inspect;
    private double appeal;

    BiteHookGoal(FishrFishEntity fish) {
        this.fish = fish;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (fish.isBattleDummy()
                || fish.specimen().species().boss() != null
                || --cooldown > 0
                || fish.getRandom().nextInt(reducedTickDelay(20)) != 0) return false;
        target =
                fish
                        .level()
                        .getEntitiesOfClass(
                                FishingHook.class,
                                fish.getBoundingBox().inflate(RANGE),
                                this::interesting)
                        .stream()
                        .min(Comparator.comparingDouble(fish::distanceToSqr))
                        .orElse(null);
        if (target == null) return false;
        // Der Köder entscheidet: Ein beliebter Köder lockt den Fisch an, ein unbeliebter wird nicht
        // beachtet,
        // ein blanker Haken nur manchmal.
        appeal = appeal(target.getPlayerOwner());
        if (appeal < 0.5 && fish.getRandom().nextFloat() > appeal) {
            cooldown = 100;
            return false;
        }
        return Fishr.BATTLES.claimBite(target, fish);
    }

    private boolean interesting(FishingHook hook) {
        return hook.isAlive()
                && hook.isInWater()
                && hook.getPlayerOwner() != null
                && hook.getHookedIn() == null
                && !Fishr.BATTLES.isBattling(hook)
                && !Fishr.BATTLES.hasBiter(hook);
    }

    private double appeal(Player player) {
        ItemStack rod =
                player.getMainHandItem().getItem() instanceof FishingRodItem
                        ? player.getMainHandItem()
                        : player.getOffhandItem();
        ItemStack other =
                rod == player.getMainHandItem()
                        ? player.getOffhandItem()
                        : player.getMainHandItem();
        ItemStack bait =
                !FishrRodItem.bait(rod).isEmpty()
                        ? FishrRodItem.bait(rod)
                        : BattleEquipment.isBait(other) ? other : ItemStack.EMPTY;
        long time = Math.floorMod(fish.level().getOverworldClockTime(), 24000L);
        return BattleEquipment.appeal(
                bait, rod, fish.specimen().species(), time >= 13000 && time < 23000);
    }

    @Override
    public void start() {
        ticks = 0;
        afterBite = -1;
        inspect = -1;
        fish.getNavigation().moveTo(target.getX(), target.getY() - 0.4, target.getZ(), 1.4);
    }

    @Override
    public boolean canContinueToUse() {
        return target != null
                && target.isAlive()
                && target.isInWater()
                && !fish.isBattleDummy()
                && ticks < 400
                && afterBite != 0
                && fish.distanceToSqr(target) < (RANGE + 4) * (RANGE + 4)
                && !Fishr.BATTLES.isBattling(target);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (target == null || !target.isAlive()) return;
        ticks++;
        fish.getLookControl().setLookAt(target.getX(), target.getY(), target.getZ());
        double distance = fish.distanceToSqr(target.getX(), target.getY() - 0.4, target.getZ());
        if (afterBite < 0) {
            if (distance > 1.6) {
                if (ticks % 10 == 0)
                    fish.getNavigation()
                            .moveTo(target.getX(), target.getY() - 0.4, target.getZ(), 1.4);
                return;
            }
            fish.getNavigation().stop();
            // Ein halb interessierter Fisch sieht sich den Köder kurz an und dreht vielleicht ab
            // (er kann später
            // wiederkommen).
            if (inspect < 0) inspect = 20 + fish.getRandom().nextInt(30);
            if (inspect-- > 0) {
                fish.setDeltaMovement(
                        (target.getX() - fish.getX()) * 0.05,
                        0,
                        (target.getZ() - fish.getZ()) * 0.05);
                return;
            }
            if (fish.getRandom().nextDouble() > Math.min(0.95, appeal)) {
                afterBite = 0;
                return;
            }
            ((BiteTarget) target).fishr$bite(40);
            afterBite = 45;
        } else {
            // Er hat den Köder genommen und schwimmt damit hinaus ins offene Wasser: Die Pose wird
            // vom Ufer
            // weggezogen.
            Player owner = target.getPlayerOwner();
            var away =
                    owner == null
                            ? net.minecraft.world.phys.Vec3.ZERO
                            : target.position()
                                    .subtract(owner.position())
                                    .multiply(1, 0, 1)
                                    .normalize()
                                    .scale(0.06);
            var next = target.position().add(away);
            if (fish.level()
                    .getFluidState(
                            net.minecraft.core.BlockPos.containing(
                                    next.x, target.getY() - 0.3, next.z))
                    .is(net.minecraft.tags.FluidTags.WATER)) {
                target.setDeltaMovement(target.getDeltaMovement().add(away.scale(0.5)));
            }
            fish.setDeltaMovement(
                    (target.getX() + away.x * 4 - fish.getX()) * 0.3,
                    (target.getY() - 0.45 - fish.getY()) * 0.15,
                    (target.getZ() + away.z * 4 - fish.getZ()) * 0.3);
            fish.getLookControl()
                    .setLookAt(fish.getX() + away.x * 10, fish.getY(), fish.getZ() + away.z * 10);
            afterBite--;
        }
    }

    @Override
    public void stop() {
        if (target != null) Fishr.BATTLES.releaseBite(target, fish);
        target = null;
        cooldown = 200 + fish.getRandom().nextInt(200);
    }
}
