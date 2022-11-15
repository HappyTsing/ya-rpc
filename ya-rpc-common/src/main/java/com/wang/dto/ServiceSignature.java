package com.wang.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * RPC 服务的签名
 * 由于 ServiceSignature 将在 Map 中用作 key，因此需要重写 equals 和 hashcode
 * Set 去重操作时，会先判断两个 hashCode 是否相同，再使用 equals 判断是否相同。
 * 因此，在重写 equals 时，必须重写 hashCode。且 equals 是必须重写的，因为其原本的实现非常粗糙。
 * @author happytsing
 */
@Getter
@AllArgsConstructor
public class ServiceSignature {

    private final String interfaceName;
    private final String group;
    private final String version;

    @Override
    public String toString() {
        return interfaceName+"?"+"group="+group+"&version="+version;
    }


    @Override
    public boolean equals(Object another){
        // 测试两个对象是否是同一个对象，是的话返回true
        if(this == another) {
            return true;
        }

        // 判断 another 实例是否是 ServiceSignature类 的实例
        if(another instanceof ServiceSignature){
            // 向下转型,父类无法调用子类的成员和方法
            ServiceSignature anotherServiceSignature = (ServiceSignature) another;
            // 判断所有属性是否相同
            return this.getInterfaceName().equals(anotherServiceSignature.getInterfaceName())
                    && this.getGroup().equals(anotherServiceSignature.getGroup())
                    && this.getVersion().equals(anotherServiceSignature.getVersion());
        }
        return false;
    }

    /**
     * 根据 JDK 给出的构造 hashCode 的方法进行书写
     * @Reference https://www.cnblogs.com/myseries/p/10977868.html
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (interfaceName == null ? 0 : interfaceName.hashCode());
        result = 31 * result + (group == null ? 0 : group.hashCode());
        result = 31 * result + (version == null ? 0 : version.hashCode());
        return result;
    }
}
