import './App.less'
import {BrowserRouter, Routes, Route, Link} from "react-router-dom";

import {UsergroupDeleteOutlined, WechatOutlined} from '@ant-design/icons';
import Chat from './view/chat';
import Friend from './view/friend';


import {useEffect} from 'react';
import {queryFriend} from "./api/app.ts";
import {useDispatch, useSelector} from "react-redux";
import {AppDispatch, RootState} from "./store/store.ts";
import {initFriendList} from "./store/Friend.ts";

function App() {
    const friendSate = useSelector((state: RootState )=>state.friend);
    const dispatch = useDispatch<AppDispatch>();
    useEffect( () => {
         (async ()=>{
            const friends =  await queryFriend({
                appId: 10000,
                fromId: "app01"
            })
             dispatch(initFriendList(friends))
        })()
    }, [])

    return (
        <>
            <BrowserRouter>
                {/* <div>{friendSate.friendList.map(item=>item.toId)}</div> */}
                <div className='App'>
                    <div className='left-side'>
                        <Link to="/">
                            <WechatOutlined className="route-icon"/>
                        </Link>
                        <Link to="/friend">
                            <UsergroupDeleteOutlined className="route-icon"/>
                        </Link>
                    </div>
                    <div className='right-side'>
                        <Routes>
                            <Route path="/" element={<Chat/>}/>
                            <Route path="/friend" element={<Friend/>}/>
                        </Routes>
                    </div>
                </div>
            </BrowserRouter>
        </>
    )
}

export default App
