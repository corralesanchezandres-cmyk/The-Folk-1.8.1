package com.thefolk;

import com.thefolk.item.FolkConfigItem;
import com.thefolk.item.ParalyzerItem;
import com.thefolk.item.ReaperItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TheFolkMod.MOD_ID);

    public static final RegistryObject<Item> FOLK_CONFIG = ITEMS.register("folkconfig",
            () -> new FolkConfigItem(new Item.Properties().stacksTo(1)));

    /** Segador: ralentiza y vuelve tontos a los monstruos durante 3 segundos. */
    public static final RegistryObject<Item> REAPER = ITEMS.register("reaper",
            () -> new ReaperItem(new Item.Properties()));

    /** Paralizador: paraliza a los monstruos durante 2 segundos. */
    public static final RegistryObject<Item> PARALYZER = ITEMS.register("paralyzer",
            () -> new ParalyzerItem(new Item.Properties().durability(48)));
}
