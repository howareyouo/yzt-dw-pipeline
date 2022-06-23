################################################################################
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
# limitations under the License.
################################################################################

FROM openjdk:8-jre-alpine


ENV TZ=Asia/Shanghai

### Set timezone of the container. See https://serverfault.com/a/683651 
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.tuna.tsinghua.edu.cn/g' /etc/apk/repositories & \
    apk add --no-cache tzdata bash snappy curl gcompat && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone


# Flink environment variables
ENV FLINK_INSTALL_PATH=/opt
ENV FLINK_HOME $FLINK_INSTALL_PATH/flink
ENV FLINK_LIB_DIR $FLINK_HOME/lib
ENV FLINK_PLUGINS_DIR $FLINK_HOME/plugins
ENV FLINK_OPT_DIR $FLINK_HOME/opt
ENV FLINK_JOB_ARTIFACTS_DIR $FLINK_INSTALL_PATH/artifacts
ENV FLINK_USR_LIB_DIR $FLINK_HOME/usrlib
ENV PATH $PATH:$FLINK_HOME/bin

# flink-dist can point to a directory or a tarball on the local system
ARG flink_dist=$FLINK_INSTALL_PATH/flink-1.10.1-bin-scala_2.11.tgz
ARG job_artifacts=./yzt-dw-pipeline/build/libs


RUN wget -O ${flink_dist} https://apachemirror.sg.wuchna.com/flink/flink-1.10.1/flink-1.10.1-bin-scala_2.11.tgz \
    && tar -xzf ./${flink_dist} -C $FLINK_INSTALL_PATH \
    && rm ./${flink_dist}

ADD $job_artifacts/* $FLINK_JOB_ARTIFACTS_DIR/


RUN set -x && \
  ln -s $FLINK_INSTALL_PATH/flink-[0-9]* $FLINK_HOME && \
  ln -s $FLINK_JOB_ARTIFACTS_DIR $FLINK_USR_LIB_DIR && \
  addgroup -S flink && adduser -D -S -H -G flink -h $FLINK_HOME flink && \
  chown -R flink:flink ${FLINK_INSTALL_PATH}/flink-* && \
  chown -R flink:flink ${FLINK_JOB_ARTIFACTS_DIR}/ && \
  chown -h flink:flink $FLINK_HOME

COPY ./docker/docker-entrypoint.sh /

USER flink
EXPOSE 8081 6123
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["--help"]
