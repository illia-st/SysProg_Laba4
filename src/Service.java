import java.util.Collections;
import java.util.HashSet;

public class Service {
    public static <T> HashSet<T> newHashSet(T... objs) {
        HashSet<T> set = new HashSet<T>();
        Collections.addAll(set, objs);
        return set;
    }
}
