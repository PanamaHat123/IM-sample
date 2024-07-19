import axios from 'axios'
import config from "../config/env.js"

axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'

const service = axios.create({
  baseURL: config.baseApiUrl,
  timeout: 15000
})
service.interceptors.request.use(config => {

  return config
}, error => {
    console.log(error)
    Promise.reject(error)
})

service.interceptors.response.use(res => {

    return res.data
  },
  error => {

    return Promise.reject(error)
  }
)

export default service
