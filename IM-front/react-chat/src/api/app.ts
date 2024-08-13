
import { Friend } from '../model/response/friend'
import request from '../utils/request'



export const queryFriend = async (data:object) :Promise<Friend[]> =>{

        try {
            const res = await request.post<Friend[]>("/v1/friendship/getAllFriendShip?appId=10000",data)
            return res.data
        } catch (error) {
            console.log("err",error);
            return []
        }
}