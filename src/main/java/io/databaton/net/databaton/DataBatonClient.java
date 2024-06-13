package io.databaton.net.databaton;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInboundHandler;

import java.io.Closeable;

/**
 * abstraction of the DataBaton Client
 * @author zxx
 */
public interface DataBatonClient extends ChannelInboundHandler {

    /**
     * Establishing a connection with the remote server
     * @return future
     */
    void connectToRemoteServer();

    /**
     * check the connection is active
     * @return
     */
    boolean isActive();

    /**
     * send data to remote server
     * @param data
     * @return
     */
    void sendData(Object data);

    /**
     * send data to remote server and add a listener for this operation
     * @param data
     * @param listener
     */
    void sendData(Object data, ChannelFutureListener listener);


}
