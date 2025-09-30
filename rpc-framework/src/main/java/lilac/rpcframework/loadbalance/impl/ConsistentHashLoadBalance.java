package lilac.rpcframework.loadbalance.impl;

import lilac.rpcframework.loadbalance.AbstractLoadBalance;
import lilac.rpcframework.remote.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ConsistentHashLoadBalance extends AbstractLoadBalance {

    // rpcServiceName -> selector
    private static final ConcurrentHashMap<String, ConsistentSelector> selectors = new ConcurrentHashMap<>();

    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest request) {
        int hashCode = System.identityHashCode(serviceAddresses);

        String rpcServiceName = request.getRpcServiceName();
        ConsistentSelector selector = selectors.get(rpcServiceName);

        if (selector == null || selector.identityHashCode != hashCode) {
            selectors.put(rpcServiceName, new ConsistentSelector(serviceAddresses, 4, hashCode));
            selector = selectors.get(rpcServiceName);
        }

        // 用调用的服务名称+参数作为键进行选取服务IP
        return selector.select(rpcServiceName + Arrays.toString(request.getParameters()));
    }


    /**
     * 内部类，一组服务对应一个内部类，identityHashCode是用所有能提供服务的IP地址算出来的
     */
    static class ConsistentSelector {
        private final TreeMap<Long, String> virtualInvokers;
        private final int identityHashCode;

        public ConsistentSelector(List<String> invokers, int replicaNum, int identityHashCode) {
            this.virtualInvokers = new TreeMap<>();
            this.identityHashCode = identityHashCode;

            // invoker : ip + port
            for (String invoker : invokers) {
                for (int i = 0; i < replicaNum / 4; i++){
                    byte[] digest = md5(invoker + i);
                    for (int h = 0; h < 4; h++) {
                        long m = 0;
                        if (digest != null) {
                            m = hash(digest, h);
                        }
                        virtualInvokers.put(m, invoker);
                    }
                }
            }
        }

        /**
         * 计算md5
         * @param key
         * @return
         */
        static byte[] md5(String key) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                log.error("md5 error with key: {}, {}", key, e.getMessage());
                return null;
            }
            return md.digest();
        }

        /**
         * 由一个md5，算出来4个哈希，代表了一致性哈希的虚拟结点
         * @param digest
         * @param idx
         * @return
         */
        static long hash(byte[] digest, int idx) {
            return ((long) (digest[3 + idx * 4] & 255) << 24 | (long) (digest[2 + idx * 4] & 255) << 16 | (long) (digest[1 + idx * 4] & 255) << 8 | (long) (digest[idx * 4] & 255)) & 4294967295L;
        }

        public String select(String rpcRequestKey) {
            byte[] bytes = md5(rpcRequestKey);
            if (bytes != null) {
                return selectForKey(hash(bytes, 0));
            }
            return null;
        }

        /**
         * 一致性哈希+虚拟结点，找到圆圈上请求所对应的哈希值的下一个哈希
         * @param hashcode 由请求算出来的哈希值
         * @return
         */
        public String selectForKey(long hashcode) {
            Map.Entry<Long, String> entry = virtualInvokers.tailMap(hashcode, true).firstEntry();

            if (entry == null) {
                entry = virtualInvokers.firstEntry();
            }
            return entry.getValue();
        }

    }
}
