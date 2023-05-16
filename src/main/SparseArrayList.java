package main;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;

public class SparseArrayList<T> {

    private ArrayList<T> listHead = new ArrayList<>();
    private TreeMap<Integer, T> listTail = new TreeMap<>();

    public SparseArrayList() {}

    public SparseArrayList(List<T> old) {
        listHead = new ArrayList<>(old);
    }

    public boolean add(T t) {
        return listHead.add(t);
    }

    public int size() {
        return listHead.size() + listTail.size();
    }

    public T get(int index) {
        if (index < listHead.size()) {
            return listHead.get(index);
        }
        if (listTail.containsKey(index)) {
            return listTail.get(index);
        }
        return (T)ATOMValue.NULL();
    }

    public T set(int index, T value) {
        if (index > listHead.size()) {
            listTail.put(index, value);
        } else if (listHead.size() == index) {
            add(value);
        } else {
            listHead.set(index, value);
        }
        return value;
    }

    public <R> SparseArrayList<R> map(Function<? super T,R> mapper) {
        SparseArrayList<R> toReturn = new SparseArrayList<>();
        for (int i=0;i<listHead.size();i++) {
            toReturn.add(mapper.apply(listHead.get(i)));
        }
        for (Integer i : listTail.navigableKeySet()) {
            toReturn.listTail.put(i, mapper.apply(listTail.get(i)));
        }
        return toReturn;
    }

    public List<T> toList() {
        ArrayList<T> toReturn = new ArrayList<>(listHead);
        for (Integer i : listTail.navigableKeySet()) {
            toReturn.add(listTail.get(i));
        }
        return toReturn;
    }

    public String toString() {
        StringBuilder toString = new StringBuilder("[");
        if (listHead.size()==0 && listTail.size()==0) {
            return "[]";
        }
        for (T t : listHead) {
            toString.append(t.toString()).append(",");
        }
        for (Integer i : listTail.navigableKeySet()) {
            toString.append(listTail.get(i).toString()).append(",");
        }
        // Delete the trailing comma
        toString.deleteCharAt(toString.length()-1);
        toString.append("]");
        return toString.toString();
    }

}
