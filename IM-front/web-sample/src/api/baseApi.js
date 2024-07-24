
import request from "../utils/request";


export function queryAllFriendShip(appId,data){
  return request({
    url: '/v1/friendship/getAllFriendShip?appId='+appId,
    method: 'post',
    data: data
  })
}

export function login(appId,data){
  return request({
    url: '/v1/user/login?appId='+appId,
    method: 'post',
    data: data
  })
}
