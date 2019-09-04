package com.viewscenes.netsupervisor.registry;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 注册服务
 * Created by MACHENIKE on 2018-11-30.
 */
@Component
public class ServiceRegistry {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${registry.address}")
    private String registryAddress;
    
    @Value ("${spring.discovery.serviceName}")
    private String serviceName;
    
    @Autowired
    private ZkClient zkClient;

    private static final String ZK_REGISTRY_PATH = "/rpc";

    public void register(String data) {
        if (data != null) {
            if (zkClient != null) {
                AddRootNode();
                createNode(data);
            }
        }
    }
//    private ZkClient connectServer() {
//        ZkClient client = new ZkClient(registryAddress,20000,20000);
//        return client;
//    }

    //创建主节点
    private void AddRootNode(){
        boolean exists = zkClient.exists(ZK_REGISTRY_PATH + "/"+serviceName);
        if (!exists){
            zkClient.createPersistent(ZK_REGISTRY_PATH + "/"+serviceName);
            logger.info("创建zookeeper主节点 {}",ZK_REGISTRY_PATH + "/"+serviceName);
        }
    }
    //创建服务节点
    private void createNode(String data) {
        String path= ZK_REGISTRY_PATH + "/"+serviceName+"/"+data;
        if( !zkClient.exists(path)) {
            zkClient.createEphemeral (path);
            logger.info("创建zookeeper临时节点 [{}]", path);
        }
//       String path = client.create(ZK_REGISTRY_PATH + "/"+serviceName, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    
    }
}
