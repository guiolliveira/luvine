package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class CircularHierarchyDetectedException extends BusinessException {
    public CircularHierarchyDetectedException() {
        super(
                "Hierarquia circular detectada",
                ErrorCode.CIRCULAR_HIERARCHY_DETECTED
        );
    }
}
