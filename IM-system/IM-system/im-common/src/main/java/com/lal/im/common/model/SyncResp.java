package com.lal.im.common.model;

import lombok.Data;

import java.util.List;


@Data
public class SyncResp<T> {

    private Long maxSequence;

    private boolean isCompleted;

    private List<T> dataList;

}
