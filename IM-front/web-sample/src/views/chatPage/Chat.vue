
<template>
  <div class="chat-view">
    <div class="records" id="scroll-container">
      <div class="record"
           v-for="(msg,index) in $store.state.conversation.records[current.conversationId]" :key="index">
        <div class="record-right" v-if="msg.fromId === $store.state.infoForm.fromId">
          <span>{{msg.messageObj?msg.messageObj.content:msg.messageBody}}</span>
          <span> &nbsp;&nbsp;:&nbsp;{{msg.fromId}}</span>
        </div>
        <div v-else class="record-left">
          <span>{{msg.fromId}}&nbsp;:&nbsp;&nbsp;</span>
          <span>{{msg.messageObj?msg.messageObj.content:msg.messageBody}}</span>
        </div>
      </div>
    </div>
    <div class="input">
      <el-input type="textarea" v-model="message" @keyup.enter.native="sendMsg"/>
      <el-button type="primary" @click="sendMsg">send</el-button>
    </div>
  </div>
</template>

<script>
import Pubsub from "pubsub-js"
import {mapMutations,mapState} from "vuex";

export default {
  data(){
    return {
      message:"",
      seq:5,
    }
  },
  computed:{
    ...mapState("conversation",["current","records"])
  },
  created() {
    this.init()
    console.log("chat create")

  },

  methods:{
    ...mapMutations("conversation",["addRecord"]),
    init(){
      if (Pubsub.getSubscriptions("P2PMessage").length > 0) {
        return
      }

      Pubsub.subscribe("P2PMessage",(name,param)=>{
        console.log(name,param)
        console.log("current conversation",this.current)
        if(param.fromId === this.current.toId){
          //message to current conversation
          let messageBodyStr = param.messageBody
          if(messageBodyStr){
            let messageObj = JSON.parse(messageBodyStr)
            param.messageObj = messageObj
          }
          this.addRecord({conversationId:this.current.conversationId,message:param})
          this.$forceUpdate()
        }else{

        }
        param.conversationId = this.current.conversationId
        this.$db.records.add(param)
        this.$nextTick(()=>{
          this.scroll()
        })

      })
      Pubsub.subscribe("GroupMessage",(name,param)=>{
        console.log(name,param)
      })
    },
    sendMsg(){

      if(!this.message)return

      if (!this.current.toId) {
        return this.$message.error("no toId")
      }
      let messagePack = this.$tim.createP2PTextMessage(this.current.toId,this.message );
      this.$tim.sendP2PMessage(messagePack);

      console.log("send messagePack",messagePack)
      messagePack.messageObj = JSON.parse(messagePack.messageBody)
      this.addRecord({conversationId:this.current.conversationId,message:messagePack})
      messagePack.conversationId = this.current.conversationId
      this.$db.records.add(messagePack)
      this.message = "";
      this.$nextTick(()=>{
        this.scroll()
      })
    },
    scroll(){
      const container = document.getElementById('scroll-container');
      container.scrollTop = container.scrollHeight;
    }
  }
}

</script>


<style scoped>

.chat-view{
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.records{
  height: 80%;
  width: 100%;
  background: azure;
  overflow-y: auto;
}

.input{
  width: 100%;
  height: 20%;
  background: #ffffff;
}
.record{
  width: 100%;
  height: 50px;
  border-bottom: 1px solid #42b983;
}
.record-left{
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: start;
  align-items: center;
}
.record-right{
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: end;
  align-items: center;
}
</style>
