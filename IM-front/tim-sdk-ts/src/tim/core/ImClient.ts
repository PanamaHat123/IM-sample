import log from "../log/Logger";
import ByteBuffer from '../codec/ByteBuffer';
import { LoginPack } from '../pack/LoginPack';
import WebToolkit from '../common/WebToolkit';
import { w3cwebsocket, IMessageEvent, ICloseEvent } from 'websocket';
import { RequestBase } from '../model/RequestBase';
import { RequestParams } from '../model/RequestParams';
import HttpApi from '../api/HttpApi';
import Beans from '../common/utils';
import { reverse } from 'dns';
import {
    MessageCommand,
    FriendShipCommand,
    GroupCommand,
    SystemCommand,
    UserEventCommand,
    ConversationEventCommand
} from '../common/Command';
import { MessagePack } from '../pack/MessagePack';
import { MessageContent } from '../model/dto/MessageContent';

const loginTimeout = 10 * 1000 // 10 seconds
const heartbeatInterval = 10 * 1000 // seconds
let firstMonitorSocket: boolean = false;// first ? socket

export enum State {
    INIT,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    CLOSEING,
    CLOSED,
}

enum TimeUnit {
    Second = 1000,
    Millisecond = 1,
}

export let sleep = async (second: number, Unit: TimeUnit = TimeUnit.Second): Promise<void> => {
    return new Promise((resolve, _) => {
        setTimeout(() => {
            resolve()
        }, second * Unit)
    })
}

export interface IListener {
    onLogin(userId: string): void; // login success
    onSocketConnectEvent(url: string, data: any): void; // socket connect success
    onSocketErrorEvent(e: any): void;// exception event
    onSocketReConnectEvent(): void;// reconnecting
    onSocketReConnectSuccessEvent(): void;// reconnect success
    onSocketCloseEvent(): void;//connect close
    onP2PMessage(e: any): void;//p2p message
    onTestMessage(e: any): void;// testing use
    // onOfflineMessage(data):void; // pull offlineMessage
}

export class ImClient {

    url: string = ""
    userId!: string
    version: number = 1
    clientType: number = 1
    imei!: string;
    listeners: IListener | any = null;
    appId!: number
    userSign!: string;
    imeiLength?: number
    state = State.INIT
    lastOfflineMessageSequence: number = 0;
    offlineMessageList: Array<any> = new Array<any>()
    httpUrl: string = ""//http://127.0.0.1:8000/v1

    private conn?: w3cwebsocket

    constructor() {

    }

    public getRequestBase(): RequestBase {
        return new RequestBase(this.appId, this.clientType, this.imei);
    }

    public isInit(): boolean {
        return this.state == State.CONNECTED;
    }

    public getRequestParams(): RequestParams {
        return new RequestParams(this.appId, this.userId, this.userSign);
    }

    public async init(httpUrl: string, appId: number, userId: string, userSign: string, listeners: any, callback: (sdk: ImClient) => void) {
        let self = this;
        self.httpUrl = httpUrl
        self.appId = appId;
        self.listeners = listeners
        self.imei = WebToolkit.getDeviceInfo().system;
        self.imeiLength = getLen(self.imei);
        self.userId = userId;
        this.userSign = userSign
        this.imeiLength = self.imeiLength;
        if (Beans.isEmpty(this.url)) {
            log.info("get im addr")
            let api = new HttpApi(this.httpUrl);
            let resp = await api.call("/user/login", {}, { clientType: this.clientType, appId: this.appId, userId: this.userId })
            if (resp.isFailed()) {
                log.info("login failed：get im address failed")
                return;
            }
            let ip = resp.data.ip;
            let port = resp.data.port;
            this.url = "ws://" + ip + ":" + port + "/ws";
        }

        let req = new LoginPack(self.appId, self.userId, 1);
        let { success, err, conn } = await limLogin(self.url, req, self);
        if (success) {
            if (!firstMonitorSocket) {
                firstMonitorSocket = true;
            }
            conn.onerror = (error) => {
                log.info("websocket error: ", error)
                if (typeof imClient.listeners.onSocketErrorEvent === 'function') {
                    imClient.listeners.onSocketErrorEvent(error);
                }
                this.errorHandler(error, req)
            }

            conn.onclose = (e: ICloseEvent) => {
                log.info("event[onclose] fired")
                if (self.state == State.CLOSEING) {
                    this.onclose("logout")
                    return
                }

                if (typeof imClient.listeners.onSocketCloseEvent === 'function') {
                    imClient.listeners.onSocketCloseEvent();
                }
                this.errorHandler(new Error(e.reason), req)
            }

            conn.onmessage = (evt) => {
                let bytebuf = new ByteBuffer(evt.data);
                let byteBuffer = bytebuf.int32().int32().unpack();

                let command = byteBuffer[0];// command
                let bodyLen = byteBuffer[1];// bodylen
                let unpack = bytebuf.vstring(null, bodyLen).unpack();//string
                let msgBody = unpack[2];
                console.log("sdk receive server msg：" + msgBody)

                if (command === MessageCommand.MSG_P2P) {
                    //p2p message sending and receiving
                    if (typeof imClient.listeners.onP2PMessage === 'function') {
                        imClient.listeners.onP2PMessage(msgBody);
                    }
                } else {
                    if (typeof imClient.listeners.onTestMessage === 'function') {
                        imClient.listeners.onTestMessage(msgBody);
                    }
                }
            }
            this.conn = conn;
            this.state = State.CONNECTED
            //Pull an offline message
            // this.loadOfflineMessage();
            //Heart beat
            this.heartbeatLoop(this.conn);

            if (typeof imClient.listeners.onLogin === 'function') {
                imClient.listeners.onLogin(this.userId);
            }
            callback(self);

        } else {
            log.error(err?.message)
        }

    }

