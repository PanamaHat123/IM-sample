import {useEffect, useLayoutEffect, useState} from 'react'
import './App.css'
import {BrowserRouter, Routes, Route, Link} from "react-router-dom";
import Home from "./view/Home";
import Mine from "./view/Mine";

function App() {
    const [count, setCount] = useState(0)

    useEffect(()=>{
        console.log("useEffect",count)
    },[])
    useLayoutEffect(()=>{
        console.log("useLayoutEffect")
    })

    const click = ()=>{
        setCount(count+1)
    }
    return (
        <>
            <BrowserRouter>
                <div>
                    <div>
                        <Link to="/">home</Link>
                    </div>
                    <div onClick={click}>
                        <Link to="/mine">mine</Link>
                    </div>
                </div>
                <div>{count}</div>
                <div>
                    <Routes>
                        <Route path="/" element={<Home count={count}/>}/>
                        <Route path="/mine" element={<Mine/>}/>
                    </Routes>
                </div>
            </BrowserRouter>
        </>
    )
}

export default App
