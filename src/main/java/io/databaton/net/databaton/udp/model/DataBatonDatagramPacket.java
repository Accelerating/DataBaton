package io.databaton.net.databaton.udp.model;

import io.databaton.utils.DateTime;
import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;
import lombok.Data;

import java.net.InetSocketAddress;

/**
 * @author zxx
 */
@Data
public class DataBatonDatagramPacket {

    private DataBatonUdpDispatchMessageProto.DataBatonUdpDispatchMessage message;

    private DateTime sendTime;

    private InetSocketAddress target;

}
