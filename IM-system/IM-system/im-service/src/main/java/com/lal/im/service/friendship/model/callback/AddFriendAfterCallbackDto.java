package com.lal.im.service.friendship.model.callback;

import com.lal.im.service.friendship.model.req.FriendDto;
import lombok.Data;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class AddFriendAfterCallbackDto {

    private String fromId;

    private FriendDto toItem;
}
