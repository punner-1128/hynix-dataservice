package com.example.amos.service.service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Component;

import com.example.amos.service.dto.UdpData;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TibPublisher {

    private static final String DAEMON = "tcp:7500";
    private static final String SERVICE = "1000";
    private static final String SUBJECT = "test.data";

    private volatile boolean initialized;
    private volatile boolean available = true;
    private final AtomicBoolean initializationStarted = new AtomicBoolean(false);
    private Object transport;
    private Method sendMethod;

    public void publish(UdpData udpData) {
        try {
            initializeIfNecessary();
            if (!initialized || !available) {
                return;
            }

            Object message = createMessage();
            addField(message, "field1", udpData.getField1());
            addField(message, "field2", udpData.getField2());
            addField(message, "field3", udpData.getField3());
            addField(message, "field4", udpData.getField4());
            addField(message, "field5", udpData.getField5() == null ? null : udpData.getField5().toString());
            sendMethod.invoke(transport, message);
        } catch (Exception e) {
            log.error("Failed to publish UDP data to TIBCO RV", e);
        }
    }

    @PreDestroy
    public void close() {
        if (!initialized || !available) {
            return;
        }

        try {
            Class<?> tibrvClass = Class.forName("com.tibco.tibrv.Tibrv");
            Method closeMethod = tibrvClass.getMethod("close");
            closeMethod.invoke(null);
        } catch (Exception e) {
            log.warn("Failed to close TIBCO RV cleanly", e);
        }
    }

    private void initializeIfNecessary() {
        if (initialized || !available) {
            return;
        }

        if (!initializationStarted.compareAndSet(false, true)) {
            return;
        }

        try {
            Class<?> tibrvClass = Class.forName("com.tibco.tibrv.Tibrv");
            Field implNativeField = tibrvClass.getField("IMPL_NATIVE");
            Method openMethod = tibrvClass.getMethod("open", int.class);
            openMethod.invoke(null, implNativeField.getInt(null));

            Class<?> transportClass = Class.forName("com.tibco.tibrv.TibrvRvdTransport");
            transport = transportClass
                    .getConstructor(String.class, String.class, String.class)
                    .newInstance(SERVICE, "", DAEMON);

            Class<?> msgClass = Class.forName("com.tibco.tibrv.TibrvMsg");
            sendMethod = transportClass.getMethod("send", msgClass);
            initialized = true;
        } catch (Exception e) {
            available = false;
            log.warn("TIBCO RV library is not available or failed to initialize. UDP ingest will continue without publish.", e);
        }
    }

    private Object createMessage() throws Exception {
        Class<?> msgClass = Class.forName("com.tibco.tibrv.TibrvMsg");
        Object message = msgClass.getConstructor().newInstance();
        Method setSendSubjectMethod = msgClass.getMethod("setSendSubject", String.class);
        setSendSubjectMethod.invoke(message, SUBJECT);
        return message;
    }

    private void addField(Object message, String fieldName, Object value) throws Exception {
        if (value == null) {
            return;
        }

        Method updateMethod = message.getClass().getMethod("update", String.class, Object.class);
        updateMethod.invoke(message, fieldName, value);
    }
}
