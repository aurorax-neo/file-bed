package com.customfile.app.common.utils;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.customfile.app.common.constant.ApiMapping.CUSTOM_FILE_DOWNLOAD_CONTROLLER_MAPPING;


/**
 * 控制器工具
 *
 * @author YCJ
 * @date 2023/01/08
 */
public class ControllerUtils {
    /**
     * 列表
     *
     * @param objService obj服务
     * @param objRequest obj请求
     * @param objClazz   obj clazz
     * @param objVOClazz obj voclazz
     * @return {@link List}<{@link ?}>
     */
    public static List<?> getList(Object objService, Object objRequest, Class<?> objClazz, Class<?> objVOClazz) {
        try {
            Object obj = objClazz.getDeclaredConstructor().newInstance();
            if (objRequest != null) {
                BeanUtils.copyProperties(objRequest, obj);
            }
            QueryWrapper<?> objectQueryWrapper = new QueryWrapper<>(obj);
            Method listMethod = objService.getClass().getDeclaredMethod("list", Wrapper.class);
            List<?> objList = (List<?>) listMethod.invoke(objService, objectQueryWrapper);
            // 是否返回视图
            if (objVOClazz != null) {
                List<Object> objVOList = new ArrayList<>();
                for (Object i : objList) {
                    Object objVO = objVOClazz.getDeclaredConstructor().newInstance();
                    BeanUtils.copyProperties(i, objVO);
                    objVOList.add(objVO);
                }
                return objVOList;
            }
            return objList;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                 InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 分页列表
     *
     * @param objService obj服务
     * @param objRequest obj请求
     * @param objClazz   obj clazz
     * @param objVOClazz obj voclazz
     * @return {@link Page}<{@link ?}>
     */
    public static Page<?> getPaginationList(Object objService, Object objRequest, Class<?> objClazz, Class<?> objVOClazz) {
        try {
            long current = 1;
            long size = 10;
            Object obj = objClazz.getDeclaredConstructor().newInstance();
            if (objRequest != null) {
                BeanUtils.copyProperties(objRequest, obj);
                Method getCurrent = objRequest.getClass().getMethod("getCurrent");
                current = (long) getCurrent.invoke(objRequest);
                Method getPageSize = objRequest.getClass().getMethod("getPageSize");
                size = (long) getPageSize.invoke(objRequest);
            }
            QueryWrapper<?> objectQueryWrapper = new QueryWrapper<>(obj);
            Method pageMethod = objService.getClass().getDeclaredMethod("page", IPage.class, Wrapper.class);
            Page<?> objPage = (Page<?>) pageMethod.invoke(objService, new Page<>(current, size), objectQueryWrapper);
            // 是否返回视图
            if (objVOClazz != null) {
                Page<Object> objVOPage = new Page<>(objPage.getCurrent(), objPage.getSize(), objPage.getTotal());
                List<Object> objVOList = new ArrayList<>();
                for (Object i : objPage.getRecords()) {
                    Object objVO = objVOClazz.getDeclaredConstructor().newInstance();
                    BeanUtils.copyProperties(i, objVO);
                    objVOList.add(objVO);
                }
                objVOPage.setRecords(objVOList);
                return objVOPage;
            }
            return objPage;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                 InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}
