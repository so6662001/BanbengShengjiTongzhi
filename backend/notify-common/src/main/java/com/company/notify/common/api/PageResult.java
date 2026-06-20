package com.company.notify.common.api;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 统一分页返回结构。
 */
@Data
public class PageResult<T> implements Serializable {

    private List<T> records;
    private long total;
    private long current;
    private long size;

    public static <T> PageResult<T> of(List<T> records, long total, long current, long size) {
        PageResult<T> p = new PageResult<>();
        p.records = records == null ? Collections.emptyList() : records;
        p.total = total;
        p.current = current;
        p.size = size;
        return p;
    }

    public static <T> PageResult<T> empty(long current, long size) {
        return of(Collections.emptyList(), 0, current, size);
    }
}
