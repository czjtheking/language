package com.steincker.common.util;

/**
 * @ClassName PropertiesUtils
 * @Author ST000056
 * @Date 2024-04-02 15:52
 * @Version 1.0
 * @Description
 **/

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * Enumeration 的作用，枚举一个List , 将一个list变成枚举
 * 枚举：一个存有固定时的list，不能添加，也不能减少，只能使用这个list里面的值
 */
public class PropertiesUtils extends Properties {
    //List是有序的所以用List<Objcet> 来存储key
    private List<Object> keyList = new ArrayList<Object>();

    //keyList 的getter方法
    public List<Object> getKeyList() {
        return keyList;
    }

    /**
     * Class.getResourceAsStream(String path) 读取的是 targer/classes/ 下的配置文件
     * 读取配置文件
     * @param fileName
     */
    public void loadProperties(String path) {
        try {
//            this.load(new InputStreamReader(PropertiesUtils.class.getResourceAsStream(path)));
            this.load(new InputStreamReader(new FileInputStream(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 重写put方法，按照property的存入顺序保存key到keyList，遇到重复的后者将覆盖前者。
     */
    @Override
    public synchronized Object put(Object key, Object value) {
        this.removeKeyIfExists(key);
        keyList.add(key);
        return super.put(key, value);
    }

    /**
     * 重写remove方法，删除属性时清除keyList中对应的key。
     */
    @Override
    public synchronized Object remove(Object key) {
        this.removeKeyIfExists(key);
        return super.remove(key);
    }

    private void removeKeyIfExists(Object key) {
        keyList.remove(key);
    }


    /**
     * 重写keys方法，返回根据keyList适配的Enumeration，且保持HashTable keys()方法的原有语义，
     * 每次都调用返回一个新的Enumeration对象，且和之前的不产生冲突
     */
    @Override
    public synchronized Enumeration<Object> keys() {
        return new EnumerationAdapter<Object>(keyList);
    }

    /**
     * List到Enumeration的适配器
     */
    private class EnumerationAdapter<T> implements Enumeration<T> {
        private int index = 0;
        private final List<T> list;
        private final boolean isEmpty;

        public EnumerationAdapter(List<T> list) {
            this.list = list;
            this.isEmpty = list.isEmpty();
        }

        public boolean hasMoreElements() {
            //isEmpty的引入是为了更贴近HashTable原有的语义，在HashTable中添加元素前调用其keys()方法获得一个Enumeration的引用，
            //之后往HashTable中添加数据后，调用之前获取到的Enumeration的hasMoreElements()将返回false，但如果此时重新获取一个
            //Enumeration的引用，则新Enumeration的hasMoreElements()将返回true，而且之后对HashTable数据的增、删、改都是可以在
            //nextElement中获取到的。
            return !isEmpty && index < list.size();
        }

        public T nextElement() {
            if (this.hasMoreElements()) {
                return list.get(index++);
            }
            return null;
        }
    }
}
