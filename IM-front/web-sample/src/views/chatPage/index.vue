<template>
  <div class="chat-container">

    <div class="chat-box">
      <div class="left-side"></div>
      <div class="right-side"></div>
    </div>
    <div class="info-box">
      <el-form ref="infoForm" :model="infoForm" label-width="80px">
        <el-form-item label="fromId" prop="fromId">
          <el-input v-model="infoForm.fromId" placeholder="fromId" />
        </el-form-item>
        <el-form-item label="appId" prop="appId">
          <el-input v-model="infoForm.appId" placeholder="appId" />
        </el-form-item>
        <el-form-item label="clientType" prop="clientType">
          <el-input v-model="infoForm.clientType" placeholder="clientType" />
        </el-form-item>
        <el-form-item label="imei" prop="imei">
          <el-input v-model="infoForm.imei" placeholder="imei" />
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import {queryAllFriendShip} from "../../api/baseApi"
export default {
  name: 'ChatPage',
  data () {
    return {
      msg: 'Welcome to Your Vue.js App',
      infoForm:{
        fromId: "app01",
        appId:10000,
        clientType:1,
        imei:1
      }
    }
  },
  async created() {
    console.log(await this.getFriendList())
  },
  methods:{
    async getFriendList(){
      let data = {
        fromId:"app01"
      }
      try {
        let res = await queryAllFriendShip(10000, data)
        if(res.code != 200){
          throw new Error(res.msg)
        }
        return res.data
      } catch (e) {
        this.$message.error(e.msg || "get friend failed")
      }
    },
    async queryGroupList(){

    }
  }
}
</script>

<style scoped>
.chat-container{
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}
.chat-box{
  width: 1200px;
  height: 800px;
  background: #42b983;
  display: flex;
}
.left-side{
  width: 30%;
  height: 100%;
  background: palegoldenrod;
}
.right-side{
  width: 70%;
  height: 100%;
  background: skyblue;
}
.info-box{
  width: 300px;
  height: 800px;
  background: cadetblue;
}
</style>
