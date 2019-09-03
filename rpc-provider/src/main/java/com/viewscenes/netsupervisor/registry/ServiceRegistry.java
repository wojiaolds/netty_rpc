package com.viewscenes.netsupervisor.registry;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by MACHENIKE on 2018-11-30.
 */
@Component
public class ServiceRegistry {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${registry.address}")
    private String registryAddress;
    
    @Value ("${spring.discovery.serviceName}")
    private String serviceName;

    private static final String ZK_REGISTRY_PATH = "/rpc";

    public void register(String data) {
        if (data != null) {
            ZkClient client = connectServer();
            if (client != null) {
                AddRootNode(client);
                createNode(client, data);
            }
        }
    }
    private ZkClient connectServer() {
        ZkClient client = new ZkClient(registryAddress,20000,20000);
        return client;
    }

    private void AddRootNode(ZkClient client){
        boolean exists = client.exists(ZK_REGISTRY_PATH + "/"+serviceName);
        if (!exists){
            client.createPersistent(ZK_REGISTRY_PATH + "/"+serviceName);
            logger.info("创建zookeeper主节点 {}",ZK_REGISTRY_PATH + "/"+serviceName);
        }
    }

    private void createNode(ZkClient client, String data) {
        String path= ZK_REGISTRY_PATH + "/"+serviceName+"/"+data;
        client.createEphemeral (path);
//       String path = client.create(ZK_REGISTRY_PATH + "/"+serviceName, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        logger.info("创建zookeeper临时节点 [{}]", path);
    }
}
