import {useDispatch, useSelector} from "react-redux";
import {addCount} from "../store/homeData";


function Home(){
    const dispatch = useDispatch()
    console.dir(addCount)
    const count = useSelector((state)=>state.home.count);
    return(
        <div>
            <button onClick={()=>dispatch(addCount(11))}>add</button>
            <span>home</span>
            <span>home count: {count}</span>
        </div>
    )
}

export default Home