/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.brainlounge.zooterrain.netty;

import com.brainlounge.zooterrain.zkclient.ZkStateObserver;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * A HTTP server which serves Web Socket requests at:
 *
 * http://localhost:8080/websocket
 *
 * Open your browser at http://localhost:8080/, then the demo page will be loaded and a Web Socket connection will be
 * made automatically.
 *
 * This server illustrates support for the different web socket specification versions and will work with:
 *
 * <ul>
 * <li>Safari 5+ (draft-ietf-hybi-thewebsocketprotocol-00)
 * <li>Chrome 6-13 (draft-ietf-hybi-thewebsocketprotocol-00)
 * <li>Chrome 14+ (draft-ietf-hybi-thewebsocketprotocol-10)
 * <li>Chrome 16+ (RFC 6455 aka draft-ietf-hybi-thewebsocketprotocol-17)
 * <li>Firefox 7+ (draft-ietf-hybi-thewebsocketprotocol-10)
 * <li>Firefox 11+ (RFC 6455 aka draft-ietf-hybi-thewebsocketprotocol-17)
 * </ul>
 */
public class WebSocketServer {

    protected final int port;
    protected final String zkConnection;

    public WebSocketServer(int port, String zkConnection) {
        this.port = port;
        this.zkConnection = zkConnection;
    }

    public void run() throws Exception {

        final ZkStateObserver zkStateObserver = new ZkStateObserver(zkConnection);

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new WebSocketServerInitializer(zkStateObserver));

            Channel ch = b.bind(port).sync().channel();
            System.out.println("Zooterrain server started at port: http://localhost:" + port + '.');

            zkStateObserver.start();
            
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
        
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 38080;
        }

        //final String zookeeperConnection = System.getProperty("zooterrain.conn");
        //final String zookeeperConnection = "192.168.2.48:2181";
        final String zookeeperConnection = "192.168.100.80:2181";
        if (zookeeperConnection == null) {
            System.err.println("please provide a zookeeper connection string in property 'zooterrain.conn'.");
            System.exit(-1);
        }
        System.out.println("you specified a ZooKeeper at " + zookeeperConnection);

        new WebSocketServer(port, zookeeperConnection).run();
    }
}
