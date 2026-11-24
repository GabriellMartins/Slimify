package com.gabrielmartins.slimify.importer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import com.flowpowered.nbt.ByteArrayTag;
import com.flowpowered.nbt.ByteTag;
import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.IntArrayTag;
import com.flowpowered.nbt.IntTag;
import com.flowpowered.nbt.ListTag;
import com.flowpowered.nbt.StringTag;
import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.TagType;
import com.flowpowered.nbt.stream.NBTOutputStream;

import lombok.experimental.UtilityClass;

@UtilityClass
public class WorldFixture {

    private static final int GZIP = 1;
    private static final int ZLIB = 2;
    private static final int SECTOR = 4096;

    public Path create(Path root, String name) throws IOException {
        Path world = Files.createDirectories(root.resolve(name));
        Files.write(world.resolve("level.dat"), nbt(GZIP, levelData()));
        Path region = Files.createDirectories(world.resolve("region"));
        Files.write(region.resolve("r.0.0.mca"), regionWithOneChunk());
        return world;
    }

    public Path empty(Path root, String name) throws IOException {
        Path world = Files.createDirectories(root.resolve(name));
        Files.write(world.resolve("level.dat"), nbt(GZIP, levelData()));
        Path region = Files.createDirectories(world.resolve("region"));
        Files.write(region.resolve("r.0.0.mca"), new byte[SECTOR * 2]);
        return world;
    }

    public Path corrupt(Path root, String name) throws IOException {
        Path world = Files.createDirectories(root.resolve(name));
        Files.write(world.resolve("level.dat"), new byte[] {1, 2, 3});
        Files.createDirectories(world.resolve("region"));
        return world;
    }

    private Tag<?> levelData() {
        CompoundMap data = new CompoundMap();
        data.put(new StringTag("LevelName", "fixture"));
        data.put(new IntTag("version", 19133));
        CompoundMap root = new CompoundMap();
        root.put(new CompoundTag("Data", data));
        return new CompoundTag("", root);
    }

    private byte[] regionWithOneChunk() throws IOException {
        byte[] chunk = nbt(ZLIB, chunk());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeInt((2 << 8) | 1);
        out.write(new byte[SECTOR - 4]);
        out.write(new byte[SECTOR]);
        out.writeInt(chunk.length + 1);
        out.writeByte(ZLIB);
        out.write(chunk);
        int padding = SECTOR - (chunk.length + 5) % SECTOR;
        out.write(new byte[padding]);
        return bytes.toByteArray();
    }

    private Tag<?> chunk() {
        byte[] blocks = new byte[4096];
        Arrays.fill(blocks, (byte) 1);
        CompoundMap section = new CompoundMap();
        section.put(new ByteTag("Y", (byte) 0));
        section.put(new ByteArrayTag("Blocks", blocks));
        section.put(new ByteArrayTag("Data", new byte[2048]));
        section.put(new ByteArrayTag("BlockLight", new byte[2048]));
        section.put(new ByteArrayTag("SkyLight", new byte[2048]));

        CompoundMap level = new CompoundMap();
        level.put(new IntTag("xPos", 0));
        level.put(new IntTag("zPos", 0));
        level.put(new ByteArrayTag("Biomes", new byte[256]));
        level.put(new IntArrayTag("HeightMap", new int[256]));
        level.put(new ListTag<>("Sections", TagType.TAG_COMPOUND, List.of(new CompoundTag("", section))));
        level.put(new ListTag<>("Entities", TagType.TAG_COMPOUND, List.<CompoundTag>of()));
        level.put(new ListTag<>("TileEntities", TagType.TAG_COMPOUND, List.<CompoundTag>of()));

        CompoundMap root = new CompoundMap();
        root.put(new CompoundTag("Level", level));
        return new CompoundTag("", root);
    }

    private byte[] nbt(int compression, Tag<?> tag) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTOutputStream out = new NBTOutputStream(bytes, compression)) {
            out.writeTag(tag);
        }
        return bytes.toByteArray();
    }
}
