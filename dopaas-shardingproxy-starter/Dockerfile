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
## 1). Example:
##      export appName=myapp
##      export appPort=8080
##      export appDataDir="/mnt/disk1/${appName}"
##      export appLogDir="/mnt/disk1/log/${appName}"
##      #export javaToolOptions='-javaagent:/opt/javaagent/jmx_prometheus_javaagent-0.16.1.jar=10105:/opt/javaagent/jmx-metrics.yml'
##      #export javaOpts=""
##      #export javaHeapOpts='-Xms4G -Xmx4G -XX:MaxDirectMemorySize=4G'
##      #export javaDumpOpts='-XX:+HeapDumpOnOutOfMemoryError -XX:-OmitStackTraceInFastThrow -XX:HeapDumpPath=${appLogDir}/jvm_dump.hprof'
##      #export javaGcOpts='-XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:+DisableExplicitGC -Xloggc:${appLogDir}/${appName}-gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M'
##      docker run -d \
##        -e SHARDING_NOHUP=y \
##        -e SHARDING_DEBUG=y \
##        -e SHARDING_PORT=${appPort} \
##        -e SHARDING_DATA=${appDataDir} \
##        -e SHARDING_LOG=${appLogDir} \
##        -e JAVA_TOOL_OPTIONS=${javaToolOptions} \
##        -e JAVA_OPTS=${javaOpts} \
##        -e JAVA_HEAP_OPTS=${javaHeapOpts} \
##        -e JAVA_DUMP_OPTS=${javaDumpOpts} \
##        -e JAVA_GC_OPTS=${javaGcOpts} \
##        -p ${appPort}:${appPort} \
##        -v ${appDataDir}:${appDataDir} \
##        -v ${appLogDir}:${appLogDir} \
##        --name=${appName} ${appName}:latest

# see:https://blogs.wl4g.com/archives/2969
FROM openjdk:8u212-jre-alpine3.9 AS wl4g_base
LABEL maintainer="Wanglsir<983708408@qq.com>, Wanglsir<Wanglsir@gmail.com>"

RUN echo "http://mirrors.aliyun.com/alpine/v3.8/main" > /etc/apk/repositories \
&& echo "http://mirrors.aliyun.com/alpine/v3.8/community" >> /etc/apk/repositories \
&& apk update upgrade \
&& apk add --no-cache procps unzip curl bash tzdata \
&& ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
&& echo "Asia/Shanghai" > /etc/timezone

FROM wl4g_base
LABEL maintainer="Wanglsir<983708408@qq.com>, Wanglsir<Wanglsir@gmail.com>"

##
## Define the immutable & default properties.
##
ARG SHARDING_NAME
ARG SHARDING_VERSION
ARG SHARDING_MAINCLASS
ARG SHARDING_EXT_CLASSPATH # Optional
ENV SHARDING_NAME "${SHARDING_NAME}"
ENV SHARDING_VERSION "${SHARDING_VERSION}"
ENV SHARDING_MAINCLASS "${SHARDING_MAINCLASS}"
ENV SHARDING_EXT_CLASSPATH "${SHARDING_EXT_CLASSPATH}"
ENV SHARDING_BIN_NAME "${SHARDING_NAME}-${SHARDING_VERSION}-bin"
ENV SHARDING_HOME_PARENT "/opt"
ENV SHARDING_HOME "${SHARDING_HOME_PARENT}/${SHARDING_NAME}"
ENV SHARDING_HOME_LINK "${SHARDING_HOME_PARENT}/${SHARDING_NAME}-current"
ENV SHARDING_CONF "${SHARDING_HOME}/conf"
ENV SHARDING_DATA "${SHARDING_HOME}/data"
ENV SHARDING_LOG "${SHARDING_HOME}/log"

##
## Installation APP files.
##
COPY target/${SHARDING_BIN_NAME}.tar /tmp/${SHARDING_BIN_NAME}.tar

