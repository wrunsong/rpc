package lilac.rpcframework.serialize;

import lilac.rpcframework.extension.SPI;

@SPI
public interface Serializer {

    /**
     * 序列化
     * @param o
     * @return
     */
    byte[] serialize(Object o);


    /**
     * 反序列化
     * @param bytes
     * @param clazz
     * @return
     * @param <T>
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
