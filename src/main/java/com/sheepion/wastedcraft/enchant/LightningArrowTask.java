package com.sheepion.wastedcraft.enchant;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

/**
 * project name: WastedCraft
 * package: com.sheepion.wastedcraft.enchant
 *
 * @author Sheepion
 * @date 3/2/2022
 */
public class LightningArrowTask implements Runnable {
    private BukkitTask task;

    private Entity projectile;

    public LightningArrowTask(Entity projectile) {
        this.projectile = projectile;
    }

    public void setTask(BukkitTask task) {
        this.task = task;
    }

    @Override
    public void run() {
        //check hit ground or mob
        if (projectile.isOnGround() || projectile.isDead()) {
            projectile.getWorld().strikeLightning(projectile.getLocation());
            projectile.remove();
            task.cancel();
        //spawn particles while flying
        } else {
            projectile.getWorld().spawnParticle(Particle.ASH, projectile.getLocation(), 16, 0.1, 0.1, 0.1);
        }
    }
}

