package com.Jessy1237.DwarfCraft;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

public enum EffectType
{
    // IMPLEMENTATION PRIORITY ORDER
    BLOCKDROP, 
    MOBDROP, 
    SWORDDURABILITY, 
    PVPDAMAGE, 
    PVEDAMAGE, 
    EXPLOSIONDAMAGE, 
    FIREDAMAGE, 
    FALLDAMAGE, 
    FALLTHRESHOLD, 
    PLOWDURABILITY, 
    TOOLDURABILITY, 
    EAT, 
    CRAFT, 
    PLOW, 
    DIGTIME, 
    BOWATTACK, 
    VEHICLEDROP, 
    VEHICLEMOVE,
    SPECIAL,
    FISH,
    RODDURABILITY,
    SMELT,
    BREW,
    SHEAR
    ;
    protected static EffectType getEffectType( String name )
    {
        for ( EffectType effectType : EffectType.values() )
        {
            if ( effectType.toString().equalsIgnoreCase( name ) )
                return effectType;
        }
        return null;
    }

}
