package com.haothink.common.validate;


import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.*;


/**
 * @author wanghao
 * @date 2018年08月13日 11:39
 * description: 验证不符合条件的字段
 */
public class HibernateValidator {

    private static Validator validator;

    static {

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * 取第一个不符合条件的信息
     * @param obj
     *        需要校验的bean
     * @return
     *        返回第一个不符合条件的字段信息
     */
    public static String volidate(Object obj){

        Map<String,String> allBadCheck = volidateALL(obj);
        if(!allBadCheck.isEmpty()) {
            Optional<String> optional = volidateALL(obj).keySet().stream().findFirst();
            if(optional.isPresent()){
                String key = optional.get();
                return key + ":" + allBadCheck.get(key);
            }
        }

        return null;
    }

    public static List<String> mismatchField(Object obj){

        return new ArrayList<>(volidateALL(obj).keySet());
    }

    /**
     * 获取所有不符合验证条件的字段及其条件信息
     * @param obj
     *        需要校验的bean
     * @return
     *        所有不符合验证条件的字段及其条件信息
     */
    public static Map<String,String> volidateALL(Object obj){

        Map<String,String> allBadCheck = new LinkedHashMap<>();
        Set<ConstraintViolation<Object>> constraintViolations =
                validator.validate(obj);
        if(!constraintViolations.isEmpty()){
            constraintViolations.forEach(objectConstraintViolation -> {
                allBadCheck.put(objectConstraintViolation.getPropertyPath().toString(),objectConstraintViolation.getMessage());
            });
        }
        return allBadCheck;
    }


}
