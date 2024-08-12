
import Conversation from "./Conversation/index"
import ChatBox from "./ChatBox/index"
import "./index.less"

const Chat = () => {
  return (
    <div className="chatContainer">
      <Conversation />
      <ChatBox />
    </div>
  )
}


export default Chat
