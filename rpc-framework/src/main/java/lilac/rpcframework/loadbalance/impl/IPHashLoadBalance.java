package lilac.rpcframework.loadbalance.impl;

import com.google.common.hash.Hashing;
import lilac.rpcframework.loadbalance.AbstractLoadBalance;
import lilac.rpcframework.remote.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class IPHashLoadBalance extends AbstractLoadBalance {

    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest request, String clientAddress) {

        int hash = Hashing.murmur3_32_fixed().hashString(clientAddress, StandardCharsets.UTF_8).asInt();

        return serviceAddresses.get(Math.abs(hash) % serviceAddresses.size());


    }
}
