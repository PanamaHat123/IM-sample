package com.lal.im.service.utils;


import com.lal.im.common.constant.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class WriteUserSeq {

    //redis  hash
    // uid  friend 10
    //      group  12
    //      conversation
    @Autowired
    RedisTemplate redisTemplate;

    public void writeUserSeq(Integer appId,String userId, String type,Long seq){

        String key = appId + ":" + Constants.RedisConstants.SeqPrefix +":"+userId;
        redisTemplate.opsForHash().put(key,type,seq);

    }

}
