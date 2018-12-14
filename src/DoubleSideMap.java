import java.util.ArrayList;

class DoubleSideMap<K, V> {
    private ArrayList<K> value1;
    private ArrayList<V> value2;

    DoubleSideMap() {
        value1 = new ArrayList<>();
        value2 = new ArrayList<>();
    }

    void put(K obj1, V obj2) {
        if (!value1.contains(obj1) && !value2.contains(obj2)) {
            value1.add(obj1);
            value2.add(obj2);
        }
    }

    K get1Arg(V obj2) {
        for (int i = 0; i < value2.size(); i++) {
            if (obj2.equals(value2.get(i))) {
                return value1.get(i);
            }
        }
        return null;
    }

    void clear() {
        value1.clear();
        value2.clear();
    }

}
