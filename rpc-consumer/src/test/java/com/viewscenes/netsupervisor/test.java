package com.viewscenes.netsupervisor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: lds
 * @Date: 2019/9/3 19:11
 */
public class test {
	
	public static void main ( String[] args ) {
		Map<String,String> map = new HashMap<> ();
		map.put ("1","dew");
		map.put ("2","refrw");
		map.put ("3","fefrw");
		
		List<String> list = map.entrySet ().stream ().
			filter (e-> (e.getValue ().equals ("refrw"))).
			map (x->x.getValue ()).collect(Collectors.toList());
		
		System.out.print (map);
		System.out.println (list);
	}
}
