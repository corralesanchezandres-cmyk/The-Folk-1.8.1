package com.thefolk;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Guía de supervivencia: un libro escrito que se entrega equipado (en la mano izquierda) al aparecer.
 * Nunca se acumula: si el jugador ya tiene una, no se le da otra.
 */
public class FolkGuide {
    public static final String TAG = "thefolk_guide";

    private static final String[] PAGES_ES = {
            "§l§4Guía de supervivencia§r\n\nLas criaturas son invulnerables: no puedes matarlas. Sobrevives huyendo, aguantando su tiempo o frenándolas con el Segador y el Paralizador.\n\nTodas desaparecen solas tras un rato.",
            "§lAvisos§r\n\n\"Im C0MING\": The Thing acaba de aparecer.\n\"atrás tuyo\": date la vuelta.\n\"CORRE\": Man viene a por ti.\nUn silbido: evento Stalk.\nUn rugido: Uknow anda cerca.",
            "§lThe Folk§r\n\nFases 1 y 2: solo te mira o camina lento; no ataca.\nFase 3: 4,5 bloques/s, más lento que tú corriendo (5,6). Rompe bloques.\nFase 4: 8 bloques/s, no lo dejas atrás. Usa el escudo y las armas.",
            "§lStalk§r\n\nUna cara en una ventana y un silbido: sal de ahí.\nA los 4 s corre a 10 bloques/s hacia la puerta más cercana a ti y la rompe.\nDura 15 s. No rompe paredes.",
            "§lThe Thing§r\n\nDura 20 s y corre a 10 bloques/s. Cada golpe hace 12 corazones.\nAbre puertas, pero no rompe bloques ni trepa: un pilar de 5 bloques o más la frena.",
            "§lEvento Atrás§r\n\nThe Thing aparece detrás de ti, sin moverse. Si no la miras en 6 s, se va. Si la miras, se queda quieta 2 s y luego corre a 15 bloques/s.\nTen el Paralizador listo antes de girarte.",
            "§lMan§r\n\nAparece 1 bloque detrás de ti, quieto. Si no lo miras, se va a los 45 s.\nSi lo miras, a los 3 s corre a 12 bloques/s y hace 10 corazones por golpe.\nTrae más criaturas durante unos 3 minutos.",
            "§lUknow§r\n\nBestia con cuernos que mata todo lo que ve. Camina a 5,8 bloques/s (más que tú corriendo) y se abalanza al doble, a 11,6.\nSi te atrapa, quedas inmovilizado y recibes 3 corazones cada segundo.",
            "§lEscapar de Uknow§r\n\nPulsa ESPACIO 3 veces en 2 segundos.\nAl soltarte quedas con Sangrado: un veneno suave que te ralentiza y deja partículas rojas.\nTarda 1,5 s en volver a atacar.",
            "§lEvitar a Uknow§r\n\nSolo ataca a lo que ve. Un muro o una puerta cerrada lo detiene: no abre puertas ni rompe bloques.\nDesaparece a los 90 s.",
            "§lArmas§r\n\nSegador: golpea y la criatura vaga lenta y sin atacar 3 s (espera 5 s).\nParalizador: clic derecho, la paraliza 2 s (espera 8 s).\nAmbos liberan a quien Uknow sujeta.",
            "§lMás consejos§r\n\nEl escudo bloquea golpes de frente, pero no de espalda. La absorción y las manzanas doradas dan margen.\nSolo aparecen en el Overworld: el Nether y el End son seguros.",
            "§lLaboratorios§r\n\nEdificios de ladrillo con 2 cofres que pueden tener Segador o Paralizador.\nBúscalos con /locate structure thefolk:laboratory.\nTambién se fabrican en la mesa de crafteo."
    };

