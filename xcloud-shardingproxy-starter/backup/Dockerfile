# Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
## 1). Run container for example:
##      export SHARDING_NAME=myapp
##      export SHARDING_LOG_DIR=/mnt/disk1/log/${SHARDING_NAME}
##      docker run -d \
##        -e=APP_NOHUP=y \
##        -e=SHARDING_DEBUG=y \
##        -e=SHARDING_LOG_DIR=${SHARDING_LOG_DIR} \
##        -v=${SHARDING_LOG_DIR}:${SHARDING_LOG_DIR} \
##        --name=${SHARDING_NAME} ${SHARDING_NAME}:latest /bin/bash

FROM openjdk:8-jre-alpine
LABEL maintainer="Wanglsir<983708408@qq.com>, Wanglsir<Wanglsir@gmail.com>"

ARG SHARDING_NAME
ARG SHARDING_VERSION
ARG SHARDING_MAINCLASS
ARG SHARDING_CLASSPATH # Optional
ENV SHARDING_BIN_NAME "${SHARDING_NAME}-${SHARDING_VERSION}-bin"
ENV SHARDING_HOME_PARENT "/opt/apps/ecm/${SHARDING_NAME}-package/"
ENV SHARDING_HOME "${SHARDING_HOME_PARENT}/${SHARDING_BIN_NAME}"

RUN echo "http://mirrors.aliyun.com/alpine/v3.8/main" > /etc/apk/repositories \
&& echo "http://mirrors.aliyun.com/alpine/v3.8/community" >> /etc/apk/repositories \
&& apk update upgrade \
&& apk add --no-cache procps unzip curl bash tzdata \
&& ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
&& echo "Asia/Shanghai" > /etc/timezone

COPY target/${SHARDING_BIN_NAME}.tar /tmp/${SHARDING_BIN_NAME}.tar

RUN mkdir -p ${SHARDING_HOME_PARENT}
RUN tar -xf /tmp/${SHARDING_BIN_NAME}.tar -C ${SHARDING_HOME_PARENT} \
&& rm -rf /tmp/${SHARDING_BIN_NAME}.tar

# Make script.
RUN touch /start.sh && chmod +x /start.sh
RUN echo -e '#!/bin/bash' >>/start.sh
RUN echo -e "export SHARDING_NAME=${SHARDING_NAME}" >>/start.sh
# Support configurable environments.
RUN echo -e "export SHARDING_LOG_DIR=\${SHARDING_LOG_DIR:-/mnt/disk1/log/\$SHARDING_NAME}" >>/start.sh
RUN echo -e "[ -z \"\$APP_NOHUP\" ] && export CMD_NOHUP=\"nohup\"" >>/start.sh
RUN echo -e "[ -n \"\$SHARDING_DEBUG\" ] && export DEBUG_OPTS=\"-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n\"" >>/start.sh
RUN echo -e "echo \"Starting \${SHARDING_NAME}:${SHARDING_VERSION} ... \"\n" >>/start.sh
RUN echo -e "mkdir -p \${SHARDING_LOG_DIR}\n" >>/start.sh

# Startup commands.
RUN echo -e "cmdRun=\"\${CMD_NOHUP} java -server \${DEBUG_OPTS} -Xms256M -Xmx1G \
-XX:MaxGCPauseMillis=20 -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:LargePageSizeInBytes=128m \
-XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 -XX:InitiatingHeapOccupancyPercent=35 \
-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${SHARDING_LOG_DIR}/jvm_dump.hprof -Xloggc:${SHARDING_LOG_DIR}/shardingproxy-gc.log -verbose:gc \
-XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M \
-Dfile.encoding=UTF-8 -Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom \
-cp .:${SHARDING_HOME}/conf:${SHARDING_CLASSPATH}:${SHARDING_HOME}/ext-lib/*:${SHARDING_HOME}/lib/* ${SHARDING_MAINCLASS} \
1>${SHARDING_LOG_DIR}/${SHARDING_NAME}.stdout \
2>${SHARDING_LOG_DIR}/${SHARDING_NAME}.stderr\"" >>/start.sh

# Print. (if debug mode)
RUN echo -e "\n[ -n \"\$SHARDING_DEBUG\" ] && echo \$cmdRun\n" >>/start.sh

# Execute.
RUN echo -e "exec \${cmdRun}" >>/start.sh
RUN echo -e "\necho \"Started \${SHARDING_NAME}:${SHARDING_VERSION} \"\n" >>/start.sh

CMD ["/bin/bash", "-c", "/start.sh"]
