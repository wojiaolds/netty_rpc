package com.viewscenes.netsupervisor.config;

import com.viewscenes.netsupervisor.zk.CusZkSerializer;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: lds
 * @Date: 2019/9/4 13:17
 */
@Configuration
public class AppConfig {
	
	@Bean
	ZkClient zkClient( @Value ("${registry.address}") String registryAddress,
					   @Value ("${zk.root.node}") String root){
		ZkClient client = new ZkClient(registryAddress,20000,20000);
		client.setZkSerializer (new CusZkSerializer ());
		//创建缓存的根节点
		String[] nodes = root.split (",");
		for ( String node:nodes ) {
			if(!client.exists ("/"+node)){
				client.createPersistent ("/"+node);
			}
		}
		return client;
	}
}
