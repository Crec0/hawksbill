package club.mindtech.mindbot.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TypedList {
    @SafeVarargs
    public static <T> List<T> of(T... elements) {
        List<T> list = new ArrayList<>(elements.length);
        Collections.addAll(list, elements);
        return list;
    }
}
