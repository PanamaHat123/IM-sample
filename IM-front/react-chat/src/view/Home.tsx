import {useDispatch, useSelector} from "react-redux";
import {addCount} from "../store/homeData";
import type {AppDispatch, RootState} from "../store/store"
import { Button } from 'antd';
import {StepBackwardOutlined, StepForwardOutlined} from '@ant-design/icons'

function Home(){
    const dispatch = useDispatch<AppDispatch>()
    console.dir(addCount)
    const count = useSelector((state: RootState )=>state.home.count);
    return(
        <div className="home">
            <Button onClick={()=>dispatch(addCount(11))}>add</Button>
            <div className="test">
                <span>home</span>
            </div>
            <StepBackwardOutlined />
            <StepForwardOutlined />
            <span>home count: {count}</span>
        </div>
    )
}

export default Home