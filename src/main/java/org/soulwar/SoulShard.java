package org.soulwar;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SoulShard extends AbstractItem{
    private final SoulWars pluginInstance;
    public Advancement advancement;
    public String effectTrigger;
    public String effect;
    public SoulShard(SoulWars pluginInstance){
        super(pluginInstance, "SOUL_SHARD", Material.ENCHANTED_BOOK);
        this.pluginInstance = pluginInstance;

    }
    @Override
    protected ItemStack generateItem(ItemStack itemStack, Player player) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        String name = "Isso é um bug, reporte-o por favor no nosso Discord";
        if(Math.random() <= 0.3 || getPlayerAdvancements(player).isEmpty()){
            boolean removed = false;
            for (String trait : TraitsManager.traitID) {
                Bukkit.getLogger().info( trait);
                if(player.hasMetadata(trait)){
                    Bukkit.getLogger().info("trait " + trait);
                    List<String> lore = new ArrayList<>();
                    List<String> effects = (List<String>) player.getMetadata(trait).get(0).value();
                    if(!effects.isEmpty()){
                        effectTrigger = trait;
                        effect = effects.get(0);
                        lore.add( effect+" - " +trait);
                        name = ChatColor.AQUA + pluginInstance.getText(trait) + ChatColor.RESET +
                                " - " + ChatColor.GREEN + pluginInstance.getText(effect);
                        effects.remove(0);
                        player.removeMetadata(trait, pluginInstance);

                        if(!effects.isEmpty()) {
                            player.setMetadata(trait, new FixedMetadataValue(pluginInstance, effects));
                            pluginInstance.playerSouls.set(player.getName() + ":" + player.getUniqueId()
                                    + "." + trait, effects);
                        }else{

                            pluginInstance.playerSouls.set(player.getName()+":"+player.getUniqueId() + "."+trait, "");}
                        itemMeta.setLore(lore);
                        pluginInstance.saveSouls();
                        removed = true;
                        Bukkit.broadcastMessage(player.getName() + " perdeu o traço "+ name);
                        break;
                    }

                }
            }
            if(!removed) name = removeAdvancement(player, itemMeta);
        }else name = removeAdvancement(player, itemMeta) ;

        itemMeta.setDisplayName(name);
        itemMeta.setUnbreakable(true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        ((Damageable)itemMeta).setDamage(1);
        itemMeta.setUnbreakable(true);
        itemStack.setItemMeta(itemMeta);
        pluginInstance.events.updateScoreBoard(player);
        if(getPlayerTraitAmount(player) <= 0 && getPlayerAdvancements(player).isEmpty()){
            player.setGameMode(GameMode.SPECTATOR);
            List<String> killed = (List<String>) pluginInstance.playerSouls.getList("killed");
            if(null == killed) killed = new ArrayList<>();
            killed.add(player.getName()+":"+player.getUniqueId());
            pluginInstance.playerSouls.set("killed", killed);
            pluginInstance.playerProgression.set(player.getName()+":"+player.getUniqueId() + ".gone", true);
            pluginInstance.saveSouls();
            pluginInstance.saveProgression();
        }
        return itemStack;
    }
    private List<Integer> getPlayerAdvancements(Player player){
        List<Integer> advancements = new ArrayList<>();
        for (SoulWars.advancements value : SoulWars.advancements.values()) {
           if(pluginInstance.challengerManager.advancementMap.get(value.getIndex()).isGranted(player))
               advancements.add(value.getIndex());
        }
        return advancements;
    }
    private String removeAdvancement(Player player, ItemMeta itemMeta){

        List<Integer> advancements = getPlayerAdvancements(player);
        if(advancements.isEmpty()) return "Traço Invalido";
        int removal = (int) Math.round(Math.random()*(advancements.size()-1));
        int index = advancements.get(removal);
        advancement = pluginInstance.challengerManager.advancementMap.get(index);
        List<String> lore = new ArrayList<>();
        lore.add(advancement.getDisplay().getTitle() +" - " +index);
        pluginInstance.revokeAvancement(index, player);
        String adPath = "given"+ index;
        pluginInstance.getConfig().set(adPath, "-1");
        pluginInstance.saveConfig();
        //update scoreboard
        itemMeta.setLore(lore);
        return  ChatColor.DARK_PURPLE + pluginInstance.getText("t"+index);
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
        if(getPlayerTraitAmount(player) >= 5) {
            player.sendMessage(ChatColor.BOLD + "[SoulWars] "+ChatColor.RESET + "Você não pode ter mais que 5 traços.");
            return;
        }
        List<String> lore = itemStack.getItemMeta().getLore();
        lore.forEach(s -> {
                String[] data = s.split(" - ");

                if(data.length > 1){

                        if(isNumeric(data[1]) && getPlayerAdvancements(player).size() < 2){
                            int advancementId = Integer.parseInt(data[1]);
                            Advancement advancement1 = pluginInstance.challengerManager.advancementMap.get
                                    (advancementId);
                          //  Bukkit.getLogger().info("Advancement " + advancementId +advancement1.getDisplay().getTitle());
                            pluginInstance.grantAdvancement(advancementId, player);
                        }else if(!isNumeric(data[1])){
                            String playerPath = player.getName() + ":" + player.getUniqueId();
                            List<String>  playerTraits =  (List<String>)
                                    pluginInstance.playerSouls.getList(playerPath + "." + data[1]);
                            if(null == playerTraits ) playerTraits = new ArrayList<>();
                            playerTraits.add(data[0]);
                            pluginInstance.playerSouls.set(playerPath+"."+data[1], playerTraits);
                       //     Bukkit.getLogger().info("Adding " + data[0] + " "+ data[1]);
                           String name = ChatColor.AQUA + pluginInstance.getText(data[1]) + ChatColor.RESET +
                                    " - " + ChatColor.GREEN + pluginInstance.getText(data[0]);
                            Bukkit.broadcastMessage(player.getName() + " ganhou o traço "+ name);
                            pluginInstance.saveSouls();
                        }else if(getPlayerAdvancements(player).size() >= 2){
                            player.sendMessage(ChatColor.BOLD + "[SoulWars] "+ChatColor.RESET + "Você não pode ter mais que 2 traços lendários.");
                            return;
                        }
                        player.getInventory().setItemInMainHand(null);
                        
                        //remember to make it not be unlockable after becoming a item


                }else player.sendMessage(ChatColor.BOLD + "[SoulWars] "+ChatColor.RESET + "Isso é um bug, reporte o no nosso Discord por favor.");

            });
        pluginInstance.events.updateScoreBoard(player);



      //  player.getWorld().strikeLightning(player.getLocation());
    }
    @EventHandler
    private void itemDespawn(ItemDespawnEvent e){
        if(!isApplicable(e.getEntity().getItemStack())) return;
        e.setCancelled(true);
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
