package com.freedsuniverse.realweather;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RealWeather extends JavaPlugin{
    
    final String NODE = "<condition data=";
    final String DEFAULT = "Stockholm,Sweden";
    private URL webpage;
    
    public void onEnable(){
        File conf = new File("plugins/RealWeather/config.yml");
        
        YamlConfiguration config;
        
        if(!conf.exists()){
            try {
                new File(conf.getParent()).mkdirs();
                conf.createNewFile();
            } catch (IOException e) {
            }
            config = YamlConfiguration.loadConfiguration(conf);
            config.set("Zip", DEFAULT);
        }else{
            config = YamlConfiguration.loadConfiguration(conf);
        }
        
        String zipCode = config.getString("Zip");
            
        if(zipCode.length() == 5 || zipCode.length() == DEFAULT.length()){
            System.out.println("[Real Weather] Enabled and basing weather off zip code:" + zipCode);
            setZip(zipCode);
            
            this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                public void run() {
                    updateWeather();
                }
            }, 7200L, 80L);
        }else{
            System.out.println("[Real Weather] Invalid zip code, disabling...");
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd,String commandlabel, String[] args){
        if(cmd.getName().equalsIgnoreCase("rw")){
            if(args.length == 1){
                if(sender instanceof Player){
                    Player p = (Player)sender;
                    if(!p.hasPermission("realweather.setzip")){
                        return true;
                    }
                }
                String zip = args[0];
                if(zip.length() == 5){
                    sender.sendMessage(ChatColor.GREEN + "Zip code set to '" + zip + "'");                 
                    setZip(zip);
                    updateWeather();
                }else{
                    sender.sendMessage(ChatColor.RED + "Invalid zip code.");
                }
                return true;
            }
            return false;
        }
        return true;
    }
    
    public void updateWeather(){
        String cond = getCondition();
        
        for (World world : Bukkit.getServer().getWorlds()){
            if(isRain(cond)){
                world.setStorm(true);     
            }else{
                world.setStorm(false);
            }
        }
    }
    
    public String getCondition(){
        try {      
            BufferedReader in = new BufferedReader(new InputStreamReader(webpage.openStream()));

            String inputLine = in.readLine();
            in.close();

            int start = inputLine.indexOf(NODE) + NODE.length() + 1;
            int end = inputLine.indexOf("/", start) - 1;

            String condition = inputLine.subSequence(start, end).toString();
            return condition;
        } catch (MalformedURLException e) {
            return "";
        } catch (IOException e) {
            return "";
        }
    }
    
    public void setZip(String zip){
        try {
            webpage = new URL("http://www.google.com/ig/api?weather=" + zip);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    
    public boolean isRain(String condition){
        return condition.contains("rain") || condition.contains("storm");
    }    
}