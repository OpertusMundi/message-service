#!/bin/sh
set -u -e
#set -x

function _gen_configuration()
{
    cat <<-EOD
	spring.datasource.url = jdbc:postgresql://${DATABASE_HOST:-localhost}:${DATABASE_PORT:-5432}/${DATABASE_NAME}
	spring.datasource.username = ${DATABASE_USERNAME}
	spring.datasource.password = $(cat ${DATABASE_PASSWORD_FILE} | tr -d '\n')
	
	opertus-mundi.security.jwt.secret = $(cat ${JWT_SECRET_FILE} | tr -d '\n')
	EOD
}

default_java_opts="-server -Djava.security.egd=file:///dev/urandom -Xms128m"
java_opts="${JAVA_OPTS:-${default_java_opts}}"

runtime_profile=$(hostname | hexdump -e '"%02x"')
_gen_configuration > ./config/application-${runtime_profile}.properties

exec java ${java_opts} -cp "/app/classes:/app/dependency/*" eu.opertusmundi.message.Application \
  --spring.profiles.active=production,${runtime_profile}
