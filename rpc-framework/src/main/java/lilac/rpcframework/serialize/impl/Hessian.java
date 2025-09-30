package lilac.rpcframework.serialize.impl;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import lilac.rpcframework.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Slf4j
public class Hessian implements Serializer {
    @Override
    public byte[] serialize(Object o) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            HessianOutput hessianOutput = new HessianOutput(out);
            hessianOutput.writeObject(o);

            return out.toByteArray();
        } catch (Exception e) {
            log.error("Hessian serialize error: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            HessianInput hessianInput = new HessianInput(in);
            Object o = hessianInput.readObject();

            return clazz.cast(o);
        } catch (Exception e) {
            log.error("Hessian deserialize error: {}", e.getMessage());
            return null;
        }
    }
}
