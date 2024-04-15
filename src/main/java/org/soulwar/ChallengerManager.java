package org.soulwar;


import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChallengerManager {

   // public List<Advancement> advancementList = new ArrayList<>();
    private  SoulWars pluginInstance;
    Set<BaseAdvancement> advancementSet = new HashSet<>();
    public void initializeAdvancements(SoulWars pluginInstance){
        this.pluginInstance = pluginInstance;
        this.pluginInstance.advancementTab = this.pluginInstance.api.createAdvancementTab("soulwars");
        AdvancementDisplay rootDisplay = new AdvancementDisplay(Material.TOTEM_OF_UNDYING,
                "Soul Wars", AdvancementFrameType.TASK, true, true,
                0, 0, "Jovem, você precisa conquistar sua alma");
        this.pluginInstance.root = new RootAdvancement(this.pluginInstance.advancementTab
                , "root", rootDisplay, "textures/block/stone.png");
        createVulto();
        createAlquimista();
        createBomba();
        createFranco();
        createCabra();
        createCowboy();
        createNether();
        createOceano();
        createshama();
        createVoid();
        createLider();
        createGuerra();
        /// above all
        this.pluginInstance.advancementTab.registerAdvancements(this.pluginInstance.root, advancementSet);

    }


    private void createFranco(){
        AdvancementDisplay rootDisplay = new AdvancementDisplay(Material.ARROW,
                pluginInstance.getText("francoNome"), AdvancementFrameType.TASK, true, true,
                1, 1, pluginInstance.getText("francoDesc"));
        BaseAdvancement ad = new BaseAdvancement ("franco",rootDisplay, this.pluginInstance.root);
        advancementSet.add(ad);
    }
    private void createGuerra(){
        AdvancementDisplay rootDisplay = new AdvancementDisplay(Material.IRON_SWORD,
                pluginInstance.getText("guerraNome"), AdvancementFrameType.TASK, true, true,
                2, 1, pluginInstance.getText("guerraDesc"));
        BaseAdvancement ad = new BaseAdvancement ("guerra",rootDisplay, this.pluginInstance.root);
        advancementSet.add(ad);
    }
    private void createAlquimista(){
        AdvancementDisplay rootDisplay = new AdvancementDisplay(Material.POTION,
                pluginInstance.getText("alquimistaNome"), AdvancementFrameType.TASK, true, true,
                3, 1, pluginInstance.getText("alquimistaDesc"));
        BaseAdvancement ad = new BaseAdvancement ("alquimista",rootDisplay, this.pluginInstance.root);
        advancementSet.add(ad);
    }
    private void createBomba(){
        AdvancementDisplay rootDisplay = new AdvancementDisplay(Material.TNT,
                pluginInstance.getText("bombaNome"), AdvancementFrameType.TASK, true, true,
                4, 1, pluginInstance.getText("bombaDesc"));
        BaseAdvancement ad = new BaseAdvancement ("bomba",rootDisplay, this.pluginInstance.root);
        advancementSet.add(ad);
    }
    private void createLider(){
        AdvancementDisplay rootDisplay = new AdvancementDisplay(Material.WOLF_SPAWN_EGG,
                pluginInstance.getText("matilhaNome"), AdvancementFrameType.TASK, true, true,
                5, 1, pluginInstance.getText("matilhaDesc"));
        BaseAdvancement ad = new BaseAdvancement ("matilha",rootDisplay, this.pluginInstance.root);
        advancementSet.add(ad);
    }
    private void createNether(){
        AdvancementDisplay rootDisplay = new AdvancementDisplay(Material.LAVA_BUCKET,
                pluginInstance.getText("netherNome"), AdvancementFrameType.CHALLENGE, true, true,
                1, 2, pluginInstance.getText("netherDesc"));
        BaseAdvancement ad = new BaseAdvancement ("nether",rootDisplay, this.pluginInstance.root);
        advancementSet.add(ad);
    }
    private void createOceano(){
        AdvancementDisplay rootDisplay = new AdvancementDisplay(Material.WATER_BUCKET,
                pluginInstance.getText("oceanoNome"), AdvancementFrameType.CHALLENGE, true, true,
                2, 2, pluginInstance.getText("oceanoDesc"));
        BaseAdvancement ad = new BaseAdvancement ("oceano",rootDisplay, this.pluginInstance.root);
        advancementSet.add(ad);
    }
    private void createVulto(){
        AdvancementDisplay rootDisplay = new AdvancementDisplay(Material.SCULK,
                pluginInstance.getText("vultoNome"), AdvancementFrameType.CHALLENGE, true, true,
                3, 2, pluginInstance.getText("vultoDesc"));
        BaseAdvancement vulto = new BaseAdvancement ("vulto",rootDisplay, this.pluginInstance.root);
        advancementSet.add(vulto);
    }
    private void createshama(){
        AdvancementDisplay rootDisplay = new AdvancementDisplay(Material.HONEYCOMB,
                pluginInstance.getText("shamaNome"), AdvancementFrameType.GOAL, true, true,
                1, 3, pluginInstance.getText("shamaDesc"));
        BaseAdvancement ad = new BaseAdvancement ("shama",rootDisplay, this.pluginInstance.root);
        advancementSet.add(ad);
    }
    private void createCowboy(){
        AdvancementDisplay rootDisplay = new AdvancementDisplay(Material.SADDLE,
                pluginInstance.getText("cowboyNome"), AdvancementFrameType.GOAL, true, true,
                2, 3, pluginInstance.getText("cowboyDesc"));
        BaseAdvancement ad = new BaseAdvancement ("cowboy",rootDisplay, this.pluginInstance.root);
        advancementSet.add(ad);
    }
    private void createCabra(){
        AdvancementDisplay rootDisplay = new AdvancementDisplay(Material.GOAT_HORN,
                pluginInstance.getText("cabraNome"), AdvancementFrameType.GOAL, true, true,
                3, 3, pluginInstance.getText("cabraDesc"));
        BaseAdvancement ad = new BaseAdvancement ("cabra",rootDisplay, this.pluginInstance.root);
        advancementSet.add(ad);
    }
    private void createVoid(){
        AdvancementDisplay rootDisplay = new AdvancementDisplay(Material.END_PORTAL,
                pluginInstance.getText("voidNome"), AdvancementFrameType.CHALLENGE, true, true,
                4, 3, pluginInstance.getText("voidDesc"));
        BaseAdvancement ad = new BaseAdvancement ("void",rootDisplay, this.pluginInstance.root);
        advancementSet.add(ad);
    }
}
