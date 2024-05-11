package org.soulwar;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.item.ItemBuilder;
import com.samjakob.spigui.menu.SGMenu;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import com.samjakob.spigui.SpiGUI;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ReviveBeacon extends AbstractItem{
    private final SoulWars pluginInstance;
    public static SpiGUI spiGUI;
    public SGMenu menu;
    public SGMenu newMenu;
    public SGMenu confirmation;
    private ItemStack selectedSacrifice;
    private OfflinePlayer selectedPlayer;
    public ReviveBeacon(SoulWars pluginInstance){
        super(pluginInstance, "REVIVE_BEACON", Material.BEACON);
        this.pluginInstance = pluginInstance;
        spiGUI = new SpiGUI(pluginInstance);
    }
    @Override
    protected ItemStack generateItem(ItemStack itemStack, Player player) {
        return itemStack;
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        Player player = e.getPlayer();
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if(!isApplicable(itemStack) ) return;
        if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() ==Action.RIGHT_CLICK_BLOCK) {
            createInventory(player);

        }
        e.setCancelled(true);
    }
    private void createInventory(Player player){

        AtomicInteger slotID = new AtomicInteger(2);
        menu = spiGUI.create("Qual será seu sacríficio?", 1);
        AtomicInteger traitss = new AtomicInteger();
        for (String trait : TraitsManager.traitID) {
            if(player.hasMetadata(trait)){
                List<String> lore = new ArrayList<>();
                List<String> effects = (List<String>) player.getMetadata(trait).get(0).value();
                if(!effects.isEmpty()){
                    Material icon = Material.ENCHANTED_BOOK;
                    SGButton button;
                    ItemStack itemIcon = new ItemBuilder(icon).name(ChatColor.LIGHT_PURPLE + effects.get(0)).build();
                    ItemMeta itemMeta = itemIcon.getItemMeta();
                    lore.add( effects.get(0) +" - " +trait);
                    itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    itemMeta.setLore(lore);
                    itemIcon.setItemMeta(itemMeta);
                    button =  new SGButton(itemIcon).withListener((InventoryClickEvent event) -> {
                        selectedSacrifice = itemIcon;
                        selectPlayerToRevive( player);

                    });
                    traitss.getAndIncrement();
                    menu.setButton(slotID.get(), button);
                    slotID.getAndIncrement();
                }

            }
        }
        List<Integer> advancements = getPlayerAdvancements(player);

        if(!advancements.isEmpty()){
            advancements.forEach(index -> {

                Advancement advancement = pluginInstance.challengerManager.advancementMap.get(index);
                List<String> lore = new ArrayList<>();
                lore.add(advancement.getDisplay().getTitle() +" - " +index);
                Material icon = Material.ENCHANTED_BOOK;
                SGButton button;
                ItemStack itemIcon = new ItemBuilder(icon).name(ChatColor.LIGHT_PURPLE + advancement.getDisplay().getTitle()).build();
                ItemMeta itemMeta = itemIcon.getItemMeta();
                itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemMeta.setLore(lore);
                itemIcon.setItemMeta(itemMeta);
                button =  new SGButton(itemIcon).withListener((InventoryClickEvent event) -> {
                    selectedSacrifice = itemIcon;
                    selectPlayerToRevive( player);
                });
                menu.setButton(slotID.get(), button);
                slotID.getAndIncrement();
                traitss.getAndIncrement();
            });


        }
        if(traitss.get() > 1) {
            player.openInventory(menu.getInventory());
        }else{
            player.sendMessage(ChatColor.BOLD + "[SoulWars] "+ChatColor.RESET + " Você precisa ter mais do que 1 traço para reviver alguém.");
            player.closeInventory();
            return;
        }
    }
    private void selectPlayerToRevive( Player player){
        player.closeInventory();

        newMenu =spiGUI.create("Escolha quem será revivido?", 6);
        List<String> players = (List<String>) pluginInstance.playerSouls.getList("killed");
        AtomicInteger slotID = new AtomicInteger(11);
        Bukkit.getLogger().info(players.toString());
        if(null == players || players.isEmpty()){
            player.sendMessage(ChatColor.BOLD + "[SoulWars] "+ChatColor.RESET + " Não há nenhum jogador para ser revivido.");
            player.closeInventory();
            return;
        }else{
            players.forEach(s -> {
                String[] uuidS = s.split(":");
                String nick = uuidS[0];
                UUID uuid = UUID.fromString(uuidS[1]);
                Material icon = Material.PLAYER_HEAD;
                SGButton button;
                ItemStack itemIcon = new ItemBuilder(icon).name(ChatColor.RED + nick).build();
                SkullMeta itemMeta =(SkullMeta) itemIcon.getItemMeta();
                itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                selectedPlayer = Bukkit.getOfflinePlayer(uuid);
                itemMeta.setOwningPlayer(selectedPlayer);
                itemIcon.setItemMeta(itemMeta);
                button =  new SGButton(itemIcon).withListener((InventoryClickEvent event) -> {
                        createConfirmPage(player, itemIcon);

                });
                slotID.getAndIncrement();
                newMenu.setButton(slotID.get(), button);
            });

        }
        player.openInventory(newMenu.getInventory());
     }
    private void createConfirmPage(Player player, ItemStack head){
        confirmation =  spiGUI.create("", 1);
        confirmation.setName("Reviver " + selectedPlayer.getName()+"?");
        SGButton back = new SGButton(

                new ItemBuilder(Material.COAL).name("Cancelar").build()
        ).withListener((InventoryClickEvent event) -> {


            player.openInventory(newMenu.getInventory());
        });
        SGButton neutral = new SGButton(head);
        SGButton confirm = new SGButton(
                new ItemBuilder(Material.DIAMOND).name("Confirmar").build()
        ).withListener((InventoryClickEvent event) -> {
            moveTraits(player);
            revokeKill();
            player.closeInventory();
            player.playSound(player, Sound.BLOCK_PORTAL_TRIGGER, 1, 1);
            player.spawnParticle(Particle.FLASH, player.getLocation(), 50);

        });

        confirmation.setButton(0,3,back);
        confirmation.setButton(0,4,neutral);
        confirmation.setButton(0,5,confirm);
        player.openInventory(confirmation.getInventory());
    }
    public void moveTraits(Player player){
        List<String> lore = selectedSacrifice.getItemMeta().getLore();
        lore.forEach(s -> {
            String[] data = s.split(" - ");
            if(data.length > 1){
                    if(isNumeric(data[1])){
                        int advancementId = Integer.parseInt(data[1]);
                        Advancement advancement1 = pluginInstance.challengerManager.advancementMap.get
                                (advancementId);
                        Bukkit.getLogger().info("Advancement " + advancementId +advancement1.getDisplay().getTitle());
                        pluginInstance.revokeAvancement(advancementId, player);
                        pluginInstance.playerProgression.set(selectedPlayer.getName()+":"+selectedPlayer.getUniqueId() + ".pendingAdvancement", advancementId);

                    }else if(!isNumeric(data[1])){
                        String playerPath = player.getName() + ":" + player.getUniqueId();
                        List<String>  playerTraits =  (List<String>)
                                pluginInstance.playerSouls.getList(playerPath + "." + data[1]);
                        if(null == playerTraits ) playerTraits = new ArrayList<>();
                        if(playerTraits.contains(data[0])) playerTraits.remove(data[0]);
                        pluginInstance.playerSouls.set(playerPath+"."+data[1], playerTraits);
                        player.removeMetadata(data[0], pluginInstance);
                        player.setMetadata(data[0], new FixedMetadataValue(pluginInstance, playerTraits));
                        String player2Path = selectedPlayer.getName() + ":" + selectedPlayer.getUniqueId();
                        playerTraits =  (List<String>)
                                pluginInstance.playerSouls.getList(player2Path + "." + data[1]);
                        if(null == playerTraits ) playerTraits = new ArrayList<>();
                        playerTraits.add(data[0]);
                        pluginInstance.playerSouls.set(player2Path+"."+data[1], playerTraits);
                        pluginInstance.saveSouls();
                    }

            }


        });
    }

    private void revokeKill(){
        try{
            selectedPlayer.getPlayer().setGameMode(GameMode.SURVIVAL);
        }catch (Exception ignored){}
        pluginInstance.playerProgression.set(selectedPlayer.getName()+":"+selectedPlayer.getUniqueId() + ".gone", false);
        List<String> players = (List<String>) pluginInstance.playerSouls.getList("killed");
        if(null != players && !players.isEmpty()){
            players.remove(selectedPlayer.getName()+":"+selectedPlayer.getUniqueId());
            pluginInstance.playerProgression.set("killed", players);
        }
        pluginInstance.saveProgression();
    }
    private List<Integer> getPlayerAdvancements(Player player){
        List<Integer> advancements = new ArrayList<>();
        for (SoulWars.advancements value : SoulWars.advancements.values()) {
            if(pluginInstance.challengerManager.advancementMap.get(value.getIndex()).isGranted(player))
                advancements.add(value.getIndex());
        }
        return advancements;
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

