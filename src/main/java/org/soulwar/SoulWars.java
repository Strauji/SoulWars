package org.soulwar;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class SoulWars extends JavaPlugin implements Listener {
    private File playerSoulsFile;
    private File playerProgressionFile;
    private String langFile = "pt_br.yml";
    private File localizationFile;
    private FileConfiguration localization;
    private FileConfiguration playerSouls;
    private FileConfiguration playerProgression;
    public UltimateAdvancementAPI api ;

    public AdvancementTab advancementTab;
    public RootAdvancement root;
    private AdvancementMain main;
    public ChallengerManager challengerManager = new ChallengerManager();
    @Override
    public void onLoad() {
        main = new AdvancementMain(this);
        main.load();

        // Rest of your code
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

        Bukkit.getPluginManager().registerEvents(new Events(), this);

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
        saveResource("Config.yml", false);
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
        String[] traitID = {"oc", "oh","ep","id","ie","3h","ae","ok","od","uc","fc","dd","dn","dn","na","nl","nn"};
        Arrays.stream(traitID).toList().forEach(s -> {
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
    public void onDisable() {
        saveSouls();
        saveProgression();
        main.disable();

        // Rest of your code
    }
}
