package org.soulwar;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Commands implements CommandExecutor{
    private final SoulWars pluginInstance = (SoulWars)
            Bukkit.getPluginManager().getPlugin("SoulWars");
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
       if(args.length > 1){
           switch (args[0]){
               case "grant":
                //sw grant player advancement
                   if(args.length > 2){
                       Player player;
                       try{
                         player = Bukkit.getPlayerExact(args[1]);
                       }catch (Exception ignored){
                           sender.sendMessage("Jogador não encontrado");
                           return false;
                       }//
                     //  BaseAdvancement advancement = pluginInstance.getAdvancement();

                           pluginInstance.grantAdvancement(Integer.parseInt(args[2]),player);
                           return true;


                   }
                   break;
               case "revoke":
                   //sw grant player advancement
                   if(args.length > 2){
                       Player player;
                       try{
                           player = Bukkit.getPlayerExact(args[1]);
                       }catch (Exception ignored){
                           sender.sendMessage("Jogador não encontrado");
                           return false;
                       }
                       BaseAdvancement advancement = pluginInstance.getAdvancement(Integer.parseInt(args[2]));
                       if(null != advancement){
                           if(advancement.isGranted(player)){
                               advancement.revoke(player);
                               return true;
                           }else{

                              sender.sendMessage("O jogador " + player.getName() +
                                      " não possuia "+ advancement.getDisplay().getTitle());
                              return false;
                           }

                       }else{
                           sender.sendMessage("Conquista não encontrada");
                           return false;
                       }

                   }
                   break;
               case "reload":
                   if(sender.isOp()){
                       pluginInstance.reload();
                   }
                   break;
           }
       }
        return false;
    }

}
