package com.yx.demo.beans;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import javafx.scene.chart.ScatterChart;


import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author yinxing
 * @date 2020/12/7 15:00
 * @desc channel 缓存管理类
 */

public class ChannelManagerBean {

    private static ConcurrentSkipListMap<String, ChannelHandlerContext> channelListMap = new ConcurrentSkipListMap<>();

    private static ConcurrentSkipListSet<String> channelListSet = new ConcurrentSkipListSet<>();

    public static void addChannel(String channelId, ChannelHandlerContext ctx){
        channelListSet.add(channelId);
        channelListMap.put(channelId,ctx);
    }

    public static ChannelHandlerContext getActiveChannel(String channelId){
        return channelListMap.get(channelId);
    }

    public static ConcurrentSkipListMap<String, ChannelHandlerContext> getActiveChannelList(){
        return channelListMap;
    }

    public static ConcurrentSkipListSet<String> getChannelListIds(){
        return channelListSet;
    }

    public static void removeChannel(String channelId){
        channelListMap.remove(channelId);
        channelListSet.remove(channelId);
    }

}
