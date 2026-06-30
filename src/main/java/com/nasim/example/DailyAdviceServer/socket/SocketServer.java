package com.nasim.example.DailyAdviceServer.socket;

import com.nasim.example.DailyAdviceServer.socket.messageGenerator.MessageGenerator;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
public class SocketServer implements AutoCloseable {

    private final SocketChannel clientChannel;
    private final MessageGenerator messageGenerator;

    private volatile boolean running = true;

    public SocketServer(SocketChannel clientChannel, MessageGenerator messageGenerator) {
        this.clientChannel = clientChannel;
        this.messageGenerator = messageGenerator;
    }

    public void handleClient() {
        try {
            PrintWriter writer = new PrintWriter(
                    Channels.newOutputStream(clientChannel),
                    true
            );

            while (running && clientChannel.isOpen()) {
                String message = messageGenerator.onMessage();

                if (message != null && !message.isBlank()) {
                    writer.println(message);

                    if (writer.checkError()) {
                        System.out.println("Client disconnected.");
                        break;
                    }

                    System.out.println("Sent to client: " + message);
                }

                Thread.sleep(10_000);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Client handler interrupted.");
        } catch (Exception e) {
            System.out.println("Client handler error: " + e.getMessage());
        } finally {
            close();
        }
    }

    @Override
    public void close() {
        running = false;

        try {
            if (clientChannel != null && clientChannel.isOpen()) {
                clientChannel.close();
                System.out.println("Client channel closed.");
            }
        } catch (IOException e) {
            System.out.println("Error while closing client channel: " + e.getMessage());
        }
    }
}