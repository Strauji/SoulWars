package org.soulwar;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
        if(!playerSouls.contains(playerPath+".played") ||
                !Boolean.valueOf( playerSouls.get(playerPath+".played").toString())){
            Map<String, List<String>> traits =  TraitsManager.generateRandomPowers(3, pluginInstance);
            for(String trait: traits.keySet()){
                playerSouls.set(playerPath+"."+trait, traits.get(trait));
            }
            playerSouls.set(playerPath+".played", true);
            pluginInstance.saveSouls();
        }
        TraitsManager.readTraits(playerSouls, player, pluginInstance);
        //here we'll enable the permanent effects on the player
        AttributeModifier warriorAttribute;
        if(hasAdvancement(SoulWars.advancements.Guerra, player)){
            warriorAttribute= new AttributeModifier(
                    nonRandomUUIDGenerator(player.getUniqueId(), pluginInstance.attackSpeedUUIDMOD, 'a')
                    ,"guerra", 99
                    , AttributeModifier.Operation.MULTIPLY_SCALAR_1);
        }else{
            warriorAttribute = new AttributeModifier(
                    nonRandomUUIDGenerator(player.getUniqueId(), pluginInstance.attackSpeedUUIDMOD, 'a')
                    ,"guerra", 0
                    , AttributeModifier.Operation.MULTIPLY_SCALAR_1);
        }

        try{Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)).removeModifier(warriorAttribute);}
        catch (Exception ignored){}
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)).addModifier(warriorAttribute);
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
                if(hasAdvancement(SoulWars.advancements.Vulto, player)){
                    Block stepBlock = player.getLocation().getBlock().getRelative(0, 1, 0); //Help me ste-....
                    int lightLevel = stepBlock.getLightLevel();
                    int sunlightLevel = stepBlock.getLightFromSky();
                    int shadowMultiplier =  util_isDay(player) ? 1: 0;
                    int shadowPower =  (( 15 - Math.max(sunlightLevel*shadowMultiplier, lightLevel)));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 400, 1 ));
                    AttributeModifier shadowAttribute = new AttributeModifier(
                            nonRandomUUIDGenerator(player.getUniqueId(), pluginInstance.speedUUIDMOD, 'a')
                            ,"livingShadow", shadowPower*0.1
                            , AttributeModifier.Operation.MULTIPLY_SCALAR_1);
                    try{
                        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED))
                                .removeModifier(shadowAttribute);

                        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE))
                                .removeModifier(shadowAttribute);
                    }catch (Exception ignored){}
                    Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED))
                            .addModifier(shadowAttribute);
                    Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE))
                            .addModifier(shadowAttribute);

                }
                if(hasAdvancement(SoulWars.advancements.Alquimista, player)){
                    player.getActivePotionEffects().forEach(potionEffect -> {

                        if(!(pluginInstance.badPotionEffects.contains(potionEffect.getType().getKey()
                                .toString().replace("minecraft:", "").toUpperCase()))){
                            player.addPotionEffect(new PotionEffect(potionEffect.getType(), 10000000
                                    , potionEffect.getAmplifier()));

                        }

                    });
                }
                if(hasAdvancement(SoulWars.advancements.Oceano, player)){
                    player.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, PotionEffect.INFINITE_DURATION
                            , 3));
                }
                if(hasAdvancement(SoulWars.advancements.Nether, player)){
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION
                            , 10));
                }
                if(player.hasMetadata("ie")){
                    List<String> effect = (List<String>) player.getMetadata("ie").get(0).value();
                    Collection<PotionEffect> potionEffects = player.getActivePotionEffects();

                    potionEffects.forEach(potionEffect -> {
                        if(effect.contains(potionEffect.getType().getKey().
                                toString().replace("minecraft:","").toUpperCase().toString())){
                            player.removePotionEffect(potionEffect.getType());
                        }
                    });
                }
                progressionCheck(player);
            }
        };
        bukkitRunnable.runTaskTimer(pluginInstance, 0, 10L);
        initScoreBoard(player);
        ItemStack itemStack = pluginInstance.itemManager.getHandler(SoulShard.class).getItem(player);
        player.getInventory().addItem(itemStack);
    }
    private void progressionCheck(Player player){
        //Let's start by checking for light levels for Vulto
        Block stepBlock = player.getLocation().getBlock().getRelative(0, 1, 0);
        int lightLevel = stepBlock.getLightLevel();
        int sunlightLevel = stepBlock.getLightFromSky();
        Advancement advancement = pluginInstance.challengerManager.advancementMap
                .get((SoulWars.advancements.Vulto.getIndex()));
        String adPath = "given"+ (SoulWars.advancements.Vulto.getIndex());
        String adOwner = Objects.requireNonNull(pluginInstance.getConfig().get(adPath)).toString();
        int vultotickCount = 1000;
        if(adOwner.equals("0") && !hasAdvancement(SoulWars.advancements.Vulto, player)){
            if(Math.max(lightLevel, sunlightLevel) < 2){ //If player is standing in light level 2 or below
                if(player.hasMetadata("vultotick")){
                    vultotickCount += player.getMetadata("vultotick").get(0).asInt(); //increase it
                    player.removeMetadata("vultotick", pluginInstance);
                }
                if(vultotickCount > 480000){
                    Bukkit.getLogger().info("Unlocked vulto");
                    pluginInstance.getAdvancement(10).grant(player);
                }
            }else{ // else, reset, failed
                vultotickCount = 0;
            }

            player.setMetadata("vultotick", new FixedMetadataValue(pluginInstance, vultotickCount));
        }

    }
    private void initScoreBoard(Player player){
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("ServerName", "dummy", ChatColor.BOLD+ "Soul Wars");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        String[] traitID = {"oc", "oh","ep","id","ie","3h","ok","od","dd","dn","na","nl",
                "nn"};



        AtomicInteger i = new AtomicInteger();

        for (String s : traitID) {
            if(player.hasMetadata(s)){

                List<String> effects = (List<String>) player.getMetadata(s).get(0).value();

                AtomicBoolean hasEffect = new AtomicBoolean(false);
                    effects.forEach(e -> {
                        hasEffect.set(true);
                        Score aux2 = obj.getScore( ChatColor.DARK_PURPLE + "-> " + e);
                        aux2.setScore(i.get());
                        i.getAndIncrement();
                    });
                    if(hasEffect.get()){

                        Score aux = obj.getScore(ChatColor.GREEN + pluginInstance.getText(s) + ":");
                        aux.setScore(i.get());
                        i.getAndIncrement();
                    }

            }
        }

        Score aux4 = obj.getScore( ChatColor.BOLD + "Traços Normais:");
        aux4.setScore(i.get());
        i.getAndIncrement();
        boolean hasLen = false;
        for (SoulWars.advancements c : SoulWars.advancements.values()) {
            if(hasAdvancement(c, player)){
                Score aux2 = obj.getScore( ChatColor.DARK_PURPLE + "->" +
                        pluginInstance.getText((c.toString().toLowerCase())+"Nome"));
                aux2.setScore(i.get());
                i.getAndIncrement();
                hasLen = true;
            }

        }

        Score aux3 = obj.getScore( ChatColor.BOLD + "Traços Lendários:");
        aux3.setScore(i.get());
        i.getAndIncrement();
        player.setScoreboard(board);


    }
    @EventHandler
    public void onProjectileLaunchEvent(ProjectileLaunchEvent e){
        if(e.getEntity().getShooter() instanceof  Player player && hasAdvancement(SoulWars.advancements.Franco, player))
        {

            Projectile projectile = e.getEntity();
            if(!((projectile instanceof EnderPearl)))

                projectile.setVelocity(projectile.getVelocity().multiply(2.0));
            if(projectile instanceof Arrow arrow){
                projectile.setVelocity(projectile.getVelocity().multiply(2.0));
            }
            float rng = (float) Math.random();
            if(rng < 0.9){
                ItemStack itemStack = getProjectileItemStack(projectile);
                player.getInventory().removeItem(itemStack);
                player.getInventory().addItem(itemStack);
            }
        }
    }
    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Block placed = e.getBlockPlaced();
        BlockData blockData  = placed.getBlockData();
        if(hasAdvancement(SoulWars.advancements.Bomba, player)){
            if((blockData.getMaterial().getKey()
                    .toString().replace("minecraft:","").toUpperCase().equals("TNT")))
            {
                Inventory playerInventory = e.getPlayer().getInventory();
                playerInventory.removeItem(new ItemStack(blockData.getMaterial()));
                playerInventory.addItem(new ItemStack(blockData.getMaterial()));
            }
        }
    }
    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        if (e.getRightClicked() instanceof Tameable friendShaped) {
            //if no friend why friendshaped? let's solve that]

            if (friendShaped.isTamed() || friendShaped instanceof Camel ) {

                if(friendShaped instanceof Wolf) {
                    if (friendShaped.getOwner() instanceof Player tamer) {
                        if (hasAdvancement(SoulWars.advancements.Lide, tamer)) {
                            friendShaped.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,
                                    PotionEffect.INFINITE_DURATION, 1));
                        }
                    }
                }
                if (friendShaped instanceof AbstractHorse horse){
                     if(friendShaped.getOwner() instanceof Player tamer) {
                          if (hasAdvancement(SoulWars.advancements.Cowboy, tamer)){
                                horse.setJumpStrength(1.5);
                                friendShaped.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
                                        PotionEffect.INFINITE_DURATION, 10));
                                horse.getInventory().setSaddle(new ItemStack(Material.SADDLE, 1));
                            }
                        }
                    }


                    // {
                      //  horse.setJumpStrength(1.5);


                        //horse.getInventory().setSaddle(new ItemStack(Material.SADDLE, 1));


                }
            }
        }
    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (hasAdvancement(SoulWars.advancements.Nether, player)) {
            boolean nbCondition = (player.getLocation().getBlock().getType() == Material.LAVA);
            if (player.getGameMode() != GameMode.CREATIVE) {
                if (nbCondition) player.setAllowFlight(true);
                else player.setAllowFlight(pluginInstance.getServer().getAllowFlight());
                player.setFlying(nbCondition);
                BukkitRunnable bukkitRunnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.getLocation().getBlock().getType() != Material.LAVA) {
                            this.cancel();
                            player.removeMetadata("netherblessing", pluginInstance);
                            return;
                        }
                        player.setMetadata("netherblessing", new FixedMetadataValue(pluginInstance, true));
                    }
                };
                bukkitRunnable.runTaskTimer(pluginInstance, 0L, 20L);
            }
        }
        if(hasAdvancement(SoulWars.advancements.Void, player)){
            if(player.getWorld().getEnvironment() == World.Environment.THE_END && player.getLocation().getY() <= 1){
                voidBornFrostWalker(player);
            }
        }
    }
    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent e){
        Player player = e.getEntity().getKiller();
        if(player != null){
            if(player.hasMetadata("ok")){
                applyEffects(player, player.getMetadata("ok").get(0).value(), 120 );
            }

        }
    }
    @EventHandler
    public void onEntityDamage(EntityDamageEvent e){
        EntityDamageEvent.DamageCause cause = e.getCause();
        if(e.getEntity() instanceof  Tameable friendShaped){
            if(friendShaped.isTamed() && friendShaped.getOwner() instanceof Player tamer){
                if(hasAdvancement(SoulWars.advancements.Lide, tamer)){
                    e.setDamage(e.getDamage()*0.5);
                }
               if (hasAdvancement(SoulWars.advancements.Cowboy, tamer)) {
                   if (friendShaped instanceof AbstractHorse horse){
                       e.setDamage(0);
                   }
               }

            }
        }else if(e.getEntity() instanceof  Player player){
            boolean isFallDamage =
                    cause == EntityDamageEvent.DamageCause.FALL || cause == EntityDamageEvent.DamageCause.FLY_INTO_WALL;
            boolean isVoidDamage = cause == EntityDamageEvent.DamageCause.VOID;
            if(hasAdvancement(SoulWars.advancements.Cabra, player) && isFallDamage){
                e.setCancelled(true);
            }
            if(hasAdvancement(SoulWars.advancements.Void, player) && isVoidDamage){
                voidBornFrostWalker(player);
                e.setCancelled(true);
            }
            if(player.hasMetadata("id")){
                List<String> damageI = (List<String>) player.getMetadata("id").get(0).value();
                if(damageI.contains(cause.toString())){
                    e.setCancelled(true);
                }
            }
        }
    }
    @EventHandler
    public void onEntityTarget(EntityTargetEvent e){
        if(e.getTarget() instanceof  Player player){
            if(hasAdvancement(SoulWars.advancements.Shama, player)){
                e.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onEntityDamageEntity(EntityDamageByEntityEvent e){
        if(e.getEntity() instanceof  LivingEntity damaged){
            if(e.getDamager() instanceof Player player){
                if(player.hasMetadata("3h")){
                    int count = 0;
                    if(damaged.hasMetadata("got3hed")){
                        count = damaged.getMetadata("got3hed").get(0).asInt();
                        damaged.removeMetadata("got3hed", pluginInstance);
                    }
                    count++;
                    if(count >= 3){
                        applyEffects(damaged, player.getMetadata("3h").get(0).value() );
                        count = 0;
                    }
                    damaged.setMetadata("got3hed", new FixedMetadataValue(pluginInstance, count));
                }
            }
        }

    }
    @EventHandler
    private void playerDeathEvent(PlayerDeathEvent e){

    }
    private ItemStack getProjectileItemStack(Projectile projectile) {
        ItemStack itemStack = new ItemStack(Material.AIR);
        projectile.setMetadata("archesembrace", new FixedMetadataValue(pluginInstance, "archesembrace"));
        if(projectile instanceof Egg) itemStack = new ItemStack(Material.EGG);
        else if(projectile instanceof EnderPearl) itemStack = new ItemStack(Material.ENDER_PEARL);
        else if(projectile instanceof Snowball) itemStack = new ItemStack(Material.SNOWBALL);
        else if(projectile instanceof  TippedArrow arrow)  {
            try{
                if(arrow.getBasePotionType() != PotionType.UNCRAFTABLE) {
                    itemStack = new ItemStack(Material.TIPPED_ARROW);
                    PotionMeta itemMeta = (PotionMeta) itemStack.getItemMeta();
                    if (null != itemMeta) itemMeta.setBasePotionType(arrow.getBasePotionType());
                    itemStack.setItemMeta(itemMeta);
                }
            }catch (NoSuchMethodError ignored){
                if(arrow.getBasePotionData() != new PotionData(PotionType.UNCRAFTABLE)) {
                    itemStack = new ItemStack(Material.TIPPED_ARROW);
                    PotionMeta itemMeta = (PotionMeta) itemStack.getItemMeta();
                    if (null != itemMeta) itemMeta.setBasePotionData(arrow.getBasePotionData());
                    itemStack.setItemMeta(itemMeta);
                }
            }
        }
        else if(projectile instanceof Arrow arrow ){
            itemStack = new ItemStack( Material.ARROW);
            try{
                if(arrow.getBasePotionType() != PotionType.UNCRAFTABLE) {
                    itemStack = new ItemStack(Material.TIPPED_ARROW);
                    PotionMeta itemMeta = (PotionMeta) itemStack.getItemMeta();
                    if (null != itemMeta) itemMeta.setBasePotionType(arrow.getBasePotionType());
                    itemStack.setItemMeta(itemMeta);
                }
            }catch (NoSuchMethodError ignored){
                if(arrow.getBasePotionData().getType() != PotionType.UNCRAFTABLE) {
                    itemStack = new ItemStack(Material.TIPPED_ARROW);
                    PotionMeta itemMeta = (PotionMeta) itemStack.getItemMeta();
                    if (null != itemMeta) itemMeta.setBasePotionData(arrow.getBasePotionData());
                    itemStack.setItemMeta(itemMeta);
                }
            }
        }
        else if(projectile instanceof  SpectralArrow) itemStack = new ItemStack(Material.SPECTRAL_ARROW);
        return itemStack;
    }
    private void applyEffects(LivingEntity player, Object o_effects){
        List<String> effects = (List<String>) o_effects;
        effects.forEach(s -> {
            int potency = 0;
            PotionEffectType potionEffectType = TraitsManager.stringToEffect.get(s);
            if(player.hasPotionEffect(potionEffectType)){
                potency += player.getPotionEffect(potionEffectType).getAmplifier();
            }
            int duration = s.toLowerCase() != "night_vision" ? 400 : 119;
            player.addPotionEffect(new PotionEffect(potionEffectType, duration,
                    potency, false, false,true ));
        });
    }
    private void applyEffects(LivingEntity player, Object o_effects, int duration){
        List<String> effects = (List<String>) o_effects;
        effects.forEach(s -> {
            int potency = 0;
            PotionEffectType potionEffectType = TraitsManager.stringToEffect.get(s);
            if(player.hasPotionEffect(potionEffectType)){
                potency += player.getPotionEffect(potionEffectType).getAmplifier();
            }

            player.addPotionEffect(new PotionEffect(potionEffectType, duration,
                    potency, false, false,true ));
        });
    }
    public void voidBornFrostWalker(Player player){
        player.setFallDistance(0);
        Location loc = player.getLocation();

        //  player.getWorld().getBlockAt(loc).setType(Material.ICE);
        player.getWorld().getBlockAt(loc).getRelative(-1,-1,1).setType(Material.ICE);
        if(player.getWorld().getBlockAt(loc).getRelative(-1,-1,1).getType() == Material.AIR)
            player.getWorld().getBlockAt(loc).getRelative(-1,-1,0).setType(Material.ICE);
        if(player.getWorld().getBlockAt(loc).getRelative(1,-1,0).getType() == Material.AIR)
            player.getWorld().getBlockAt(loc).getRelative(1,-1,0).setType(Material.ICE);
        if(player.getWorld().getBlockAt(loc).getRelative(-1,-1,-1).getType() == Material.AIR)
            player.getWorld().getBlockAt(loc).getRelative(-1,-1,-1).setType(Material.ICE);
        if(player.getWorld().getBlockAt(loc).getRelative(1,-1,-1).getType() == Material.AIR)
            player.getWorld().getBlockAt(loc).getRelative(1,-1,-1).setType(Material.ICE);
        if(player.getWorld().getBlockAt(loc).getRelative(0,-1,-1).getType() == Material.AIR)
            player.getWorld().getBlockAt(loc).getRelative(0,-1,-1).setType(Material.ICE);
        if(player.getWorld().getBlockAt(loc).getRelative(0,-1,1).getType() == Material.AIR)
            player.getWorld().getBlockAt(loc).getRelative(0,-1,1).setType(Material.ICE);
        if(player.getWorld().getBlockAt(loc).getRelative(0,-1,1).getType() == Material.AIR)
            player.getWorld().getBlockAt(loc).getRelative(1,-1,1).setType(Material.ICE);
        if(player.getWorld().getBlockAt(loc).getRelative(0,-1,0).getType() == Material.AIR)
            player.getWorld().getBlockAt(loc).getRelative(0,-1,0).setType(Material.ICE);

        if(player.getLocation().getY() < 0) {
            loc.setY(1);
            player.teleport(loc);
        }


    }
    public boolean hasAdvancement(SoulWars.advancements advancements, Player player){
        int index = advancements.getIndex();
              return ((com.fren_gor.ultimateAdvancementAPI.advancement.Advancement)
                       pluginInstance.challengerManager.advancementMap.get(index)).isGranted(player);
    }
    public int getSoulsAmount(){
        return pluginInstance.challengerManager.advancementMap.size();
    }
    public boolean util_isDay(Player player) {

        long time = player.getWorld().getTime();
        return time < 12300 || time > 23850;

    }
    private UUID nonRandomUUIDGenerator(UUID baseUUID, int digit, char mod){// it's hackin' o'clock
        String uuidString = baseUUID.toString();
        int newDigit = Integer.parseInt(String.valueOf(uuidString.charAt(digit)), 16);
        newDigit += mod;
        newDigit = newDigit % 16;
        String newChar = Integer.toHexString(newDigit);
        StringBuilder newUUID = new StringBuilder(uuidString);
        newUUID.setCharAt(digit,newChar.charAt(0));
        return  UUID.fromString(newUUID.toString());
    }
}
