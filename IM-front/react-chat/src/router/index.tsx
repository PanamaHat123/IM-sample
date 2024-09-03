import { UsergroupDeleteOutlined, WechatOutlined } from "@ant-design/icons"
import { createBrowserRouter, Link, Outlet } from "react-router-dom"
import Friend from "../view/friend"
import Chat from "../view/chat"


const router =  createBrowserRouter([

    {
        path: "/",
        element: (
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
                <Outlet />
            </div>
        </div>
        ),
        children:[
            {
                path: "/friend",
                element:  <Friend/>,
                index: true,
              },
              {
                // path: "/chat",
                index: true,
                element:  <Chat/>
              }
        ]
      },
    
])

export default router