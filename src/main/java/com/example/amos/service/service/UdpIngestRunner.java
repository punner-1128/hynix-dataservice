package com.example.amos.service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import com.example.amos.service.dto.UdpData;
import com.example.amos.service.repository.UdpBatchRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UdpIngestRunner implements SmartLifecycle {

    private static final int UDP_PORT = 3600;
    private static final int FLUSH_INTERVAL_SECONDS = 10;
    private static final int DB_BATCH_SIZE = 1000;
    private static final long TIB_IDLE_NANOS = TimeUnit.MILLISECONDS.toNanos(1);

    private final TibPublisher tibPublisher;
    private final UdpBatchRepository udpBatchRepository;
    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<String, UdpData> bufferMap = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<UdpData> tibQueue = new ConcurrentLinkedQueue<>();
    private final AtomicLong sequence = new AtomicLong();

    private volatile boolean running;
    private ScheduledExecutorService scheduler;
    private Thread listenerThread;
    private Thread tibPublisherThread;
    private UdpDataReceiver udpDataReceiver;

    @Override
    public void start() {
        if (running) {
            return;
        }

        running = true;
        udpDataReceiver = new UdpDataReceiver(UDP_PORT, objectMapper, this::handleIncomingData);
        listenerThread = new Thread(udpDataReceiver, "udp-listener");
        listenerThread.setDaemon(true);
        listenerThread.start();

        tibPublisherThread = new Thread(this::publishLoop, "udp-tib-publisher");
        tibPublisherThread.setDaemon(true);
        tibPublisherThread.start();

        scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("udp-db-writer"));
        scheduler.scheduleWithFixedDelay(this::flushToOracle, FLUSH_INTERVAL_SECONDS, FLUSH_INTERVAL_SECONDS, TimeUnit.SECONDS);
        log.info("UDP ingest runner started");
    }

    @Override
    public void stop() {
        running = false;

        if (udpDataReceiver != null) {
            udpDataReceiver.stop();
        }

        if (tibPublisherThread != null) {
            tibPublisherThread.interrupt();
            tibPublisherThread = null;
        }

        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        log.info("UDP ingest runner stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    private void handleIncomingData(UdpData udpData) {
        String key = System.nanoTime() + "-" + sequence.incrementAndGet();
        bufferMap.put(key, udpData);
        tibQueue.offer(udpData);
    }

    private void flushToOracle() {
        if (bufferMap.isEmpty()) {
            return;
        }

        List<Map.Entry<String, UdpData>> snapshot = new ArrayList<>(bufferMap.entrySet());
        List<UdpData> batchData = new ArrayList<>(snapshot.size());
        for (Map.Entry<String, UdpData> entry : snapshot) {
            batchData.add(entry.getValue());
        }

        try {
            int startIndex = 0;
            while (startIndex < batchData.size()) {
                int endIndex = Math.min(startIndex + DB_BATCH_SIZE, batchData.size());
                udpBatchRepository.batchInsert(batchData.subList(startIndex, endIndex));
                startIndex = endIndex;
            }

            for (Map.Entry<String, UdpData> entry : snapshot) {
                bufferMap.remove(entry.getKey(), entry.getValue());
            }

            log.info("Inserted {} UDP records into Oracle", batchData.size());
        } catch (Exception e) {
            log.error("Failed to batch insert UDP data into Oracle. Data will remain in memory for retry.", e);
        }
    }

    private void publishLoop() {
        while (running || !tibQueue.isEmpty()) {
            UdpData udpData = tibQueue.poll();
            if (udpData == null) {
                LockSupport.parkNanos(TIB_IDLE_NANOS);
                continue;
            }

            tibPublisher.publish(udpData);
        }
    }

    private static class NamedThreadFactory implements ThreadFactory {

        private final String threadName;

        private NamedThreadFactory(String threadName) {
            this.threadName = threadName;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, threadName);
            thread.setDaemon(true);
            return thread;
        }
    }
}
