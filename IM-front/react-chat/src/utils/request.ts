
import axios,{AxiosInstance,AxiosRequestConfig,AxiosResponse} from "axios";
import { BaseResponse } from "../model/response/base";

class Request{

    private instance :AxiosInstance | undefined;

    constructor(config:AxiosRequestConfig){
        this.instance = axios.create(config)


        this.instance.interceptors.response.use((res:AxiosResponse<BaseResponse<any>,any>)=>{
            if((res.data as BaseResponse<any>).code!==200){
                window.alert(res.data.msg)
                throw new Error(res.data.msg);
            }
            return res
        },
        (err)=>{
            window.alert(err.msg)
        })
    }

    request<T>(config:AxiosRequestConfig):Promise<AxiosResponse<T>>{
        return new Promise<AxiosResponse>((resolve,reject)=>{
            this.instance?.request(config).then(res=>resolve(res))
            .catch(err=>reject(err))
        });
    }

    post<T>(url:string,data:object):Promise<BaseResponse<T>>{
        return new Promise((reslove,reject)=>{
            this.instance?.post(url,data).then((response)=>{
                reslove(response.data as BaseResponse<T>)
            })
            .catch(errResponse=>{
                reject(errResponse.data as BaseResponse<null>)
            })
        })
    }

    get<T>(url:string,param:object):Promise<T>{
        return new Promise((reslove,reject)=>{
            this.instance?.get(url,param).then((response)=>{
                reslove(response.data as T)
            })
            .catch(errResponse=>{
                reject(errResponse.data)
            })
        })
    }
}

export default new Request({
    baseURL:"http://localhost:8080"
})