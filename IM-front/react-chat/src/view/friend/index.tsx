import "./index.less"


const Friend = () => {
    return (
        <div className='friendContainer'>
            <div className="friendList">

                <div className="friendItem">
                    <div className="head"></div>
                    <div className="name">Jack</div>
                </div>

                <div className="friendItem">
                    <div className="head"></div>
                    <div className="name">Lee</div>
                </div>

                <div className="friendItem">
                    <div className="head"></div>
                    <div className="name">Kris</div>
                </div>

            </div>
            <div className="friendInfo">
                <div className="head"></div>
                <div className="name">Jack</div>
                <div className="introduce">this is a introduce personally</div>
            </div>
        </div>
    )
}

export default Friend
