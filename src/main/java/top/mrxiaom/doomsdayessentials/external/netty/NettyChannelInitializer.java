package top.mrxiaom.doomsdayessentials.external.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import net.minecraft.server.v1_15_R1.NetworkManager;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.external.haproxy.HAProxyMessage;
import top.mrxiaom.doomsdayessentials.external.haproxy.HAProxyMessageDecoder;
import top.mrxiaom.doomsdayessentials.utils.NMSUtil;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class NettyChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final ChannelInitializer<SocketChannel> oldChildHandler;
    private final Method oldChildHandlerMethod;

    public NettyChannelInitializer(ChannelInitializer<SocketChannel> oldChildHandler) throws Exception {
        this.oldChildHandler = oldChildHandler;
        this.oldChildHandlerMethod = this.oldChildHandler.getClass().getDeclaredMethod("initChannel", Channel.class);
        this.oldChildHandlerMethod.setAccessible(true);
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        this.oldChildHandlerMethod.invoke(this.oldChildHandler, channel);

        channel.pipeline().addAfter("timeout", "haproxy-decoder", new HAProxyMessageDecoder());
        channel.pipeline().addAfter("haproxy-decoder", "haproxy-handler", new ChannelInboundHandlerAdapter(){
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                if (msg instanceof HAProxyMessage) {
                    HAProxyMessage message = (HAProxyMessage) msg;
                    String realaddress = message.sourceAddress();
                    int realport = message.sourcePort();

                    SocketAddress socketaddr = new InetSocketAddress(realaddress, realport);

                    // TODO 适配NMS
                    if(NMSUtil.getNMSVersion().startsWith("v1_15")) {
                        NetworkManager networkmanager = (NetworkManager) channel.pipeline().get("packet_handler");
                        networkmanager.socketAddress = socketaddr;
                    }
                    else{
                        Main.getInstance().getLogger().warning("你的服务器版本不是1.15，无法设置frp下玩家真实地址为 " +
                                "" + realaddress + ":" + realport+" ，请尽快更新插件");
                    }
                } else {
                    super.channelRead(ctx, msg);
                }
            }
        });
    }
}