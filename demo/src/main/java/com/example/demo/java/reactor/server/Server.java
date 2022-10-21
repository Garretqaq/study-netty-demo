package com.example.demo.java.reactor.server;

import java.io.IOException;

public class Server {
    public static void main(String[] args) throws IOException {
        new Reactor(233).build();
    }
}
