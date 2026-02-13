package framework.utilitaire;

import jakarta.servlet.http.HttpSession;

import java.util.*;

/**
 * A Map<String, Object> facade over HttpSession attributes.
 * Changes to this map are immediately reflected in the underlying HttpSession.
 */
public class SessionMap implements Map<String, Object> {
    private final HttpSession session;

    public SessionMap(HttpSession session) {
        this.session = Objects.requireNonNull(session, "session");
    }

    @Override
    public int size() {
        int count = 0;
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) { names.nextElement(); count++; }
        return count;
    }

    @Override
    public boolean isEmpty() {
        return !session.getAttributeNames().hasMoreElements();
    }

    @Override
    public boolean containsKey(Object key) {
        if (!(key instanceof String)) return false;
        return session.getAttribute((String) key) != null || keys().contains(key);
    }

    @Override
    public boolean containsValue(Object value) {
        for (Object v : values()) {
            if (Objects.equals(v, value)) return true;
        }
        return false;
    }

    @Override
    public Object get(Object key) {
        if (!(key instanceof String)) return null;
        return session.getAttribute((String) key);
    }

    @Override
    public Object put(String key, Object value) {
        Object old = session.getAttribute(key);
        session.setAttribute(key, value);
        return old;
    }

    @Override
    public Object remove(Object key) {
        if (!(key instanceof String)) return null;
        String k = (String) key;
        Object old = session.getAttribute(k);
        session.removeAttribute(k);
        return old;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        for (Entry<? extends String, ? extends Object> e : m.entrySet()) {
            session.setAttribute(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear() {
        for (String name : keys()) {
            session.removeAttribute(name);
        }
    }

    private List<String> keys() {
        List<String> list = new ArrayList<>();
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) list.add(names.nextElement());
        return list;
    }

    @Override
    public Set<String> keySet() {
        return new LinkedHashSet<>(keys());
    }

    @Override
    public Collection<Object> values() {
        List<Object> list = new ArrayList<>();
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            list.add(session.getAttribute(names.nextElement()));
        }
        return list;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        LinkedHashSet<Entry<String, Object>> set = new LinkedHashSet<>();
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            final String k = names.nextElement();
            final Object v = session.getAttribute(k);
            set.add(new AbstractMap.SimpleEntry<>(k, v) {
                @Override
                public Object setValue(Object value) {
                    Object old = session.getAttribute(k);
                    session.setAttribute(k, value);
                    return old;
                }
            });
        }
        return set;
    }
}
