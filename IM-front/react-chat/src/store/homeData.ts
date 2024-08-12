
import {createSlice, PayloadAction} from "@reduxjs/toolkit";


interface InitialState{
    count:number;
    title:string;
}
const initialState :InitialState = {
    count:2,
    title:"test"
}

const homeSlice = createSlice({
    name:"home",
    initialState,
    reducers:{
        addCount: (state:InitialState,actions:PayloadAction<number>)=>{
            console.log("param")
            state.count += actions.payload
        }
    }
})

export const { addCount } = homeSlice.actions

export default homeSlice.reducer