    public buildMessagePack(command: number, messagePack: any) {
        let jsonData = JSON.stringify(messagePack);
        let bodyLen = getLen(jsonData);

        let pack = new ByteBuffer(null, 0);
        pack.int32(command).int32(this.version).int32(this.clientType)
            .int32(0x0)
            .int32(this.appId)
            .int32(this.imeiLength)
            .int32(bodyLen)
            .vstring(this.imei, this.imeiLength)
            .vstring(jsonData, bodyLen);
        return pack;
    }

    // 4. Automatic reconnection
    private async errorHandler(error: Error, req: LoginPack) {
        // If the connection is actively disconnected, there is no need to automatically reconnect
        // Such as receiving a kick or actively calling the logout() method
        if (this.state == State.CLOSED || this.state == State.CLOSEING) {
            return
        }
        this.state = State.RECONNECTING
        if (typeof imClient.listeners.onSocketReConnectEvent === 'function') {
            imClient.listeners.onSocketReConnectEvent();
        }
        // Reconnect 10 times
        for (let index = 0; index < 10; index++) {
            await sleep(3)
            try {
                log.info("try to relogin")
                // let { success, err } = await this.login()
                let { success, err, conn } = await limLogin(this.url, req, this);
                if (success) {
                    if (typeof imClient.listeners.onSocketReConnectSuccessEvent === 'function') {
                        imClient.listeners.onSocketReConnectSuccessEvent();
                    }
                    return
                }
                log.info(err)
            } catch (error) {
                log.info(error)
            }
        }
        this.onclose("reconnect timeout")
    }

    // Indicating disconnection
    private onclose(reason: string) {
        if (this.state == State.CLOSED) {
            return
        }
        this.state = State.CLOSED

        log.info("connection closed due to " + reason)
        this.conn = undefined
        this.userId = ""

        // The socket closing event was added. Procedure
        if (typeof imClient.listeners.onSocketErrorEvent === 'function') {
            imClient.listeners.onSocketCloseEvent();
        }
    }

    public getSingleUserInfo(uid: string): Promise<any> {
        return new Promise((resolve, _) => {
            let api = new HttpApi(this.httpUrl);
            let resp = api.call("/user/data/getSingleUserInfo", this.getRequestParams(), { userId: uid })
            resolve(resp);
        })
    }

    public async syncGetUserInfo(userId: string[]) {
        let api = new HttpApi(this.httpUrl);
        let resp = api.call("/user/data/getUserInfo", this.getRequestParams(), { userIds: userId })
        return resp;
    }

    public getUserInfo(userId: string[]): Promise<any> {
        return new Promise((resolve, _) => {
            let api = new HttpApi(this.httpUrl);
            let resp = api.call("/user/data/getUserInfo", this.getRequestParams(), { userIds: userId })
            resolve(resp);
        })
    }

    public getAllFriend(): Promise<any> {
        return new Promise((resolve, _) => {
            let api = new HttpApi(this.httpUrl);
            let resp = api.call("/friendship/getAllFriendShip", this.getRequestParams(), { fromId: this.userId })
            resolve(resp);
        })
    }
    //Pull-up group
    public getGroup(params:any): Promise<any> {
        return new Promise((resolve, _) => {
            let api = new HttpApi(this.httpUrl);
            let resp = api.call("/group/syncJoinedGroup", this.getRequestParams(), { operator: this.userId ,maxLimit:params.maxLimit,lastSequence:params.lastSequence})
            resolve(resp);
        })
    }

    //Pull session
    public getConversation(params:any): Promise<any> {
        return new Promise((resolve, _) => {
            let api = new HttpApi(this.httpUrl);
            let resp = api.call("/conversation/syncConversationList", this.getRequestParams(), { operator: this.userId ,maxLimit:params.maxLimit,lastSequence:params.lastSequence})
            resolve(resp);
        })
    }

