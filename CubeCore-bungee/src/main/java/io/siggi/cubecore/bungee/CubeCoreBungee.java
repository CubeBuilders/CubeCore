package io.siggi.cubecore.bungee;

import com.google.gson.GsonBuilder;
import io.siggi.cubecore.CubeCore;
import io.siggi.cubecore.CubeCorePlugin;
import io.siggi.cubecore.pluginmessage.OutboundPluginMessageBuilder;
import io.siggi.nbt.NBTCompound;
import io.siggi.nbt.NBTTool;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class CubeCoreBungee extends Plugin implements CubeCorePlugin {
    private static CubeCoreBungee instance;
    private CubeCore cubeCore;

    public static CubeCoreBungee getInstance() {
        return instance;
    }

    /**
     * Open a book for a player.
     *
     * @param p
     * @param pages
     */
    public static void openBook(ProxiedPlayer p, List<? extends BaseComponent> pages) {
        NBTCompound nbtBook = CubeCore.createBook("CubeCore", "CubeCore", pages);
        byte[] serializedBook = NBTTool.serialize(nbtBook);
        CubeCoreMessengerBungee.send(p, new OutboundPluginMessageBuilder("cubecore:openBook").write((out) -> {
            out.writeInt(serializedBook.length);
            out.write(serializedBook);
        }));
    }

    public static void refreshCommands(ProxiedPlayer p) {
        CubeCoreMessengerBungee.send(p, new OutboundPluginMessageBuilder("cubecore:refreshCommands"));
    }

    @Override
    public void onEnable() {
        instance = this;
        this.cubeCore = new CubeCore(this, getDataFolder());
        CacheUpdaterBungee cacheUpdater = new CacheUpdaterBungee(CubeCore.getUserCache());
        getProxy().getPluginManager().registerListener(this, CubeCoreMessengerBungee.getListener());
        getProxy().getPluginManager().registerListener(this, cacheUpdater);

        CubeCoreMessengerBungee.setHandler("cubecore:chatAsPlayer", (p, subChannel, in) -> {
            p.chat(in.readUTF());
        });
    }

    @Override
    public void registerTypeAdapters(GsonBuilder builder) {
    }
}
