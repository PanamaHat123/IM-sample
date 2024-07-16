package com.lal.im.service.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lal.im.service.message.model.entity.ImMessageHistoryEntity;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ImMessageHistoryMapper extends BaseMapper<ImMessageHistoryEntity> {

    /**
     * 批量插入（mysql）
     * @param entityList
     * @return
     */
    Integer insertBatchSomeColumn(Collection<ImMessageHistoryEntity> entityList);


}
