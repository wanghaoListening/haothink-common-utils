package com.haothink.common.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wanghao
 * @date 2018年08月27日 17:24
 * description:
 */
public class CopyUtil {


    public static <S, D> void copyToList(List<S> sourceList, List<D> targetList, Class<D> targetClass) {
        for (S source: sourceList) {
            D target = createInstance(targetClass);
            BeanUtils.copyProperties(source , target);
            targetList.add(target);
        }
    }

    public static <S, D> List<D> copyToNewList(List<S> sourceList, Class<D> targetClass) {
        List<D> targetList = new ArrayList<>(sourceList.size());
        for (S source: sourceList) {
            D target = createInstance(targetClass);
            BeanUtils.copyProperties(source , target);
            targetList.add(target);
        }
        return targetList;
    }

    public static <S, D> void copyToArrayList(List<S> sourceList, ArrayList<D> targetList, Class<D> targetClass) {
        for (S source: sourceList) {
            targetList.add(copyToNewObject(source, targetClass));
        }
    }

    public static <S, D> ArrayList<D> copyToNewArrayList(List<S> sourceList, Class<D> targetClass) {
        if (null == sourceList) {
            return null;
        }
        ArrayList<D> targetList = new ArrayList<>(sourceList.size());
        for (S source: sourceList) {
            targetList.add(copyToNewObject(source, targetClass));
        }
        return targetList;
    }

    private static <T> T createInstance(Class<T> cls) {
        T obj = null;
        try {
            obj = cls.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return obj;
    }

    /**
     * 拷贝单个对象
     * @param source
     *        source
     * @param targetClass
     *        target对象
     * @param <T>
     *        target type
     * @return
     *        拷贝对象
     */
    public static <T> T copyToNewObject(Object source, Class<T> targetClass){
        if (null == source) {
            return null;
        }
        T target = createInstance(targetClass);
        BeanUtils.copyProperties(source , target);
        return target;
    }

}
