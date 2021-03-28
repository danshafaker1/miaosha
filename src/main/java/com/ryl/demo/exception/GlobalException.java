package com.ryl.demo.exception;

import com.ryl.demo.result.CodeMsg;

public class GlobalException extends RuntimeException{

    private static final long serialVersionUID=1L;

    private CodeMsg cm;
    public GlobalException(CodeMsg codeMsg){
        super(codeMsg.toString());
        this.cm=codeMsg;
    }

    public CodeMsg getCm() {
        return cm;
    }

}
