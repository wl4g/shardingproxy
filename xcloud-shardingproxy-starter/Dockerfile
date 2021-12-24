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
## 1). for example:
##      export appName=myapp
##      export appActive=pro
##      export appPort=8080
##      export appDataDir="/mnt/disk1/${appName}"
##      export appLogDir="/mnt/disk1/log/${appName}"
##      #export appOpts="--server.servlet.contextPath=/myapp"
##      #export javaOpts="-Xms1G -Xmx1G"
##      #export javaToolOptions='-javaagent:/opt/javaagent/jmx_prometheus_javaagent-0.16.1.jar=10105:/opt/javaagent/jmx-metrics.yml'
##      docker run -d \
##        -e SHARDING_NOHUP=y \
##        -e SHARDING_DEBUG=y \
##        -e SHARDING_ACTIVE=${appActive} \
##        -e SHARDING_PORT=${appPort} \
##        -e SHARDING_DATA=${appDataDir} \
##        -e SHARDING_LOG=${appLogDir} \
##        -e SHARDING_OPTS=${appOpts} \
##        -e JAVA_OPTS=${javaOpts} \
##        -e JAVA_TOOL_OPTIONS=${javaToolOptions} \
##        -p ${appPort}:${appPort} \
##        -v ${appDataDir}:${appDataDir} \
##        -v ${appLogDir}:${appLogDir} \
##        --name=${appName} ${appName}:latest

FROM openjdk:8-jre-alpine AS wl4g_base
LABEL maintainer="Wanglsir<983708408@qq.com>, Wanglsir<Wanglsir@gmail.com>"

RUN echo "http://mirrors.aliyun.com/alpine/v3.8/main" > /etc/apk/repositories \
&& echo "http://mirrors.aliyun.com/alpine/v3.8/community" >> /etc/apk/repositories \
&& apk update upgrade \
&& apk add --no-cache procps unzip curl bash tzdata \
&& ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
&& echo "Asia/Shanghai" > /etc/timezone

FROM wl4g_base
LABEL maintainer="Wanglsir<983708408@qq.com>, Wanglsir<Wanglsir@gmail.com>"

ARG SHARDING_NAME
ARG SHARDING_VERSION
ARG SHARDING_MAINCLASS
ARG SHARDING_EXT_CLASSPATH # Optional
ENV SHARDING_BIN_NAME "${SHARDING_NAME}-${SHARDING_VERSION}-bin"
ENV SHARDING_HOME_PARENT "/opt/apps/ecm/${SHARDING_NAME}-package/"
ENV SHARDING_HOME "${SHARDING_HOME_PARENT}/${SHARDING_BIN_NAME}"

# Installcation app files.
COPY target/${SHARDING_BIN_NAME}.tar /tmp/${SHARDING_BIN_NAME}.tar

