package org.soulwar;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Events implements Listener{
    private final SoulWars pluginInstance = (SoulWars)
            Bukkit.getPluginManager().getPlugin("SoulWars");
    FileConfiguration playerSouls;
    private String[] parseTrait(String trait){

        if(trait.contains(":")) return  trait.split(":");
        return new String[]{"", ""};
    }
    @EventHandler
    public  void onPlayerJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        assert pluginInstance != null;
        initPlayer(player, pluginInstance);


    }
    private void initPlayer(Player player, SoulWars pluginInstance){
        playerSouls = pluginInstance.getPlayerSouls();
        pluginInstance.advancementTab.showTab(player);
        String playerPath = player.getName() + ":" + player.getUniqueId();
        if(!playerSouls.contains(playerPath)){
            pluginInstance.resetPlayer(player);
        }
        if(!playerSouls.contains(playerPath+".played") || true ||
                !Boolean.valueOf( playerSouls.get(playerPath+".played").toString())){
            Map<String, List<String>> traits =  TraitsManager.generateRandomPowers(4, pluginInstance);
            for(String trait: traits.keySet()){
                playerSouls.set(playerPath+"."+trait, traits.get(trait));
            }
            playerSouls.set(playerPath+".played", true);
            pluginInstance.saveSouls();
        }
        TraitsManager.readTraits(playerSouls, player, pluginInstance);
        //here we'll enable the permanent effects on the player
        BukkitRunnable bukkitRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                if(!player.isOnline()){
                    this.cancel();
                    return;
                }
                if(player.hasMetadata("ep") )applyEffects (player, player.getMetadata("ep").get(0).value());


                if(player.hasMetadata("dd"))
                    if(util_isDay(player)) applyEffects(player, player.getMetadata("dd").get(0).value());
                if(player.hasMetadata("dn"))
                    if(!util_isDay(player)) applyEffects(player, player.getMetadata("dn").get(0).value());
                if(player.hasMetadata("nn"))
                    if(player.getWorld().getEnvironment().equals(World.Environment.NETHER))
                        applyEffects(player, player.getMetadata("nn").get(0).value());
                if(player.hasMetadata("ne"))
                    if(player.getWorld().getEnvironment().equals(World.Environment.THE_END))
                        applyEffects(player, player.getMetadata("ne").get(0).value());
                if(player.hasMetadata("no"))
                    if(player.getWorld().getEnvironment().equals(World.Environment.NORMAL))
                        applyEffects(player, player.getMetadata("no").get(0).value());
                if(player.hasMetadata("na"))
                    if( (player.getLocation().getBlock().getType() == Material.WATER))
                        applyEffects(player, player.getMetadata("na").get(0).value());
                if(player.hasMetadata("nl"))
                    if( (player.getLocation().getBlock().getType() == Material.LAVA))
                        applyEffects(player, player.getMetadata("na").get(0).value());
            }
        };
        bukkitRunnable.runTaskTimer(pluginInstance, 0, 120L);
    }
    private void applyEffects(Player player, Object o_effects){
        List<String> effects = (List<String>) o_effects;
        effects.forEach(s -> {
            PotionEffectType potionEffectType = TraitsManager.stringToEffect.get(s);
            player.addPotionEffect(new PotionEffect(potionEffectType, 2400,
                    1, false, false,true ));
        });
    }
    public boolean util_isDay(Player player) {

        long time = player.getWorld().getTime();
        return time < 12300 || time > 23850;

    }
}
