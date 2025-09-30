package lilac.rpcframework.extension;

/**
 * 用于存储支持SPI机制的所有类的实例对象
 * @param <T>
 */
public class Holder<T> {
    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
