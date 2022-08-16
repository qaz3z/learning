# 基于哪个镜像
FROM java:8
ENV JVM_OPTS '-Xms256M -Xmx256M -Xmn128M -Xss1M -XX:MetaspaceSize=128M -XX:MaxMetaspaceSize=128M -XX:+HeapDumpOnOutOfMemoryError -XX:NewRatio=1 -XX:SurvivorRatio=30 -XX:+UseParallelGC -XX:+UseParallelOldGC'
ENV JAR_PATH '/home/soft/workspace/demo-study-1.0.0-SNAPSHOT.jar'
RUN pwd
# 复制文件到容器
RUN pwd && ls
ADD target/demo-study-1.0.0-SNAPSHOT.jar ${JAR_PATH}

# 配置容器启动后执行的命令
ENTRYPOINT java ${JVM_OPTS} -Djava.security.egd=file:/dev/./urandom -jar ${JAR_PATH}
