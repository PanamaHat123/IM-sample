<template>
  <div class="info-container">
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
    <el-button @click="login">Login</el-button>
    <el-button @click="logout">Logout</el-button>
  </div>
</template>

<script>
import {mapState} from "vuex"
export default {
  props:{
    listenerMap:{
      default:{},
      required:true
    }
  },
  data(){
    return {

    }
  },

  computed: {
    ...mapState(["infoForm"]),
  },
  created() {

  },
  methods:{
    async login(){
      let data = {
        ...this.$store.state.infoForm,
        userId:this.$store.state.infoForm.fromId
      }
      try {
        let listeners = {};
        for (const v in this.listenerMap) {
          listeners[v] = this.listenerMap[v];
        }
        // from "http://127.0.0.1:8080/v1"  get a tcp node address
        // connect to netty
        this.$tim.init("http://127.0.0.1:8080/v1",data.appId,data.userId,"",listeners,(sdk)=>{
          console.log('sdk connected,  callback');

        })

      } catch (e) {
        this.$message.error(e.msg || "catch login failed")
      }

    },
    logout(){
      this.$message.error("logout")
    }
  }
}
</script>

<style>
.info-container{
  width: 100%;
  height: 100%;
}
</style>
