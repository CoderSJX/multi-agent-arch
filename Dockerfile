# 使用官方的Java运行时作为基础镜像
FROM openjdk:17-bullseye


# 设置工作目录
WORKDIR /app

# 将jar包从本地复制到容器的工作目录中
COPY target/mss-ai.jar app.jar

# 设置环境变量，比如JAVA_OPTS（如果需要）
ENV JAVA_OPTS=""

# 暴露容器服务端口
EXPOSE 8080

# 定义容器启动时执行的命令
ENTRYPOINT [ "sh", "-c", "java -jar /app/app.jar" ]