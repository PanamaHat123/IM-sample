<template>
  <div class="firend-container">
    <div class="friend-item"  v-for="(f,i) in firendList" :key="f.toId" >
      <span @click="selectFriend(f.toId)" :class="f.toId==$store.state.conversation.toId?'current':''">
        {{f.toId}}
      </span>
    </div>
    <div>
      <el-button @click="init">refresh</el-button>
    </div>
  </div>
</template>


<script>
import {mapState,mapMutations} from "vuex";
import Pubsub from "pubsub-js"
export default {
  props:{

  },
  data(){
    return {
      firendList:[],
    }
  },
  computed:{
  },
  async created() {
    console.log(this.$store)
    Pubsub.subscribe("socketConnected",(name,params)=>{
      this.init()
    })
  },
  methods:{
    ...mapMutations(["setConversationToId"]),
    async init(){
      this.firendList = await this.getFriendList()
    },
    async getFriendList(){
      try {
        let res = await this.$tim.getAllFriend()
        if(res.code != 200){
          throw new Error(res.msg)
        }
        return res.data
      } catch (e) {
        this.$message.error(e.msg || "get friend failed")
      }
    },
    selectFriend(toId){
      this.setConversationToId(toId)
    }
  }
}
</script>


<style>
.firend-container{
  width: 100%;
  height: 100%;
}
.friend-item{
  width: 100%;
  height: 40px;
  display: flex;
  align-items: center;
  padding-left: 10px;
  cursor: pointer;
  border-bottom: 1px rebeccapurple solid;
}
.current{
  color: red;
}
</style>
