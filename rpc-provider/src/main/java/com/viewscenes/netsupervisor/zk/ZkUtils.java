package com.viewscenes.netsupervisor.zk;

import com.viewscenes.netsupervisor.entity.InfoUser;
import org.I0Itec.zkclient.ZkClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: lds
 * @Date: 2019/9/4 11:59
 */
public class ZkUtils {

	public static <T> List<T> getChildrenDataList( ZkClient zkClient,String path){
		List<T> list= new ArrayList<> ();
		List<String> children = zkClient.getChildren(path);
		if(children.isEmpty()){
			return null;
		}
		for ( String chl:children ) {
			String childPath = "/USER-INFO/"+chl;
			T infoUser = zkClient.readData(childPath, true);
			list.add (infoUser);
		}
		return list;
	}
	public static <T> Map<String,T> getChildrenDataMap( ZkClient zkClient,String path){
		Map<String, T> map = new HashMap<> ();
		List<String> children = zkClient.getChildren(path);
		if(children.isEmpty()){
			return null;
		}
		for ( String chl:children ) {
			String childPath = "/USER-INFO/"+chl;
			T infoUser = zkClient.readData(childPath, true);
			map.put (chl,infoUser);
		}
		return map;
	}
}
