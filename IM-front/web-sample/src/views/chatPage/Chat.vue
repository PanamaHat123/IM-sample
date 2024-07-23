
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
      <el-button type="primary" @click="sendBackMsg">sendBack</el-button>
    </div>
  </div>
</template>

<script>

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
  },
  methods:{
    sendMsg(){
      let record = {
        fromId:"app01",
        toId:"app02",
        messageBody:this.message,
        seq:this.seq++
      }
      this.records.push(record)
      this.message = ""
    },
    sendBackMsg(){
      let record = {
        fromId:"app02",
        toId:"app01",
        messageBody:this.message,
        seq:this.seq++
      }
      this.records.push(record)
      this.message = ""
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
