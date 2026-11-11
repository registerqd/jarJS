package jarjs.net;

/**
 * java.net.Proxy 兼容层。
 * 工业应用的入口类常直接引用 Proxy.NO_PROXY，必须有实体类。
 */
public class Proxy {

    public enum Type { DIRECT, HTTP, SOCKS }

    public static final Proxy NO_PROXY = new Proxy(Type.DIRECT, null);

    private final Type type;
    private final Object address;

    public Proxy(Type type, Object address) {
        this.type = type;
        this.address = address;
    }

    public Type type() { return type; }
    public Object address() { return address; }

    @Override
    public String toString() { return "Proxy[" + type + "]"; }
}
