package model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Test {
    public static void main(String[] args) {
        Set<Integer> set = new HashSet<>();
        for (int i = 40; i < 55; i++) {
            Obj o = new Obj(i);
            set.add(o.hashCode() % 16);
            System.out.println("HashCode() = " + o.hashCode() + "; " + o.hashCode() % 16);
        }
        System.out.println();
        System.out.println(set.size());
    }
    public static class Obj {
        String name;

        public Obj(int id) {
            this.name = id + "";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Obj obj = (Obj) o;
            return name == obj.name;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}
