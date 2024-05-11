package org.soulwar;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class SoulWars extends JavaPlugin implements Listener {
    public ItemManager itemManager;
    private File playerSoulsFile;
    private File playerProgressionFile;
    private String langFile = "pt_br.yml";
    private File localizationFile;
    private FileConfiguration localization;
    public FileConfiguration playerSouls;
    public FileConfiguration playerProgression;
    public UltimateAdvancementAPI api ;

    public AdvancementTab advancementTab;
    public RootAdvancement root;
    private AdvancementMain main;
    public ChallengerManager challengerManager = new ChallengerManager();
    public  final int speedUUIDMOD = 3;
    public  final int strengthUUIDMOD = 4;
    public  final int resistanceUUIDMOD = 5;
    public  final int toughUUIDMOD = 6;
    public  final int attackSpeedUUIDMOD = 7;
    public  final int absorptionUUIDMOD = 2;
    public  List<String> badPotionEffects = new ArrayList<>();
    public static enum advancements{

     
        Cowboy(0),
        Nether(1),
        Void(2),
        Alquimista(3),
        Franco(4),
        Cabra(5),
        Shama(6),
        Oceano(7),
        Bomba(8),
        Lide(9),
        Vulto(10),
        Guerra(11);
        private int index;
        advancements(int i) {
            this.index = i;
        }
        public int getIndex(){
            return this.index;
        }
    }
    public Events events;
    @Override
    public void onLoad() {
        main = new AdvancementMain(this);
        main.load();

    }

    @Override
    public void onEnable(){

        main.enableSQLite(new File("advancements.db"));
        // After the initialisation of AdvancementMain you can get an instance of the UltimateAdvancementAPI class
        api = UltimateAdvancementAPI.getInstance(this);
        getLogger().info("Jovem, você tem que conquistar sua alma");
      //  getServer().getPluginManager().registerEvents(new Events(), this);
        getPlayerSoulsFile();
        getPlayerProgressionFile();
        readConfig();
        readLocalization();
        challengerManager.initializeAdvancements(this);
        Bukkit.getPluginManager().registerEvents(this, this);
        events = new Events();
        Bukkit.getPluginManager().registerEvents(events, this);
        this.getCommand("sw").setExecutor(new Commands());
        badPotionEffects.add("BAD_OMEN");
        badPotionEffects.add("INSTANT_DAMAGE");
        badPotionEffects.add("LONG_POISON");
        badPotionEffects.add("LONG_SLOWNESS");
        badPotionEffects.add("LONG_WEAKNESS");
        badPotionEffects.add("POISON");
        badPotionEffects.add("SLOWNESS");
        badPotionEffects.add("STRONG_HARMING");
        badPotionEffects.add("STRONG_POISO");
        badPotionEffects.add("STRONG_SLOWNESS");
        badPotionEffects.add("WEAKNESS");
        badPotionEffects.add("LEVITATION");
        itemManager = new ItemManager(this);
        ItemStack item = itemManager.getHandler(ReviveBeacon.class).getItem(null);
        NamespacedKey key = new NamespacedKey(this, "revivebeacon");
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape("DGD", "TBT", "DGD");
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('G', Material.GOLDEN_APPLE);
        recipe.setIngredient('B', Material.BEACON);
        recipe.setIngredient('T', Material.TOTEM_OF_UNDYING);
        Bukkit.addRecipe(recipe);
        BukkitRunnable bukkitRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                saveSouls();
                saveProgression();
                saveConfig();
            }
        };
        bukkitRunnable.runTaskTimer(this, 6000L, 6000L);
    }
    private void readLocalization(){
        localizationFile = new File(getDataFolder(), langFile);
        if(!localizationFile.exists()){
            getLogger().info("Arquivo "+langFile+" inexistente, criando um novo");
            getLogger().info("Criado: " + localizationFile.getParentFile().mkdirs());
            saveResource(langFile, false);
        }
        localization = new YamlConfiguration();
        try{
            localization.load(localizationFile);
        }catch (Exception e){
            getLogger().info( e.getMessage());
        }
    }
    private void readConfig(){
        saveDefaultConfig();
        langFile = Objects.requireNonNull(getConfig().get("lang")).toString()+".yml";
    }
    private void getPlayerProgressionFile(){
        playerProgressionFile = new File(getDataFolder(),"PlayersProgression.yml");
        if(!playerProgressionFile.exists()){
            getLogger().info("Arquivo PlayersProgression inexistente, criando um novo");
            getLogger().info("Criado: " + playerProgressionFile.getParentFile().mkdirs());
            saveResource("PlayersProgression.yml", false);
        }
        playerProgression = new YamlConfiguration();
        try{
            playerProgression.load(playerProgressionFile);
        }catch (Exception e){
            getLogger().info( e.getMessage());
        }
    }

    private void getPlayerSoulsFile(){
        playerSoulsFile = new File(getDataFolder(),"PlayersSouls.yml");
        if(!playerSoulsFile.exists()){
            getLogger().info("Arquivo PlayerSouls inexistente, criando um novo");
            getLogger().info("Criado: " + playerSoulsFile.getParentFile().mkdirs());
            saveResource("PlayersSouls.yml", false);
        }
        playerSouls = new YamlConfiguration();
        try{
            playerSouls.load(playerSoulsFile);
        }catch (Exception e){
            getLogger().info( e.getMessage());
        }
    }

    public FileConfiguration getPlayerSouls(){return playerSouls;}
    public String getText(String path){
        String ret = "????";
        return localization.contains(path)? localization.get(path).toString() : ret;
    }
    public void resetPlayer(Player player){
        String playerPath = player.getName() + ":" + player.getUniqueId();
        playerSouls.set(playerPath+".played", false);

        Arrays.stream(TraitsManager.traitID).toList().forEach(s -> {
            playerSouls.set(playerPath+"."+s, "---");
        });
        saveSouls();
        saveProgression();

    }
    public void saveSouls() {
        try {
            playerSouls.save(playerSoulsFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void saveProgression() {
        try {
            playerProgression.save(playerProgressionFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public BaseAdvancement getAdvancement(int index){

        if(!challengerManager.advancementMap.containsKey(index)) return null;

        return challengerManager.advancementMap.get(index);
    }
    public void grantAdvancement(advancements ad, Player player){
        int adIndex = ad.getIndex();
        if(!challengerManager.advancementMap.containsKey(adIndex)){
            Bukkit.getLogger().log(Level.WARNING, "Advancement " + adIndex + " not found");
            return;
        }
        Advancement advancement = challengerManager.advancementMap.get(adIndex);
        String adPath = "given"+ adIndex;
        String adOwner = Objects.requireNonNull(getConfig().get(adPath)).toString();
        if(adOwner.equals("0")){
            advancement.grant(player);
            String playerPath = player.getName() + ":" + player.getUniqueId();
            getConfig().set(adPath, playerPath);
            //saveResource("config.yml", true);
            saveConfig();
        }
        //  advancement.revoke(player);
    }
    public void grantAdvancement(int adIndex, Player player){
        if(!challengerManager.advancementMap.containsKey(adIndex)){
            Bukkit.getLogger().log(Level.WARNING, "Advancement " + adIndex + " not found");
            return;
        }
        Advancement advancement = challengerManager.advancementMap.get(adIndex);
        String adPath = "given"+ adIndex;
        String adOwner = Objects.requireNonNull(getConfig().get(adPath)).toString();
        String playerPath = player.getName() + ":" + player.getUniqueId();
        if(adOwner.equals("0") || adOwner.equals("-1")){
            advancement.grant(player);
            getConfig().set(adPath, playerPath);
            saveConfig();
        }
        events.updateScoreBoard(player);

    }
    public void revokeAvancement(int adIndex, Player player){
        if(!challengerManager.advancementMap.containsKey(adIndex)){
            Bukkit.getLogger().log(Level.WARNING, "Advancement " + adIndex + " not found");
            return;
        }
        Advancement advancement = challengerManager.advancementMap.get(adIndex);
        String adPath = "given"+ adIndex;

        if(advancement.isGranted(player)){
            advancement.revoke(player);
            getConfig().set(adPath,0);
            saveConfig();
        }
        events.updateScoreBoard(player);

    }
    public void onDisable() {
        saveSouls();
        saveProgression();
        main.disable();

        // Rest of your code
    }
}
