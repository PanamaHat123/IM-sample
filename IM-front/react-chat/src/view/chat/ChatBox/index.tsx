import { AppstoreAddOutlined, ProductOutlined, SendOutlined, ShareAltOutlined, SmileOutlined } from "@ant-design/icons"
import "./index.less"
import MessageBox from "./MessageBox"

const ChatBox = () => {
  return (
    <div className="chat-box">
      <div className="chat-title">
        <div className="name">张三</div>
        <div className="operate">
            <ShareAltOutlined onClick={()=>console.log('ok')}/>
            <ProductOutlined onClick={()=>console.log('ok')} />
        </div>
      </div>
      <div className="chat-message">
            <MessageBox/>
      </div>
      <div className="chat-typing">
        <div className="icon">
            <SmileOutlined onClick={()=>console.log('ok')}/>
        </div>
        <div className="extend">
            <AppstoreAddOutlined onClick={()=>console.log('ok')}/>
        </div>
        <div className="typing">
            <input type="text"/>
        </div>
        <div className="send-btn">
            <SendOutlined onClick={()=>console.log('ok')}/>
        </div>
      </div>
    </div>
  )
}

export default ChatBox
