package org.soulwar;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SoulShard extends AbstractItem{
    private final SoulWars pluginInstance;
    public Advancement advancement;
    public String effectTrigger;
    public String effect;
    public SoulShard(SoulWars pluginInstance){
        super(pluginInstance, "SOUL_SHARD", Material.WOODEN_HOE);
        this.pluginInstance = pluginInstance;

    }
    @Override
    protected ItemStack generateItem(ItemStack itemStack, Player player) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        String name = ChatColor.translateAlternateColorCodes('&', "&eTraço");
        List<Integer> advancements = getPlayerAdvancements(player);
        if(!advancements.isEmpty() && Math.random() < 0.7){
            int removal = (int) Math.round(Math.random()*(advancements.size()-1));
            int index = advancements.get(removal);
            advancement = pluginInstance.challengerManager.advancementMap.get(index);
            List<String> lore = new ArrayList<>();
            lore.add(advancement.getDisplay().getTitle() +" - " +index);
            pluginInstance.revokeAvancement(index, player);
            //update scoreboard
            itemMeta.setLore(lore);
        }else{
            for (String trait : TraitsManager.traitID) {
                if(player.hasMetadata(trait)){
                    List<String> lore = new ArrayList<>();
                    List<String> effects = (List<String>) player.getMetadata(trait).get(0).value();
                    if(!effects.isEmpty()){
                        effectTrigger = trait;
                        effect = effects.get(0);
                        lore.add( effect+" - " +trait);
                        effects.remove(0);
                        pluginInstance.playerSouls.set(player.getName() + ":" + player.getUniqueId()
                                +"."+trait, effects);
                        itemMeta.setLore(lore);
                        pluginInstance.saveSouls();
                        break;
                    }

                }
            }
        }

        itemMeta.setDisplayName(name);
     //   itemMeta.setUnbreakable(true);
     //   itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        ((Damageable)itemMeta).setDamage(1);
        itemMeta.setUnbreakable(true);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }
    public List<Integer> getPlayerAdvancements(Player player){
        List<Integer> advancements = new ArrayList<>();
        for (SoulWars.advancements value : SoulWars.advancements.values()) {
           if(pluginInstance.challengerManager.advancementMap.get(value.getIndex()).isGranted(player))
               advancements.add(value.getIndex());
        }
        return advancements;
    }
    public int getPlayerTraitAmount(Player player){
        int ret  = getPlayerAdvancements(player).size();
        for (String s : TraitsManager.traitID) {
            if(player.hasMetadata(s)){
                List<String> e = (List<String>)player.getMetadata(s).get(0).value();
                ret += e.size();
            }
        }
        return ret;
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        Player player = e.getPlayer();
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if(!isApplicable(itemStack)) return;

            List<String> lore = itemStack.getItemMeta().getLore();
            lore.forEach(s -> {
                String[] data = s.split(" - ");
                if(data.length > 1){
                    if(getPlayerTraitAmount(player) < 5){
                        if(isNumeric(data[1]) && getPlayerAdvancements(player).size() < 2){
                            int advancementId = Integer.parseInt(data[1]);
                            Advancement advancement1 = pluginInstance.challengerManager.advancementMap.get
                                    (advancementId);
                            Bukkit.getLogger().info("Advancement " + advancementId +advancement1.getDisplay().getTitle());
                            pluginInstance.grantAdvancement(advancementId, player);
                        }else{
                            String playerPath = player.getName() + ":" + player.getUniqueId();
                            List<String>  playerTraits =  (List<String>)
                                    pluginInstance.playerSouls.getList(playerPath + "." + data[1]);
                            if(null == playerTraits ) playerTraits = new ArrayList<>();
                            playerTraits.add(data[0]);
                            pluginInstance.playerSouls.set(playerPath+"."+data[1], playerTraits);
                            Bukkit.getLogger().info("Adding " + data[0] + " "+ data[1]);
                            pluginInstance.saveSouls();
                        }
                        player.getInventory().setItemInMainHand(null);
                        //remember to make it not be unlockable after becoming a item
                    }

                }
            });




      //  player.getWorld().strikeLightning(player.getLocation());
    }
    public static boolean isNumeric(String strNum) { //Thx https://www.baeldung.com/java-check-string-number
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
