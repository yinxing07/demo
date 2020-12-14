package com.yx.demo.requestDTO;

import java.io.Serializable;

/**
 * @author yinxing
 * @date 2020/11/2 14:58
 * @desc
 */

public class RequestPage implements Serializable {
    private Integer currentPage = 1;
    private Integer pageSize = 10;
}