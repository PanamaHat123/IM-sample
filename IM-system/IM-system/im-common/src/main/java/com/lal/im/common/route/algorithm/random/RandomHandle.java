package com.lal.im.common.route.algorithm.random;



import com.lal.im.common.enums.UserErrorCode;
import com.lal.im.common.exception.ApplicationException;
import com.lal.im.common.route.RouteHandle;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
public class RandomHandle implements RouteHandle {
    @Override
    public String routeServer(List<String> values, String key) {
        int size = values.size();
        if(size == 0){
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        int i = ThreadLocalRandom.current().nextInt(size);
        return values.get(i);
    }
}
