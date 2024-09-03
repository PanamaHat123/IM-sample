import './App.less'
import {RouterProvider} from "react-router-dom";
import {queryFriend} from "./api/app.ts";
import {useDispatch, useSelector} from "react-redux";
import {AppDispatch, RootState} from "./store/store.ts";
import router from './router/index.tsx'

function App() {
    const friendSate = useSelector((state: RootState )=>state.friend);
    const dispatch = useDispatch<AppDispatch>();
    
    return (
        <>
            Test
            <RouterProvider router={router} />
        </>
    )
}

export default App
