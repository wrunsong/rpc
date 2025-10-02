package lilac.rpcframework.remote.transport.netty.client.utils;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * channel缓存
 */
@Slf4j
public class ChannelProvider {

    // ip:port -> channel
    private static Map<String, Channel> channelMap = new ConcurrentHashMap<>();

    private ChannelProvider() {}

    /**
     * 获取channel
     * @param address
     * @return
     */
    public static Channel getChannel(InetSocketAddress address) {
        String key = getKey(address);

        if (channelMap.containsKey(key)) {
            Channel channel = channelMap.get(key);
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channelMap.remove(key);
            }
        }
        return null;
    }

    /**
     * 缓存channel
     * @param address
     * @param channel
     */
    public static void putChannel(InetSocketAddress address, Channel channel) {
        if (address == null) {
            log.error("address is null");
            return;
        }
        if (channel == null || !channel.isActive()) {
            log.error("invalid channel");
            return;
        }
        String key = getKey(address);
        channelMap.put(key, channel);
    }

    /**
     * 移除channel
     * @param address
     * @return
     */
    public static Channel removeChannel(InetSocketAddress address) {
        String key = getKey(address);
        return channelMap.remove(key);
    }


    private static String getKey(InetSocketAddress address) {
        return address.getHostString() + ":" + address.getPort();
    }

}
