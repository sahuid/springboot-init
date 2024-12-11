package com.sahuid.springbootinit.common;

import lombok.Builder.Default;
import lombok.Data;

@Data
public class PageRequest {

    private int page = 1;

    private int pageSize = 10;
}
