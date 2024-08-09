
import {createSlice} from "@reduxjs/toolkit";

const homeSlice = createSlice({
    name:"home",
    initialState:{
        count:2
    },
    reducers:{
        addCount: state=>{
            console.log("param",param)
            state.count += param.payload
        }
    }
})

export const { addCount } = homeSlice.actions

export default homeSlice.reducer