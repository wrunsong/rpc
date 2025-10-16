package lilac.rpcframework.serialize.impl;

import com.google.protobuf.*;
import lilac.rpcframework.api.dto.HelloDto;
import lilac.rpcframework.remote.dto.RpcRequest;
import lilac.rpcframework.remote.dto.RpcResponse;
import lilac.rpcframework.remote.proto.HelloDtoProto;
import lilac.rpcframework.remote.proto.RpcRequestProto;
import lilac.rpcframework.remote.proto.RpcResponseProto;
import lilac.rpcframework.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class Protobuf implements Serializer {

    // Protobuf 消息的类引用
    private static final Class<RpcRequestProto.RpcRequest> REQUEST_PROTO_CLASS = RpcRequestProto.RpcRequest.class;
    private static final Class<RpcResponseProto.RpcResponse> RESPONSE_PROTO_CLASS = RpcResponseProto.RpcResponse.class;
    @Override
    public byte[] serialize(Object o) {

        Message protoMessage = null;

        if (o instanceof RpcRequest) {
            // 转换成protobuf下的子类
            protoMessage = convertRequestToProto((RpcRequest) o);
        } else if (o instanceof RpcResponse) {
            protoMessage = convertResponseToProto((RpcResponse) o);
        } else {
            log.error("Protobuf serialize error, not supported obj: {}", o.getClass().getName());
            return null;
        }


        return protoMessage.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {

        try {
            // 1. 根据目标类型确定要反序列化的 Protobuf 消息类
            Class<? extends GeneratedMessage> protoClass;

            if (RpcRequest.class.isAssignableFrom(clazz)) {
                protoClass = RpcRequestProto.RpcRequest.class;
            } else if (RpcResponse.class.isAssignableFrom(clazz)) {
                protoClass = RpcResponseProto.RpcResponse.class;
            } else {
                log.error("Protobuf deserialize error, not supported obj: {}", clazz.getName());
                return null;
            }

            // 2. 反射调用 Protobuf 消息的 parseFrom 方法
            Method parseFromMethod = protoClass.getMethod("parseFrom", byte[].class);
            parseFromMethod.setAccessible(true);
            Object protoMessage = parseFromMethod.invoke(null, bytes);

            // 3. 将 Protobuf 消息转换回业务对象
            if (protoMessage instanceof RpcRequestProto.RpcRequest) {
                return (T) convertProtoToRequest((RpcRequestProto.RpcRequest) protoMessage, clazz);
            } else if (protoMessage instanceof RpcResponseProto.RpcResponse) {
                return (T) convertProtoToResponse((RpcResponseProto.RpcResponse) protoMessage, clazz);
            } else {
                log.error("Protobuf deserialize error, not supported proto class: {}", clazz.getName());
                return null;
            }
        } catch (Exception e) {
            log.error("Protobuf deserialization failed, {}", e.getMessage());
            return null;
        }
    }

    // deserialize: proto to pbj
    private RpcRequest convertProtoToRequest(RpcRequestProto.RpcRequest protoReq, Class<?> clazz) {
        List<Object> params = new ArrayList<>();

        for (int i = 0; i < protoReq.getParametersList().size(); i++) {
            Object param = convertProtoToObject(protoReq.getParameters(i),
                    protoReq.getParamTypes(i));
            params.add(param);
        }

        return RpcRequest.builder()
                .requestId(protoReq.getRequestId())
                .serviceName(protoReq.getServiceName())
                .methodName(protoReq.getMethodName())
                .parameters(params.toArray())
                .paramTypes(protoReq.getParamTypesList().toArray(new String[0]))
                .version(protoReq.getVersion())
                .group(protoReq.getGroup())
                .fullyExposeName(protoReq.getFullyExposeName())
                .returnType(protoReq.getReturnType())
                .build();
    }

    private RpcResponse convertProtoToResponse(RpcResponseProto.RpcResponse protoResp, Class<?> clazz) {
        return RpcResponse.builder()
                .requestId(protoResp.getRequestId())
                .code(protoResp.getCode())
                .message(protoResp.getMessage())
                .data(convertProtoToObject(protoResp.getData(), protoResp.getReturnType()))
                .returnType(protoResp.getReturnType())
                .serviceAddress(protoResp.getServiceAddress())
                .build();
    }



    private Object convertProtoToObject(Any any, String className) {
        Object obj = null;

        try {
            Class<?> businessClass = Class.forName(className);


            if (businessClass.equals(String.class)) {
                StringValue unpack = any.unpack(StringValue.class);
                obj = unpack.getValue();
            } else if (businessClass.equals(Integer.class)) {
                Int32Value unpack = any.unpack(Int32Value.class);
                obj = unpack.getValue();
            } else if (businessClass.equals(HelloDto.class)) {
                HelloDtoProto.HelloDto unpack = any.unpack(HelloDtoProto.HelloDto.class);
                obj = convertProtoToHelloDto(unpack);
            }
            return obj;
        } catch (Exception e) {
            log.error("Convert protobuf to object failed, {}", e.getMessage());
            return null;
        }
    }

    // serialize: obj to proto
    private RpcRequestProto.RpcRequest convertRequestToProto(RpcRequest rpcRequest) {

        return RpcRequestProto.RpcRequest.newBuilder()
                .setRequestId(rpcRequest.getRequestId())
                .setServiceName(rpcRequest.getServiceName())
                .setMethodName(rpcRequest.getMethodName())
                .addAllParameters(Arrays.stream(rpcRequest.getParameters()).map(this::convertObjectToProto).toList())
                .addAllParamTypes(Arrays.asList(rpcRequest.getParamTypes()))
                .setVersion(rpcRequest.getVersion())
                .setGroup(rpcRequest.getGroup())
                .setFullyExposeName(rpcRequest.getFullyExposeName())
                .setReturnType(rpcRequest.getReturnType())
                .build();
    }

    private RpcResponseProto.RpcResponse convertResponseToProto(RpcResponse rpcResponse) {

        return RpcResponseProto.RpcResponse.newBuilder()
                .setRequestId(rpcResponse.getRequestId())
                .setCode(rpcResponse.getCode())
                .setMessage(rpcResponse.getMessage())
                .setData(convertObjectToProto(rpcResponse.getData()))
                .setReturnType(rpcResponse.getReturnType())
                .setServiceAddress(rpcResponse.getServiceAddress())
                .build();
    }


    private Any convertObjectToProto(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return Any.pack(StringValue.of((String) obj));
        } else if (obj instanceof Integer) {
            return Any.pack(Int32Value.of((Integer) obj));
        } else if (obj instanceof HelloDto) {
            return Any.pack(convertHelloDtoToProto((HelloDto) obj));
        } else {
            log.error("Obj not supported: {}", obj.getClass());
            return null;
        }
    }

    private HelloDto convertProtoToHelloDto(HelloDtoProto.HelloDto helloDtoProto) {
        return HelloDto.builder()
                .message(helloDtoProto.getMessage())
                .description(helloDtoProto.getDescription())
                .build();
    }

    private HelloDtoProto.HelloDto convertHelloDtoToProto(HelloDto helloDto) {
        return HelloDtoProto.HelloDto.newBuilder()
                .setMessage(helloDto.getMessage())
                .setDescription(helloDto.getDescription())
                .build();
    }
}