RUN mkdir -p ${SHARDING_HOME} ${SHARDING_DATA} ${SHARDING_LOG} && ln -snf ${SHARDING_HOME} ${SHARDING_HOME_LINK} \
&& tar -xf /tmp/${SHARDING_BIN_NAME}.tar --strip-components=1 -C ${SHARDING_HOME} \
&& rm -rf /tmp/${SHARDING_BIN_NAME}.tar \
\
## Make control script.
&& touch /docker-entrypoint.sh && chmod +x /docker-entrypoint.sh \
&& echo -e "#!/bin/bash" >>/docker-entrypoint.sh \
&& echo -e "\
## Define the mutable APP properties.\n\
export SHARDING_PORT=\${SHARDING_PORT:-'3308'}\n\
\
## Define the mutable JVM options.\n\
export JAVA_OPTS=\${JAVA_OPTS:-}\n\
\
export DEFAULT_JAVA_HEAP_OPTS=\"-XX:InitialRAMPercentage=80.0 -XX:MinRAMPercentage=80.0 -XX:MaxRAMPercentage=80.0\" \n\
export JAVA_HEAP_OPTS=\${JAVA_HEAP_OPTS:-\${DEFAULT_JAVA_HEAP_OPTS}}\n\
\
export DEFAULT_JAVA_DUMP_OPTS=\"-XX:+HeapDumpOnOutOfMemoryError -XX:-OmitStackTraceInFastThrow -XX:HeapDumpPath=\${SHARDING_LOG}/jvm_dump.hprof\" \n\
export JAVA_DUMP_OPTS=\${JAVA_DUMP_OPTS:-\${DEFAULT_JAVA_DUMP_OPTS}}\n\
\
export DEFAULT_JAVA_GC_OPTS=\"-XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:+DisableExplicitGC -Xloggc:\${SHARDING_LOG}/\${SHARDING_NAME}-gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M\" \n\
export JAVA_GC_OPTS=\${JAVA_GC_OPTS:-\${DEFAULT_JAVA_GC_OPTS}}\n\
\
export JAVA_TOOL_OPTIONS=\${JAVA_TOOL_OPTIONS:-}\n\
\
[ -n \"\$SHARDING_NOHUP\" ] && export CMD_NOHUP='nohup'\n\
[ -n \"\$SHARDING_DEBUG\" ] && export JVM_DEBUG_OPTS='-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n'\n\
\
## Make run commands.\n\
export CMD_EXEC=\"\${CMD_NOHUP} java -server \
-javaagent:\${SHARDING_HOME}/ext-lib/agentlib/dopaas-shardingproxy-agent-bootstrap-\${SHARDING_VERSION}.jar \
\${JAVA_OPTS} \${JAVA_HEAP_OPTS} \${JAVA_DUMP_OPTS} \${JAVA_GC_OPTS} \${JVM_DEBUG_OPTS} \
-Djava.net.preferIPv4Stack=true -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom -cp \
.:\${SHARDING_HOME}/conf:\${SHARDING_EXT_CLASSPATH}:\${SHARDING_HOME}/ext-lib/*:\${SHARDING_HOME}/lib/* \${SHARDING_MAINCLASS} \
\${SHARDING_PORT} \${SHARDING_CONF} \
1>\${SHARDING_LOG}/\${SHARDING_NAME}.stdout \
2>\${SHARDING_LOG}/\${SHARDING_NAME}.stderr\"\n\
\
## Print run commands.\n\
[ -n \"\$SHARDING_DEBUG\" ] && echo \$CMD_EXEC\n\
\
## Execution.\n\
echo \"Starting \${SHARDING_NAME}:\${SHARDING_VERSION} on \${SHARDING_PORT} ...\"\n\
exec \${CMD_EXEC}\n\
echo \"Started \${SHARDING_NAME}:\${SHARDING_VERSION} on \${SHARDING_PORT}\"\n" >>/docker-entrypoint.sh

# Using root-less run container. see:https://docs.docker.com/engine/security/rootless/
# see:https://stackoverflow.com/questions/49955097/how-do-i-add-a-user-when-im-using-alpine-as-a-base-image
RUN adduser ${SHARDING_NAME} --disabled-password \
&& mkdir -p ${SHARDING_HOME} \
&& chown -R ${SHARDING_NAME}:${SHARDING_NAME} ${SHARDING_HOME} /docker-entrypoint.sh \
&& chmod -R 755 ${SHARDING_HOME} /docker-entrypoint.sh
USER ${SHARDING_NAME}

# [FIX]: Notice that if you use bash or sh to start the program, graceful termination cannot be achieved under kubernetes.
#CMD ["/bin/bash", "-c", "/docker-entrypoint.sh"]
CMD ["/docker-entrypoint.sh"]