package io.databaton.net.databaton;

import io.netty.channel.ChannelFuture;

/**
 * abstraction of the DataBaton Client
 * @author zxx
 */
public interface DataBatonClient {

    /**
     * Establishing a connection with the remote server
     * @return future
     */
    ChannelFuture connectToRemoteServer();

}