    //Pull an offline message
    public getOfflineMessage(params:any): Promise<any> {
        return new Promise((resolve, _) => {
            let api = new HttpApi(this.httpUrl);
            let resp = api.call("/message/syncOfflineMessage", this.getRequestParams(), { operator: this.userId ,maxLimit:params.maxLimit,lastSequence:params.lastSequence})
            resolve(resp);
        })
    }

    // 2、heartbeat
    private heartbeatLoop(conn) {
        let start = Date.now()
        let loop = () => {
            if (this.state != State.CONNECTED) {
                log.error("heartbeatLoop exited")
                return
            }
            if (Date.now() - start >= heartbeatInterval) {
                log.info(`>>> send ping ;`)
                start = Date.now()
                let pingPack = imClient.buildMessagePack(SystemCommand.PING, {});
                conn.send(pingPack.pack(false));
            }
            setTimeout(loop, 500)
        }
        setTimeout(loop, 500)
    }


    //Build a single chat message object
    public createP2PTextMessage(to: string, text: string) {
        let messagePack = new MessagePack(this.appId);
        messagePack.buildTextMessagePack(this.userId, to, text);
        return messagePack;
    }


    public sendP2PMessage(pack: MessagePack) {
        let p2pPack = imClient.buildMessagePack(MessageCommand.MSG_P2P, pack);
        if (this.conn) {
            this.conn.send(p2pPack.pack(false));
        }
    }

    //构建群聊消息对象
    public createGroupTextMessage(to: string, text: string) {
        let messagePack = new MessagePack(this.appId);
        messagePack.buildTextMessagePack(this.userId, to, text);
        messagePack.groupId = to;
        return messagePack;
    }

    public sendGroupMessage(pack: MessagePack) {
        let groupPack = imClient.buildMessagePack(GroupCommand.MSG_GROUP, pack);
        if (this.conn) {
            this.conn.send(groupPack.pack(false));
        }
    }



    public getUserId() {
        return this.userId;
    }

    private async loadOfflineMessage() {
        log.info("loadOfflineMessage start")
        let api = new HttpApi(this.httpUrl);
        let resp = await api.call("/message/syncOfflineMessage",this.getRequestParams(),{clientType : this.clientType,appId : this.appId,lastSequence:this.lastOfflineMessageSequence,maxLimit:100})
        if(resp.isSucceed()){
            this.lastOfflineMessageSequence = resp.data.maxSequence;
            let offmessages = resp.data.dataList;
            this.offlineMessageList.push(offmessages)
            if(offmessages.length > 0 && typeof imClient.listeners.onOfflineMessage === 'function'){
                imClient.listeners.onOfflineMessage(offmessages);
            }
            console.log(resp.data.completed)
            if(!resp.data.completed){
                this.loadOfflineMessage();
            }
        }else{
            log.error("loadOfflineMessage - error")
        }
    }

}

export let limLogin = async (url: string, req: LoginPack, imClient: ImClient): Promise<{ success: boolean, err?: Error, conn: w3cwebsocket }> => {
    return new Promise((resolve, _) => {
        let conn = new w3cwebsocket(url)
        conn.binaryType = "arraybuffer"
        log.info("limLogin");
        // Set up a login timeout
        let tr = setTimeout(() => {
            clearTimeout(tr)
            resolve({ success: false, err: new Error("timeout"), conn: conn });
        }, loginTimeout);

        conn.onopen = () => {
            if (conn.readyState == w3cwebsocket.OPEN) {

                // Description Adding a socket connection event
                if (typeof imClient.listeners.onSocketConnectEvent === 'function') {
                    imClient.listeners.onSocketConnectEvent(url, req);
                }
                log.info(`start connecting`);
                //Login packet
                let data = {
                    "userId": req.userId
                }
                let loginPack = imClient.buildMessagePack(0x2328, data);
                conn.send(loginPack.pack(false));
            }
        }
        conn.onerror = (error: Error) => {
            clearTimeout(tr)
            log.error(error)
            resolve({ success: false, err: error, conn: conn });
        }

        conn.onmessage = (evt) => {
            if (typeof evt.data === 'string') {
                log.info("Received: '" + evt.data + "'");
                return
            }
            clearTimeout(tr)

            let bytebuf = new ByteBuffer(evt.data);

            let byteBuffer = bytebuf.int32().int32().unpack();

            let command = byteBuffer[0];
            let bodyLen = byteBuffer[1];
            if (command == 0x2329) {
                resolve({ success: true, conn: conn });

            }

        }

    })


}



export let getLen = (str) => {
    let len = 0;
    for (let i = 0; i < str.length; i++) {
        let c = str.charCodeAt(i);
        //Single byte plus 1
        if ((c >= 0x0001 && c <= 0x007e) || (0xff60 <= c && c <= 0xff9f)) {
            len++;
        } else {
            len += 3;
        }
    }
    return len;
}




export const imClient = new ImClient();


