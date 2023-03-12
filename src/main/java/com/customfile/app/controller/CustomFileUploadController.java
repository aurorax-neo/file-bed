package com.customfile.app.controller;

import com.customfile.app.common.exception.BusinessException;
import com.customfile.app.common.response.BaseResponse;
import com.customfile.app.common.response.ResultUtils;
import com.customfile.app.common.response.StateCode;
import com.customfile.app.common.utils.FileUtil;
import com.customfile.app.model.dto.CustomFileUploadCheckFileRequest;
import com.customfile.app.model.dto.CustomFileUploadSegmentFileRequest;
import com.customfile.app.model.entity.CustomFile;
import com.customfile.app.model.vo.CustomFileVO;
import com.customfile.app.service.CustomFileService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

import static com.customfile.app.common.constant.ApiMapping.CUSTOM_FILE_CONTROLLER_MAPPING;


/**
 * 图控制器
 *
 * @author YCJ
 * @date 2023/02/20
 */
@RestController
@RequestMapping(CUSTOM_FILE_CONTROLLER_MAPPING)
public class CustomFileUploadController {


    @Resource
    private CustomFileService customFileService;

    private static BaseResponse<CustomFileVO> getCustomFileVOBaseResponse(HttpServletRequest httpServletRequest, CustomFile customFile) {
        CustomFileVO customFileVO = new CustomFileVO();
        BeanUtils.copyProperties(customFile, customFileVO);
        if (Objects.equals(customFile.getSegmentIndex(), customFile.getSegmentTotal())) {
            String fileUrl = FileUtil.getFileUrl(httpServletRequest, customFile.getFileKey());
            customFileVO.setFileUrl(fileUrl);
        }
        return ResultUtils.success(customFileVO);
    }

    @RequestMapping("/segmentFile")
    public BaseResponse<CustomFileVO> uploadFile(HttpServletRequest httpServletRequest,
                                                 CustomFileUploadSegmentFileRequest request) {
        CustomFile customFile = customFileService.upLoadCustomFile(
                httpServletRequest,
                request.getFileName(),
                request.getSegmentFile(),
                request.getSegmentIndex(),
                request.getSegmentSize(),
                request.getSegmentTotal(),
                request.getKey());
        return getCustomFileVOBaseResponse(httpServletRequest, customFile);
    }

    @PostMapping("/checkFile")
    // 检查文件是否已经存在，且返回CustomFile信息
    public BaseResponse<CustomFileVO> checkFile(HttpServletRequest httpServletRequest,
                                                @RequestBody CustomFileUploadCheckFileRequest request) {
        CustomFile customFile = customFileService.getCustomFileByKey(request.getKey());
        if (customFile == null) {
            throw new BusinessException(StateCode.NOT_FOUND_ERROR, "该文件未上传");
        }
        return getCustomFileVOBaseResponse(httpServletRequest, customFile);
    }
}
