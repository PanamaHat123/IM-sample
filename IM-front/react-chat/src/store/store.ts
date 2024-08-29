
import {configureStore} from "@reduxjs/toolkit";
import homeData from "./homeData";
import friend from "./Friend";

const store = configureStore({
    reducer:{
        home:homeData,
        friend:friend
    },
})

export default store;

export type RootState = ReturnType<typeof store.getState>

export type AppDispatch = typeof store.dispatch