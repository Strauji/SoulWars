package org.soulwar;

import java.util.HashSet;
import java.util.Set;

public class ItemManager {
    private final SoulWars pluginInstance;
    private final Set<AbstractItem> itemRegistry = new HashSet<>();
    public ItemManager(SoulWars pluginInstance){
        this.pluginInstance = pluginInstance;
        registerHandler(new SoulShard(pluginInstance));
    }
    public void registerHandler(AbstractItem... handlers){
        for(AbstractItem handler: handlers){
            this.registerHandler(handler);
        }
    }

    public void registerHandler(AbstractItem handler){
        itemRegistry.add(handler);
        pluginInstance.getServer().getPluginManager().registerEvents(handler, pluginInstance);
    }
    public <T> T getHandler(Class<? extends  T> class_){
        for(AbstractItem handler: itemRegistry){
            if(handler.getClass().equals(class_))return (T)handler;
        }
        return null;
    }
    public AbstractItem getHandler(String itemId){
        for(AbstractItem handler: itemRegistry){
            if(handler.itemID.equals(itemId)){
                return  handler;
            }
        }
        return null;
    }
}
