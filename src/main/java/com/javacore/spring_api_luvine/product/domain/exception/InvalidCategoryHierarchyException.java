package com.javacore.spring_api_luvine.product.domain.exception;

import com.javacore.spring_api_luvine.common.exception.ErrorCode;
import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;

public class InvalidCategoryHierarchyException extends BusinessException {
    public InvalidCategoryHierarchyException() {
        super(
                "Uma categoria não pode ser atribuída como pai dela mesma",
                ErrorCode.INVALID_CATEGORY_HIERARCHY
        );
    }
}