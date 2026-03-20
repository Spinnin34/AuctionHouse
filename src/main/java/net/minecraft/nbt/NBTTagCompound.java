package net.minecraft.nbt;

import java.util.Set;

/**
 * Stub for compilation - provided by server at runtime.
 */
public class NBTTagCompound implements NBTBase {
    // Check if key exists with type
    public boolean b(String key, int type) { return false; }

    // Get compound
    public NBTTagCompound p(String key) { return null; }

    // Get string
    public String l(String key) { return null; }

    // Get byte
    public byte f(String key) { return 0; }

    // Get short
    public short g(String key) { return 0; }

    // Get int
    public int h(String key) { return 0; }

    // Get list
    public NBTTagList c(String key, int type) { return null; }

    // Get keys
    public Set<String> e() { return Set.of(); }

    // Has key
    public boolean e(String key) { return false; }

    // Get tag
    public NBTBase c(String key) { return null; }

    // Remove key
    public void r(String key) {}

    // Put string
    public void a(String key, String value) {}

    // Put byte
    public void a(String key, byte value) {}

    // Put short
    public void a(String key, short value) {}

    // Put int
    public void a(String key, int value) {}

    // Put long
    public void a(String key, long value) {}

    // Put float
    public void a(String key, float value) {}

    // Put double
    public void a(String key, double value) {}

    // Put byte array
    public void a(String key, byte[] value) {}

    // Put int array
    public void a(String key, int[] value) {}

    // Put long array
    public void a(String key, long[] value) {}

    // Put NBTBase (compound, list, etc)
    public void a(String key, NBTBase value) {}

    @Override
    public String toString() { return "{}"; }
}

