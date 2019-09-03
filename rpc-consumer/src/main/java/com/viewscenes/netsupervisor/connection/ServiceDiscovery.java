package com.viewscenes.netsupervisor.connection;

import com.alibaba.fastjson.JSONObject;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class ServiceDiscovery {

    @Value("${registry.address}")
    private String registryAddress;

    @Autowired
    ConnectManage connectManage;

    // 服务地址列表
    private volatile HashMap<String,List<String>> addressMap = new HashMap <> ();
//    private volatile List<String> addressList = new ArrayList<>();
    private static final String ZK_REGISTRY_PATH = "/rpc";
    private ZkClient client;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    //初始化
    @PostConstruct
    public void init(){
        client = connectServer();
        if (client != null) {
            watchNode(client);
        }
    }
    //连接zookeeper服务
    private ZkClient connectServer() {
        ZkClient client = new ZkClient(registryAddress,20000,20000);
        return client;
    }
    //监听子节点的变化
    private void watchNode(final ZkClient client) {
        List<String> cList = client.getChildren (ZK_REGISTRY_PATH);
//        client.exists ()
        for (String chl: cList) {
            List<String> nodeList = client.subscribeChildChanges(ZK_REGISTRY_PATH+"/"+chl, (s, nodes) -> {
                logger.info("监听到"+chl+"节点变化,剩余子节点{}",JSONObject.toJSONString(nodes));
                //            addressList.clear();
                getNodeData(chl,nodes);
                updateConnectedServer();
            });
            getNodeData(chl,nodeList);
            logger.info("已发现服务列表...{}", addressMap);
            updateConnectedServer();
        }
      
        
       
    }
    private void updateConnectedServer(){
        //一个服务器创建一个链接
        connectManage.updateConnectServer(addressMap);
    }
    
    private void getNodeData(String serviceName,List<String> nodes){
        logger.info("/rpc子节点数据为:{}", JSONObject.toJSONString(nodes));
//        if(nodes.size () == 0){
//            addressMap.remove (serviceName);
//        }
        addressMap.put (serviceName,nodes);
//        for(String node:nodes){
//            String serviceName = ZK_REGISTRY_PATH+"/"+node;
//            List<String> children =client.getChildren (serviceName);
////            String address = client.readData(ZK_REGISTRY_PATH+"/"+node);
//            addressMap.put (serviceName,children);
//        }
    }
}
