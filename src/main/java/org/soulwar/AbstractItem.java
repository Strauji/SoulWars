package org.soulwar;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public abstract class AbstractItem implements Listener {
    protected final SoulWars pluginInstance;
    public final String itemID;
    public final Material material;
    public AbstractItem(SoulWars pluginInstance, String itemID, Material material){
        this.pluginInstance = pluginInstance;
        this.itemID = itemID;
        this.material = material;
    }
    public boolean isApplicable(ItemStack itemStack){
        if(null == itemStack || !itemStack.hasItemMeta()) return false;
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
        String persistentID = persistentDataContainer.get(new NamespacedKey(pluginInstance, "ITEM_ID"),
                PersistentDataType.STRING);
        return itemID.equals(persistentID);
    }
    public ItemStack getItem(Player player){
        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer persistentDataContainer = meta.getPersistentDataContainer();
        persistentDataContainer.set(new NamespacedKey(pluginInstance, "ITEM_ID"), PersistentDataType.STRING, itemID);
        itemStack.setItemMeta(meta);
        return generateItem(itemStack, player);
    }
    protected abstract ItemStack generateItem(ItemStack itemStack, Player player);
}
