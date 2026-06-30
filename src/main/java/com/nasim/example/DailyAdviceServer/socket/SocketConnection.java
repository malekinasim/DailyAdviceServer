package com.nasim.example.DailyAdviceServer.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.Optional;

public class SocketConnection {
    private static Optional<ServerSocketChannel> serverSocketChannel = Optional.empty();
    public  static ServerSocketChannel connect(String port) throws IOException {
        if(serverSocketChannel.isEmpty()){
                ServerSocketChannel serverSocketChannel= ServerSocketChannel.open();
                serverSocketChannel.bind(new InetSocketAddress(Integer.parseInt(port)));
                SocketConnection.serverSocketChannel = Optional.of(serverSocketChannel);
        }
       return serverSocketChannel.get();

    }

}
