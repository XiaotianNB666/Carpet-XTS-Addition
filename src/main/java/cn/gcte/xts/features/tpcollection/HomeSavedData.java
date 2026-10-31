package cn.gcte.xts.features.tpcollection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeSavedData extends SavedData {

    public static final Codec<PositionRecord> ENTRY_CODEC = RecordCodecBuilder.create(i ->
            i.group(
                    Codec.DOUBLE.fieldOf("x").forGetter(d -> d.pos().x()),
                    Codec.DOUBLE.fieldOf("y").forGetter(d -> d.pos().y()),
                    Codec.DOUBLE.fieldOf("z").forGetter(d -> d.pos().z()),
                    Codec.STRING.fieldOf("dim").forGetter(PositionRecord::dimToString),
                    Codec.FLOAT.fieldOf("yaw").forGetter(PositionRecord::yaw),
                    Codec.FLOAT.fieldOf("pitch").forGetter(PositionRecord::pitch)
            ).apply(i, (x, y, z, dim, yaw, pitch) ->
                    new PositionRecord(new Vec3(x, y, z), PositionRecord.dimFromString(dim), yaw, pitch))
    );

//    private static final Codec<Map<UUID, HomeManager.PositionRecord>> MAP_CODEC =
//            Codec.unboundedMap(UUIDUtil.CODEC, ENTRY_CODEC);

//    public static final Codec<HomeSavedData> CODEC = MAP_CODEC.xmap(
//            HomeSavedData::new,
//            HomeSavedData::homes
//    );

    private static final Codec<Map<String, PositionRecord>> STRING_MAP_CODEC =
            Codec.unboundedMap(Codec.STRING, ENTRY_CODEC);

    public static final Codec<HomeSavedData> CODEC = STRING_MAP_CODEC.xmap(
            m -> {
                Map<UUID, PositionRecord> map = new HashMap<>();
                m.forEach((k, v) -> map.put(UUID.fromString(k), v));
                return new HomeSavedData(map);
            },
            data -> {
                Map<String, PositionRecord> map = new HashMap<>();
                data.homes.forEach((k, v) -> map.put(k.toString(), v));
                return map;
            }
    );

    public static final SavedDataType<HomeSavedData> TYPE = new SavedDataType<>(
            //#if MC<260100
            "xts_homes",
            //#else
            //$$ net.minecraft.resources.Identifier.fromNamespaceAndPath(cn.gcte.xts.CarpetXTSAddition.MOD_ID, "xts_home"),
            //#endif
            HomeSavedData::new,
            CODEC,
            DataFixTypes.PLAYER
    );

    private final Map<UUID, PositionRecord> homes = new HashMap<>();

    public HomeSavedData() {
    }

    public HomeSavedData(Map<UUID, PositionRecord> homes) {
        this.homes.putAll(homes);
    }

    public Map<UUID, PositionRecord> homes() {
        return homes;
    }

    public boolean hasHome(UUID uuid) {
        return homes.containsKey(uuid);
    }

    public PositionRecord getHome(UUID uuid) {
        return homes.get(uuid);
    }

    public void setHome(UUID uuid, PositionRecord data) {
        homes.put(uuid, data);
        setDirty();
    }

    public void removeHome(UUID uuid) {
        homes.remove(uuid);
        setDirty();
    }

    public static HomeSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }
}