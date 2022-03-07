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

public class TrackingArrowTask implements Runnable {
    private BukkitTask task;

    private Entity projectile;

    public TrackingArrowTask(Entity projectile) {
        this.projectile = projectile;
    }

    public void setTask(BukkitTask task) {
        this.task = task;
    }

    @Override
    public void run() {
        if (projectile.isOnGround() || projectile.isDead()) {
            projectile.remove();
            task.cancel();
        } else {
            projectile.getWorld().spawnParticle(Particle.ASH, projectile.getLocation(), 16, 0.1, 0.1, 0.1);
            Location targetLocation = null;
            for (Entity e : projectile.getNearbyEntities(10, 10, 10)) {
                if (!(e instanceof Mob)) {
                    continue;
                }
                Location eLocation = e.getLocation().add(0, 0.6, 0);
                //find nearest mob
                if (targetLocation == null || eLocation.distance(projectile.getLocation()) < targetLocation.distance(projectile.getLocation())) {
                    Vector temp = new Vector(eLocation.getX() - projectile.getLocation().getX(), eLocation.getY() - projectile.getLocation().getY(), eLocation.getZ() - projectile.getLocation().getZ());
                    //make sure no block between the projectile and the target
                    if (projectile.getWorld().rayTraceBlocks(projectile.getLocation(), temp, projectile.getLocation().distance(eLocation)) == null) {
                        targetLocation = eLocation;
                    }
                }
            }
            if (targetLocation == null) {
                return;
            }
            Location currentLocation = projectile.getLocation();
            Vector v = new Vector(targetLocation.getX() - currentLocation.getX(), targetLocation.getY() - currentLocation.getY(), targetLocation.getZ() - currentLocation.getZ());
            Vector newV=projectile.getVelocity().add(v.normalize().multiply(0.3f)).normalize().multiply(1.2f);
            if(Math.random()<0.1){
                newV.add(new Vector(Math.random()-0.5,Math.random()-0.5,Math.random()-0.5).normalize().multiply(Math.random()*0.6));
            }
            projectile.setVelocity(newV);
        }
    }
}
