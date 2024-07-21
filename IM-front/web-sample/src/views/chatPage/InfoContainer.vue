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
    <el-button @click="login">登录</el-button>
    <el-button @click="logout">下线</el-button>
  </div>
</template>

<script>
import {mapState} from "vuex"
export default {
  props:{

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
    login(){
      this.$message.success("login")
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
