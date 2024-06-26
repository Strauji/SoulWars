package org.soulwar;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.function.Supplier;

public class TraitsManager {
    //oh- ep- id- ie= 3h- ok- dd- dn- na- nl- nn-
    static String[] traitID = { "ep","imd","ie","3h","ok","dd","dn","na","nl",
    "nn"};
    static String[] goodTraitID = {"ep","dd","dn","do","na","nl", "nn","ok"};
    static String[] badTraitID ={ "3h", "ie", "od"};
    static String[] otherTraitID = {"imd", "od", "cd","oc"};
    static String[] goodEffects = { "CONDUIT_POWER","DAMAGE_RESISTANCE", "DOLPHINS_GRACE","FAST_DIGGING",
            "FIRE_RESISTANCE", "HERO_OF_THE_VILLAGE", "INCREASE_DAMAGE","INVISIBILITY","JUMP",
    "REGENERATION","SPEED","WATER_BREATHING","SLOW_FALLING"};
    static String[] badEffects = {"WEAKNESS","WITHER", "HARM","SLOW_DIGGING","SLOW","POISON"};
    static String[] damageTypes= {"CONTACT","DRAGON_BREATH","DROWNING","FIRE","FIRE_TICK",
            "FLY_INTO_WALL", "FREEZE","HOT_FLOOR","LAVA","MAGIC","POISON","SONIC_BOOM","STARVATION","SUFFOCATION",
            "WITHER"
    };
    static  String[] fireDamage = {"LAVA", "HOT_FLOOR", "FIRE", "FIRE_TICK"};
    static  String[] hostileMob = {"BLAZE", "CAVE_SPIDER","CREEPER","DROWNED","ELDER_GUARDIAN", "ENDER_DRAGON","ENDERMITE","GHAST","GUARDIAN","HOGLIN","HUSK","MAGMA_CUBE","PHANTOM","PIGLIN", "PIGLIN_BRUTE","PILLAGER","PUFFERFISH","RAVAGER","SHULKER","SKELETON","SLIME","SPIDER","VINDICATOR","WARDEN","WITHER","ZOGLIN","ZOMBIE"};
    static  String[] tamableMob = {"CAT","DONKEY", "HORSE","LLAMA","MULE", "PARROT", "SKELETON_HORSE", "ZOMBIE_HORSE", "WOLF","FOX", "OCELOT"};
    public static Map<String, PotionEffectType> stringToEffect;
    static {
        stringToEffect = new HashMap<>();
        stringToEffect.put("WEAKNESS", PotionEffectType.WEAKNESS);
        stringToEffect.put("WITHER", PotionEffectType.WITHER);
        stringToEffect.put("SLOW_DIGGING", PotionEffectType.SLOW_DIGGING);
        stringToEffect.put("SLOW", PotionEffectType.SLOW);
        stringToEffect.put("POISON", PotionEffectType.POISON);
        stringToEffect.put("LEVITATION", PotionEffectType.LEVITATION);
        stringToEffect.put("HUNGER", PotionEffectType.HUNGER);
        stringToEffect.put("HARM", PotionEffectType.HARM);
        stringToEffect.put("DARKNESS", PotionEffectType.DARKNESS);
        stringToEffect.put("CONFUSION", PotionEffectType.CONFUSION);
        stringToEffect.put("BLINDNESS", PotionEffectType.BLINDNESS);
        stringToEffect.put("BAD_OMEN", PotionEffectType.BAD_OMEN);
        stringToEffect.put("ABSORPTION", PotionEffectType.ABSORPTION);
        stringToEffect.put("CONDUIT_POWER", PotionEffectType.CONDUIT_POWER);
        stringToEffect.put("DAMAGE_RESISTANCE", PotionEffectType.DAMAGE_RESISTANCE);
        stringToEffect.put("DOLPHINS_GRACE", PotionEffectType.DOLPHINS_GRACE);
        stringToEffect.put("FAST_DIGGING", PotionEffectType.FAST_DIGGING);
        stringToEffect.put("FIRE_RESISTANCE", PotionEffectType.FIRE_RESISTANCE);
        stringToEffect.put("HEALTH_BOOST", PotionEffectType.HEALTH_BOOST);
        stringToEffect.put("HERO_OF_THE_VILLAGE", PotionEffectType.HERO_OF_THE_VILLAGE);
        stringToEffect.put("INCREASE_DAMAGE", PotionEffectType.INCREASE_DAMAGE);
        stringToEffect.put("INVISIBILITY", PotionEffectType.INVISIBILITY);
        stringToEffect.put("JUMP", PotionEffectType.JUMP);
        stringToEffect.put("NIGHT_VISION", PotionEffectType.NIGHT_VISION);
        stringToEffect.put("REGENERATION", PotionEffectType.REGENERATION);
        stringToEffect.put("SATURATION", PotionEffectType.SATURATION);
        stringToEffect.put("SPEED", PotionEffectType.SPEED);
        stringToEffect.put("SLOW_FALLING", PotionEffectType.SLOW_FALLING);
        stringToEffect.put("WATER_BREATHING", PotionEffectType.WATER_BREATHING);

    }
    public static Map<String,List<String>> generateRandomPowers(int amount, Plugin pluginInstance){
        Random rand = new Random();
        List<String>  damageAux = new ArrayList<>(Arrays.stream(damageTypes).toList());
        List<String>  badEffectsAux = new ArrayList<>(Arrays.stream(badEffects).toList());
        List<String> goodEffectsAux = new ArrayList<>(Arrays.stream(goodEffects).toList());
        Map<String, List<String>> effects = new HashMap<>();
        for (int i = 0; i < amount ; i++) {
            String trait = traitID[rand.nextInt(traitID.length)];
            String traitEffect ="null";
            if(Arrays.asList(badTraitID).contains(trait)) {
                int value = rand.nextInt(badEffectsAux.size());
                traitEffect = badEffectsAux.get(value);
                badEffectsAux.remove(value);
            } else if (Arrays.asList(goodTraitID).contains(trait)) {
                boolean isNetherWatery = true;
                boolean isWaterHot = true;
                while (isWaterHot || isNetherWatery){
                    int value = rand.nextInt(goodEffectsAux.size());
                    traitEffect = goodEffectsAux.get(value);
                    isNetherWatery = trait.equals("nn") && (traitEffect.equals("WATER_BREATHING") || traitEffect.equals("DOLPHINS_GRACE") || traitEffect.equals("CONDUIT_POWER"));
                    isWaterHot = trait.equals("na") && traitEffect.equals("FIRE_RESISTANCE");

                    goodEffectsAux.remove(value);
                }

            }else if(Arrays.asList(otherTraitID).contains(trait)){
                switch (trait) {
                    //  case "ae": //Aura of effect
                    //    break;
                    case "imd", "cd" -> {
                        int value = rand.nextInt(damageAux.size());
                        traitEffect = damageAux.get(value);
                        damageAux.remove(value);

                    }
                    //Heals with damage
                   // case "od" -> {
                 //   } //on death
                    //custom
                }
            }

            List<String> tEffects = effects.get(trait);
            if(tEffects == null) tEffects =  new ArrayList<>();

            tEffects.add(traitEffect);
            effects.put(trait,tEffects);
        }


        return  effects;
    }
    private static List<String> getTrait(FileConfiguration file, Player player, String trait){

        String playerPath = player.getName() + ":" + player.getUniqueId();
        if(file.contains(playerPath+"."+trait)) return
                (List<String>) file.getList(playerPath + "." + trait);
        return null;
    }
    public static void readTraits(FileConfiguration soulsFile, Player player, Plugin pluginInstance){
        Arrays.stream(traitID).toList().forEach(s -> {
                List<String> traits = getTrait(soulsFile, player, s);


                if(null != traits){

                    List<String> effects = new ArrayList<>();
                    effects.addAll(traits);

                    player.setMetadata(s, new FixedMetadataValue(pluginInstance, effects));

                }

        });

    }
}
