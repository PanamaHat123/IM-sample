#!/bin/sh

#nohup java  -Duser.timezone=GMT+08 -jar ./tcp.jar --spring.profiles.active=prod &> tcp_take_out.log &
java  -Duser.timezone=GMT+08 -jar ./tcp.jar --spring.profiles.active=prod

echo "tcp start up success!"

sleep 100000000
echo "infinity end"
#
#sleep 3
#
#nohup java  -Duser.timezone=GMT+08 -jar ./service.jar --spring.profiles.active=prod &> service_take_out.log &
#
#echo "service start up success!"
#
#sleep 3
#
#nohup java  -Duser.timezone=GMT+08 -jar ./messageStore.jar --spring.profiles.active=prod &> messageStore_take_out.log &
#
#echo "messageStore start up success!"