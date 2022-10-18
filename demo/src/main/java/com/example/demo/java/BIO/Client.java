/**
 * Welcome to https://waylau.com
 */
package com.example.demo.java.BIO;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Blocking Client.
 * 
 * @since 1.0.0
 * @author sgz
 */
public class Client {
    private static final int DEFAULT_PORT = 7;

	public static void main(String[] args) throws UnknownHostException {
        InetAddress localHost = InetAddress.getLocalHost();
        // 获取本地主机信息
        InetAddress address = InetAddress.getByAddress(localHost.getAddress());
        String hostName = localHost.getHostName();
        System.out.println("本地地址是----" + address);
        System.out.println("本地名称是----" + hostName);

        try (
                // 建立连接
                Socket socket = new Socket(address, DEFAULT_PORT);

                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        ) {
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                System.out.println("发送: " + in.readLine());
            }
        } catch (UnknownHostException e) {
            System.err.println("不明主机，主机名为： " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("不能从主机中获取I/O，主机名为：" + hostName);
            System.exit(1);
        } 
	}

}
