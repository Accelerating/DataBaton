package io.databaton.net.databaton;

import io.netty.channel.ChannelFuture;

public interface DataBatonClient {


    ChannelFuture connectToRemoteServer();

}
