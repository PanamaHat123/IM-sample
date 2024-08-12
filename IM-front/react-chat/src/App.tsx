import './App.less'
import {BrowserRouter, Routes, Route, Link} from "react-router-dom";

import {UsergroupDeleteOutlined, WechatOutlined } from '@ant-design/icons';
import Chat from './view/chat';
import Friend from './view/friend';

import { queryFriend } from "./api/app";
import { useEffect } from 'react';

function App() {

    useEffect(()=>{
        // queryFriend({})
    },[])

    return (
        <>
            <BrowserRouter>
            <div className='App'>
                <div className='left-side'>
                    <Link to="/">
                        <WechatOutlined style={{fontSize:"30px",marginTop:"10px"}}/>
                    </Link>
                    <Link to="/friend">
                        <UsergroupDeleteOutlined style={{fontSize:"30px",marginTop:"10px"}}/>
                    </Link>
                </div>
                <div className='right-side'>
                    <Routes>
                        <Route path="/" element={<Chat />}/>
                        <Route path="/friend" element={<Friend/>}/>
                    </Routes>
                </div>
            </div>
            </BrowserRouter>
        </>
    )
}

export default App
