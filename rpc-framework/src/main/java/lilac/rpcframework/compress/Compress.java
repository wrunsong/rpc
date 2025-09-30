package lilac.rpcframework.compress;

import lilac.rpcframework.extension.SPI;

@SPI
public interface Compress {

    byte[] compress(byte[] bytes);

    byte[] decompress(byte[] bytes);
}
