
import {configureStore} from "@reduxjs/toolkit";
import homeData from "./homeData";
const store = configureStore({
    reducer:{
        home:homeData
    },
})

export default store;

export type RootState = ReturnType<typeof store.getState>

export type AppDispatch = typeof store.dispatch