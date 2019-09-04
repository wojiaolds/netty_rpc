package com.viewscenes.netsupervisor.connection;

import com.viewscenes.netsupervisor.netty.client.NettyClient;
import com.viewscenes.netsupervisor.netty.client.ThreadPool;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class ConnectManage{

    @Autowired
    NettyClient nettyClient;

    Logger logger = LoggerFactory.getLogger(ConnectManage.class);
    private AtomicInteger roundRobin = new AtomicInteger(0);
    //服务名与所有channel的映射集合
//    private ConcurrentHashMap<String,CopyOnWriteArrayList<Channel>> chlHashMap
//            = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Channel,String> chlHashMap
        = new ConcurrentHashMap<>();
//    private CopyOnWriteArrayList<Channel> channels = new CopyOnWriteArrayList<>();
    //所有SocketAddress和channel的映射集合
    private ConcurrentHashMap<Channel,String> channelNodes = new ConcurrentHashMap<>();
    
    public void print(){
        logger.info ("chlHashMap: {}"+chlHashMap);
        logger.info ("channelNodes: {}"+channelNodes);
    }
    //这里有待完善，根据服务名获取相应的地址。node对应服务名
    public  Channel loadBanlance(String serviceName) {
        List<Channel> channels =    chlHashMap.entrySet ().stream ()
            .filter (e->(e.getValue ().equals (serviceName)))
            .map(e->e.getKey ())
            .collect(Collectors.toList ());
        if (channels.size()>0) {
            int size = channels.size();
            int index = (roundRobin.getAndAdd(1) + size) % size;
            return channels.get(index);
        }else{
            return null;
        }
    }

    public synchronized void updateConnectServer( HashMap<String,List<String>> map ){
    
        for ( Map.Entry<String,List<String>> entry:map.entrySet () ) {
            List<String> addressList = entry.getValue ();
            
            List<Channel> listChl =    chlHashMap.entrySet ().stream ()
                .filter (e->(e.getValue ().equals (entry.getKey ())))
                .map(e->e.getKey ())
                .collect(Collectors.toList ());
            if(listChl == null) {
                listChl = new ArrayList<>();
            }
            
            if (addressList == null || addressList.isEmpty () ){
                logger.error("没有可用的服务器节点, 全部服务节点已关闭!");
                
                for (final Channel channel : listChl) {
//                    SocketAddress remotePeer = channel.remoteAddress();
//                    Channel handler_node = channelNodes.get(remotePeer);
                    channel.close();
                    chlHashMap.remove (channel);
                    channelNodes.remove (channel);
                }
                return;
            }
            
            HashSet<String> newAllServerNodeSet = new HashSet<>(addressList);
//            for ( int i = 0 ; i < addressList.size () ; ++i ) {
//                String[] array = addressList.get (i).split (":");
//                if (array.length == 2) {
//                    String host = array[0];
//                    int port = Integer.parseInt (array[1]);
//                    final SocketAddress remotePeer = new InetSocketAddress (host, port);
//                    newAllServerNodeSet.add (remotePeer);
//                }

//            }
    
            for (final String serverNodeAddress : newAllServerNodeSet) {
                Channel channel = null;
                List<Channel> list = channelNodes.entrySet ().stream ()
                    .filter (e->e.getValue ().equals(serverNodeAddress))
                    .map(e->e.getKey ())
                    .collect(Collectors.toList ());
                logger.info ("List<Channel>: {}"+list);
                if(list != null && !list.isEmpty ())
                    channel = list.get (0) ;
                
                
                if (channel!=null && channel.isOpen()){
                    logger.info("当前服务节点已存在,无需重新连接.{}",serverNodeAddress);
                }else{
                    connectServerNode(entry.getKey (),serverNodeAddress);
                }
            }
            
            for (int i = 0; i < listChl.size(); ++i) {
                Channel channel = listChl.get(i);
                InetSocketAddress remotePeer = (InetSocketAddress)channel.remoteAddress();
                System.out.println ("remotePeer.getHostString () : "+remotePeer.getHostString ());
                if (!newAllServerNodeSet.contains(remotePeer.getHostString ()+":"+remotePeer.getPort ())) {
                    logger.info("删除失效服务节点 " + remotePeer);
//
                    channel.close();
    
                    chlHashMap.remove(channel);
                    channelNodes.remove(channel);
                }
            }
        }
       
    }

    private void connectServerNode(String serviceName,String address){
        try {
            String[] array = address.split (":");
            if (array.length == 2) {
                String host = array[0];
                int port = Integer.parseInt (array[1]);
                SocketAddress remotePeer = new InetSocketAddress (host, port);
                Channel channel = nettyClient.doConnect(remotePeer);
                addChannel(serviceName,channel,address);
            }
          
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.info("未能成功连接到服务器:{}",address);
        }
    }
    private void addChannel(String serviceName,Channel channel,String address) {
        logger.info("加入Channel到连接管理器.{}",address);
        chlHashMap.put (channel,serviceName);
        channelNodes.put(channel, address);
    }

    public void removeChannel(Channel channel){
        logger.info("从连接管理器中移除失效Channel.{}",channel.remoteAddress());
//        SocketAddress remotePeer = channel.remoteAddress();
        channelNodes.remove(channel);
        chlHashMap.remove(channel);
    }
}
