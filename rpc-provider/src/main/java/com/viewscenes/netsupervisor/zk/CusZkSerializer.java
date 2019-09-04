package com.viewscenes.netsupervisor.zk;

import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * @Author: lds
 * @Date: 2019/9/4 11:51
 */
@Component
public class CusZkSerializer implements ZkSerializer {
	
	/**
	 * 1序列化
	 */
	@Override
	public byte[] serialize(Object data) throws ZkMarshallingError {
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;
		try {
			// 序列化
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(data);
			byte[] bytes = baos.toByteArray();
			return bytes;
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(baos!=null) baos.close();
				if(oos!=null) oos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * 1反序列化
	 */
	@Override
	public Object deserialize(byte[] bytes) throws ZkMarshallingError {
		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;
		try {
			// 反序列化
			bais = new ByteArrayInputStream(bytes);
			ois = new ObjectInputStream(bais);
			return ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bais.close();
				ois.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
