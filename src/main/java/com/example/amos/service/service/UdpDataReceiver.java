package com.example.amos.service.service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import com.example.amos.service.dto.UdpData;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UdpDataReceiver implements Runnable {

    private static final int BUFFER_SIZE = 65535;

    private final int port;
    private final ObjectMapper objectMapper;
    private final Consumer<UdpData> udpDataConsumer;
    private volatile boolean running = true;
    private DatagramSocket socket;

    public UdpDataReceiver(int port, ObjectMapper objectMapper, Consumer<UdpData> udpDataConsumer) {
        this.port = port;
        this.objectMapper = objectMapper;
        this.udpDataConsumer = udpDataConsumer;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[BUFFER_SIZE];

        try (DatagramSocket datagramSocket = new DatagramSocket(port)) {
            this.socket = datagramSocket;
            datagramSocket.setReceiveBufferSize(BUFFER_SIZE);
            log.info("UDP receiver started on port {}", port);

            while (running) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    datagramSocket.receive(packet);

                    String payload = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
                    UdpData udpData = objectMapper.readValue(payload, UdpData.class);
                    udpDataConsumer.accept(udpData);
                } catch (SocketException e) {
                    if (running) {
                        log.error("UDP socket error", e);
                    }
                } catch (Exception e) {
                    log.error("Failed to process UDP payload", e);
                }
            }
        } catch (IOException e) {
            log.error("Failed to start UDP receiver on port {}", port, e);
        } finally {
            log.info("UDP receiver stopped");
        }
    }

    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
