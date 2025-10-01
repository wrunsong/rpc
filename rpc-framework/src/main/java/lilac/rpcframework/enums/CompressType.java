package lilac.rpcframework.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CompressType {
    GZIP((byte) 0x01, "gzip");

    private final byte code;
    private final String name;

    public static String getNameByCode(byte code) {
        for (CompressType compressType : CompressType.values()) {
            if (compressType.code == code) {
                return compressType.name;
            }
        }
        return null;
    }

    public static byte getCodeByName(String name) {
        for (CompressType compressType : CompressType.values()) {
            if (compressType.name.equals(name)) {
                return compressType.code;
            }
        }
        return 0;
    }
}
