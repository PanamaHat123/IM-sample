
<template>
  <div class="chat-view">
    <div class="records">
      <div class="record"
           v-for="(record,index) in records" :key="index">
        <div class="record-right" v-if="record.fromId === $store.state.infoForm.fromId">
          <span>{{record.messageBody}}</span>
          <span> &nbsp;&nbsp;:&nbsp;{{record.fromId}}</span>
        </div>
        <div v-else class="record-left">
          <span>{{record.fromId}}&nbsp;:&nbsp;&nbsp;</span>
          <span>{{record.messageBody}}</span>
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

export default {
  data(){
    return {
      message:"",
      seq:5,
      records:[
        // {
        //   fromId:"app01",
        //   toId:"app02",
        //   messageBody:"hello",
        //   seq:1
        // },
        // {
        //   fromId:"app02",
        //   toId:"app01",
        //   messageBody:"hi",
        //   seq:2
        // },
        // {
        //   fromId:"app01",
        //   toId:"app02",
        //   messageBody:"nice to hear you",
        //   seq:3
        // },
        // {
        //   fromId:"app02",
        //   toId:"app01",
        //   messageBody:"me too",
        //   seq:4
        // },
        // {
        //   fromId:"app02",
        //   toId:"app01",
        //   messageBody:"how are you?",
        //   seq:5
        // },
      ]
    }
  },
  created() {
    this.init()
  },

  methods:{
    init(){
      Pubsub.subscribe("P2PMessage",(name,param)=>{
        console.log(name,param)
        if(param.fromId==this.$store.state.conversation.toId){
          //message to current conversation
          let messageBodyStr = param.messageBody
          if(messageBodyStr){
            let messageObj = JSON.parse(messageBodyStr)
            let message = messageObj.content
            let record = {
              fromId:param.fromId,
              toId:this.$store.state.infoForm.fromId,
              messageBody:message,
              seq:this.seq++
            }
            this.records.push(record)
          }
        }
      })
      Pubsub.subscribe("GroupMessage",(name,param)=>{
        console.log(name,param)
      })
    },
    sendMsg(){
      let record = {
        fromId:this.$store.state.infoForm.fromId,
        toId:this.$store.state.conversation.toId,
        messageBody:this.message,
        seq:this.seq++
      }
      this.records.push(record)

      if (!this.$store.state.conversation.toId) {
        return this.$message.error("no toId")
      }
      let messagePack = this.$tim.createP2PTextMessage(this.$store.state.conversation.toId,this.message );
      this.$tim.sendP2PMessage(messagePack);
      this.message = "";

    },
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
  background: yellow;
}
.input{
  width: 100%;
  height: 20%;
  background: #42b983;
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
