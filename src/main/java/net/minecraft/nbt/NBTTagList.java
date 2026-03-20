package net.minecraft.nbt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Stub for compilation - provided by server at runtime.
 */
public class NBTTagList implements NBTBase, Iterable<NBTBase> {
    private final List<NBTBase> list = new ArrayList<>();

    public NBTTagCompound a(int index) { return null; }
    public NBTTagList c(String key, int type) { return this; }
    public void d(int index, NBTBase element) {}

    public NBTBase get(int index) { return list.get(index); }
    public int size() { return list.size(); }
    public boolean add(Object element) { return list.add((NBTBase) element); }
    public boolean addAll(java.util.Collection<?> c) { return false; }

    public int indexOf(Object o) { return list.indexOf(o); }

    @Override
    public Iterator<NBTBase> iterator() { return list.iterator(); }
}