RUN mkdir -p ${SHARDING_HOME_PARENT} \
&& tar -xf /tmp/${SHARDING_BIN_NAME}.tar -C ${SHARDING_HOME_PARENT} \
&& rm -rf /tmp/${SHARDING_BIN_NAME}.tar \
# Make control script.
&& touch /startup.sh && chmod +x /startup.sh \
&& echo -e "#!/bin/bash" >>/startup.sh \
&& echo -e "\
# Define app basic the immutable properties.\n\
export SHARDING_NAME=${SHARDING_NAME:-}\n\
export SHARDING_VERSION=${SHARDING_VERSION:-}\n\
export SHARDING_MAINCLASS=${SHARDING_MAINCLASS:-}\n\
export SHARDING_EXT_CLASSPATH=${SHARDING_EXT_CLASSPATH:-}\n\
# Define app basic the mutable properties.\n\
export SHARDING_NOHUP=\$(echo \$SHARDING_NOHUP|tr '[a-z]' '[A-Z]')\n\
export SHARDING_DEBUG=\$(echo \$SHARDING_DEBUG|tr '[a-z]' '[A-Z]')\n\
export SHARDING_PORT=\${SHARDING_PORT:-'<defaultPort>'}\n\
export SHARDING_ACTIVE=\${SHARDING_ACTIVE:-pro}\n\
export SHARDING_DATA=\${SHARDING_DATA:-/mnt/disk1/\$SHARDING_NAME}\n\
export SHARDING_LOG=\${SHARDING_LOG:-/mnt/disk1/log/\$SHARDING_NAME}\n\
export SHARDING_OPTS=\${SHARDING_OPTS:-}\n\
# Define JVM options.\n\
export JAVA_OPTS=\${JAVA_OPTS:-'-Xms256M -Xmx1G'}\n\
export JAVA_TOOL_OPTIONS=\${JAVA_TOOL_OPTIONS:-}\n\
[[ \$SHARDING_NOHUP == 'TRUE' || \$SHARDING_NOHUP == 'YES' || \$SHARDING_NOHUP == 'Y' || \$SHARDING_NOHUP == 'ON' ]] && export CMD_NOHUP='nohup'\n\
[[ \$SHARDING_DEBUG == 'TRUE' || \$SHARDING_DEBUG == 'YES' || \$SHARDING_DEBUG == 'Y' || \$SHARDING_DEBUG == 'ON' ]] && export SHARDING_DEBUG_OPTS='-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n'\n\
[[ \$SHARDING_PORT != '' && \$SHARDING_PORT != '<defaultPort>' ]] && export SHARDING_PORT_OPTS='--server.port=\${SHARDING_PORT}'\n\
# Initialization.\n\
mkdir -p \${SHARDING_DATA} && mkdir -p \${SHARDING_LOG}\n\
# Make run commands.\n\
export CMD_EXEC=\"\${CMD_NOHUP} java -server \${SHARDING_DEBUG_OPTS} \${JAVA_OPTS} -XX:HeapDumpPath=\${SHARDING_LOG}/jvm_dump.hprof \
-XX:InitiatingHeapOccupancyPercent=35 -XX:+DisableExplicitGC -XX:+HeapDumpOnOutOfMemoryError \
-XX:-OmitStackTraceInFastThrow -XX:+UseG1GC -XX:MaxGCPauseMillis=20 \
-Xloggc:\${SHARDING_LOG}/\${SHARDING_NAME}-gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps \
-XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M \
-Djava.awt.headless=true -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom -cp \
.:\${SHARDING_HOME}/conf:\${SHARDING_EXT_CLASSPATH}:\${SHARDING_HOME}/ext-lib/*:\${SHARDING_HOME}/lib/* \${SHARDING_MAINCLASS} \
--spring.application.name=\${SHARDING_NAME} \
--spring.profiles.active=\${SHARDING_ACTIVE} \
\${SHARDING_PORT_OPTS} \
--server.tomcat.basedir=\${SHARDING_DATA} \
--logging.file.name=\${SHARDING_LOG}/\${SHARDING_NAME}_\${SHARDING_ACTIVE}.log \${SHARDING_OPTS} \
1>\${SHARDING_LOG}/\${SHARDING_NAME}_\${SHARDING_ACTIVE}.stdout \
2>\${SHARDING_LOG}/\${SHARDING_NAME}_\${SHARDING_ACTIVE}.stderr\"\n\
# Print run commands. (if debugging enabled)\n\
[ -n \$SHARDING_DEBUG ] && echo \$CMD_EXEC\n\
# Execution.\n\
echo \"Starting \${SHARDING_NAME}:\${SHARDING_VERSION}(\${SHARDING_ACTIVE}) on \${SHARDING_PORT} ...\"\n\
exec \${CMD_EXEC}\n\
echo \"Started \${SHARDING_NAME}:\${SHARDING_VERSION}(\${SHARDING_ACTIVE}) on \${SHARDING_PORT}\"\n" >>/startup.sh

#EXPOSE ${SHARDING_PORT} # Mapping ports when delegating to a start container.

# [FIX]: Notice that if you use bash or sh to start the program, graceful termination cannot be achieved under kubernetes.
#CMD ["/bin/bash", "-c", "/startup.sh"]
CMD ["/startup.sh"]