package lilac.rpcframework.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SerializationType {
    SERIALIZATION((byte) 0x00, "serialization"),
    KYRO((byte) 0x01, "kyro"),
    HESSIAN((byte) 0x02, "hessian"),
    PROTOSTUFF((byte) 0x03, "protostuff"),
    PROTOBUF((byte) 0x04, "protobuf");

    private final byte code;
    private final String type;

    public static String getType(byte code) {
        for (SerializationType type : SerializationType.values()) {
            if (type.getCode() == code) {
                return type.getType();
            }
        }
        return null;
    }
}
