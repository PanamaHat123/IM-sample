
import './index.less'


const Conversation = () => {

    

  return (
    <div className='conversation-container'>
    
      <div className='conv-list'>
        <div className='conv-item'>
            <div className='head'></div>
            <div className='conv-info'>
                <div className='name'>张三</div>
                <div className='recent'>张三：哈哈</div>
            </div>
            <div className='time'>13:24</div>
        </div>
        <div className='conv-item active'>
            <div className='head'></div>
            <div className='conv-info'>
                <div className='name'>张三</div>
                <div className='recent'>张三：哈哈</div>
            </div>
            <div className='time'>13:24</div>
        </div>
      </div>
    </div>
  )
}

export default Conversation
