package com.md_4.thunderauthentication.utils;

import org.bukkit.Sound;

public class SoundManager {

    public static Sound accessDenied(){
        return Sound.ENTITY_VILLAGER_NO;
    }

    public static Sound premiumADDED(){
        return Sound.ENTITY_PLAYER_LEVELUP;
    }

    public static Sound tableDeleted(){
        return Sound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR;
    }

    public static Sound tableNotFound(){
        return Sound.BLOCK_ANVIL_HIT;
    }

    public static Sound alreadyAddedToPremiumList(){
        return Sound.ENTITY_PANDA_SNEEZE;
    }

    public static Sound logged(){
        return Sound.BLOCK_NOTE_BLOCK_PLING;
    }

    public static Sound usage(){
        return Sound.ENTITY_PANDA_AMBIENT;
    }

}
