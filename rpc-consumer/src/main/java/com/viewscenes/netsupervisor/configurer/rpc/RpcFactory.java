package com.viewscenes.netsupervisor.configurer.rpc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.viewscenes.netsupervisor.annotation.RpcService;
import com.viewscenes.netsupervisor.entity.InfoUser;
import com.viewscenes.netsupervisor.entity.Request;
import com.viewscenes.netsupervisor.entity.Response;
import com.viewscenes.netsupervisor.netty.client.NettyClient;
import com.viewscenes.netsupervisor.util.IdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by MACHENIKE on 2018-12-03.
 */
@Component
public class RpcFactory<T> implements InvocationHandler {

    @Autowired
    NettyClient client;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Request request = new Request();
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameters(args);
        request.setParameterTypes(method.getParameterTypes());
        //每一次请求的唯一ID
        request.setId(IdUtil.getId());
        RpcService rpcService = method.getDeclaringClass().getDeclaredAnnotation (RpcService.class);

        //发送消息
        Object result = client.send(request,rpcService.name ());
        Class<?> returnType = method.getReturnType();

        Response response = JSON.parseObject(result.toString(), Response.class);
        if (response.getCode()==1){
            throw new Exception(response.getError_msg());
        }
        if (returnType.isPrimitive() || String.class.isAssignableFrom(returnType)){
            return response.getData();
        }else if (Collection.class.isAssignableFrom(returnType)){
            return JSONArray.parseArray(response.getData().toString(),Object.class);
        }else if(Map.class.isAssignableFrom(returnType)){
            return JSON.parseObject(response.getData().toString(),Map.class);
        }else{
            Object data = response.getData();
            return JSONObject.parseObject(data.toString(), returnType);
        }
    }
}
