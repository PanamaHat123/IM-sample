
import {createSlice, PayloadAction} from "@reduxjs/toolkit";
import {Friend} from "../model/response/friend.ts";


interface InitialState{
    friendList:Friend[];
}
const initialState :InitialState = {
    friendList:[]
}

const friendSlice = createSlice({
    name:"friend",
    initialState,
    reducers:{
        initFriendList:(state:InitialState,actions:PayloadAction<Friend[]>)=>{
            state.friendList.push(...actions.payload)
        }
    }
})

export const { initFriendList } = friendSlice.actions

export default friendSlice.reducer