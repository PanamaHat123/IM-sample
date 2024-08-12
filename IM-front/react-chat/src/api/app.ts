
import { Friend } from '../model/response/friend'
import request from '../utils/request'



export const queryFriend = async (data:object) =>{
    data = {
        appId:10000,
        fromId:"app01"
    }
    try {
        let res = await request.post<Friend[]>("/v1/friendship/getAllFriendShip?appId=10000",{})
        console.log(res);
    } catch (error) {
        console.log("err",error);
    }
}