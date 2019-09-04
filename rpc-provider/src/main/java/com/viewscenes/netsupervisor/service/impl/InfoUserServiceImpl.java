package com.viewscenes.netsupervisor.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.viewscenes.netsupervisor.annotation.RpcService;
import com.viewscenes.netsupervisor.entity.InfoUser;
import com.viewscenes.netsupervisor.service.InfoUserService;
import com.viewscenes.netsupervisor.zk.ZkUtils;
import org.I0Itec.zkclient.ZkClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;


/**
 * @program: rpc-provider
 * @description: ${description}
 * @author: shiqizhen
 * @create: 2018-11-30 16:55
 **/
@RpcService
public class InfoUserServiceImpl implements InfoUserService {
    
    Logger logger = LoggerFactory.getLogger(this.getClass());
    
    
    //    Map<String,InfoUser> infoUserMap = new ConcurrentHashMap<>();
    @Autowired
    private ZkClient zkClient;
    
    
    public List<InfoUser> insertInfoUser(InfoUser infoUser) {
        logger.info("新增用户信息:{}", JSONObject.toJSONString(infoUser));
//        if(!zkClient.exists ("/USER-INFO"))
//            zkClient.createPersistent ("/USER-INFO");
        zkClient.createEphemeral("/USER-INFO/"+infoUser.getId (), infoUser);
        //        infoUserMap.put(infoUser.getId(),infoUser);
        return getInfoUserList();
    }
    
    public InfoUser getInfoUserById(String id) {
        String childPath = "/USER-INFO/"+id;
        InfoUser infoUser = zkClient.readData(childPath, true);
        logger.info("查询用户ID:{}",id);
        return infoUser;
    }
    
    public List<InfoUser> getInfoUserList() {
        
        List<InfoUser> userList = ZkUtils.getChildrenDataList (zkClient,"/USER-INFO");
      
        logger.info("返回用户信息记录:{}", JSON.toJSONString(userList));
        return userList;
    }
    
    public void deleteInfoUserById(String id) {
        boolean flg =zkClient.delete("/USER-INFO/"+id);
        String isSucess = flg?"成功":"失败";
        logger.info("删除用户[{}]信息:{}",id,isSucess);
    }
    
    public String getNameById(String id){
        logger.info("根据ID查询用户名称:{}",id);
        String childPath = "/USER-INFO/"+id;
        InfoUser infoUser = zkClient.readData(childPath, true);
        return infoUser.getName();
    }
    public Map<String,InfoUser> getAllUser(){
        Map<String, InfoUser> map =
            ZkUtils.getChildrenDataMap (zkClient,"/USER-INFO");

        logger.info("查询所有用户信息{}",JSONObject.toJSONString(map));
        return map;
    }
}
