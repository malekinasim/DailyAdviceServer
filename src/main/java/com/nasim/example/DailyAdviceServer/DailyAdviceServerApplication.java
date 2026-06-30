package com.nasim.example.DailyAdviceServer;

import com.nasim.example.DailyAdviceServer.socket.messageGenerator.AiMessageGenerator;
import com.nasim.example.DailyAdviceServer.socket.SocketConnection;
import com.nasim.example.DailyAdviceServer.socket.SocketServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class DailyAdviceServerApplication implements CommandLineRunner {

	@Value("${dailyServer.port}")
	private  String serverPort;
	private final ExecutorService threadPool = Executors.newCachedThreadPool();
	private final List<SocketServer> clients = new CopyOnWriteArrayList<>();

	private ServerSocketChannel serverSocketChannel;
	public static void main(String[] args) {
		SpringApplication.run(DailyAdviceServerApplication.class, args);
	}

	@Override
	public void run(String... args) {
		Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownServer));

		try {
			serverSocketChannel = SocketConnection.connect(serverPort);

			System.out.println("Server is listening on port " + serverPort);

			while (serverSocketChannel.isOpen()) {
				SocketChannel channel = serverSocketChannel.accept();

				SocketServer clientHandler =
						new SocketServer(channel, new AiMessageGenerator());

				clients.add(clientHandler);

				threadPool.submit(() -> {
					try {
						clientHandler.handleClient();
					} finally {
						clients.remove(clientHandler);
					}
				});
			}

		} catch (IOException e) {
			System.out.println("Could not init server: " + e.getMessage());
		}
	}

	private void shutdownServer() {
		System.out.println("Shutting down server...");

		for (SocketServer client : clients) {
			client.close();
		}

		threadPool.shutdownNow();

		try {
			if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
				serverSocketChannel.close();
			}
		} catch (IOException e) {
			System.out.println("Error closing server socket: " + e.getMessage());
		}
	}
}