    private static final String[] PAGES_EN = {
            "§l§4Survival Guide§r\n\nThe creatures are invulnerable: you cannot kill them. You survive by running, outlasting them or slowing them down with the Reaper and the Paralyzer.\n\nThey all disappear on their own after a while.",
            "§lWarnings§r\n\n\"Im C0MING\": The Thing just appeared.\n\"behind you\": turn around.\n\"RUN\": Man is coming for you.\nA whistle: Stalk event.\nA roar: Uknow is close.",
            "§lThe Folk§r\n\nPhases 1 and 2: it only watches or walks slowly; it does not attack.\nPhase 3: 4.5 blocks/s, slower than you sprinting (5.6). It breaks blocks.\nPhase 4: 8 blocks/s, you cannot outrun it. Use a shield and the weapons.",
            "§lStalk§r\n\nA face in a window and a whistle: get out of there.\nAfter 4 s it runs at 10 blocks/s to the door closest to you and breaks it.\nLasts 15 s. It does not break walls.",
            "§lThe Thing§r\n\nLasts 20 s and runs at 10 blocks/s. Each hit deals 12 hearts.\nIt opens doors, but it does not break blocks or climb: a pillar 5 blocks high or more stops it.",
            "§lBehind event§r\n\nThe Thing appears behind you, without moving. If you do not look at it within 6 s, it leaves. If you look, it stays still for 2 s and then runs at 15 blocks/s.\nHave the Paralyzer ready before you turn around.",
            "§lMan§r\n\nHe appears 1 block behind you, motionless. If you do not look at him, he leaves after 45 s.\nIf you look, he runs at 12 blocks/s 3 s later and deals 10 hearts per hit.\nHe brings more creatures for about 3 minutes.",
            "§lUknow§r\n\nHorned beast that kills everything it sees. It walks at 5.8 blocks/s (faster than you sprinting) and pounces at double speed, 11.6.\nIf it catches you, you are immobilized and take 3 hearts every second.",
            "§lEscaping Uknow§r\n\nPress SPACE 3 times within 2 seconds.\nWhen it lets go you get Bleeding: a mild poison that slows you down and leaves red particles.\nIt needs 1.5 s before it can attack again.",
            "§lAvoiding Uknow§r\n\nIt only attacks what it sees. A wall or a closed door stops it: it does not open doors or break blocks.\nIt disappears after 90 s.",
            "§lWeapons§r\n\nReaper: hit and the creature wanders slowly without attacking for 3 s (5 s cooldown).\nParalyzer: right click, paralyzes for 2 s (8 s cooldown).\nBoth free whoever Uknow is holding.",
            "§lMore tips§r\n\nA shield blocks hits from the front, but not from behind. Absorption and golden apples give you margin.\nThey only appear in the Overworld: the Nether and the End are safe.",
            "§lLaboratories§r\n\nBrick buildings with 2 chests that may hold a Reaper or a Paralyzer.\nFind them with /locate structure thefolk:laboratory.\nThey can also be crafted."
    };

    public static boolean isGuide(ItemStack stack) {
        return stack.is(Items.WRITTEN_BOOK) && stack.hasTag() && stack.getTag().getBoolean(TAG);
    }

    private static CompoundTag buildTag(boolean english) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(TAG, true);
        tag.putString("title", english ? "Survival Guide" : "Guía de supervivencia");
        tag.putString("author", "The Folk");
        ListTag pages = new ListTag();
        for (String page : (english ? PAGES_EN : PAGES_ES)) {
            pages.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(page))));
        }
        tag.put("pages", pages);
        tag.putBoolean("resolved", true);
        tag.putInt("generation", 3);       // no se puede copiar
        return tag;
    }

    public static ItemStack create(boolean english) {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.setTag(buildTag(english));
        return book;
    }

    /** Devuelve la guía que ya tiene el jugador, o null. */
    public static ItemStack find(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isGuide(stack)) return stack;
        }
        return null;
    }

    /** Entrega la guía equipada en la mano izquierda, solo si no tiene ya una. */
    public static boolean giveIfMissing(ServerPlayer player) {
        if (find(player) != null) return false;
        give(player);
        return true;
    }

    private static void give(ServerPlayer player) {
        ItemStack book = create(FolkLang.isEnglish(player));
        if (player.getOffhandItem().isEmpty()) {
            player.setItemSlot(EquipmentSlot.OFFHAND, book);
        } else if (!player.getInventory().add(book)) {
            player.drop(book, false);
        }
    }

    /** Para /folkguide: si ya tiene la guía la pasa a su idioma; si no, se la da. Devuelve true si la actualizó. */
    public static boolean giveOrUpdate(ServerPlayer player) {
        ItemStack existing = find(player);
        if (existing != null) {
            existing.setTag(buildTag(FolkLang.isEnglish(player)));
            return true;
        }
        give(player);
        return false;
    }
}
