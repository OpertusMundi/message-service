#!/bin/sh
set -u -e -o pipefail
set -x

function _gen_configuration()
{
    cat <<-EOD
	spring.datasource.url = ${DATABASE_URL}
	spring.datasource.username = ${DATABASE_USERNAME}
	spring.datasource.password = $(cat ${DATABASE_PASSWORD_FILE} | tr -d '\n')
	
	opertus-mundi.security.jwt.secret = $(cat ${JWT_SECRET_FILE} | tr -d '\n')
	EOD
}

# Check environment and generate configuration

if echo ${DATABASE_URL} | grep -v -q -e '^jdbc:postgresql:[/][/]' ; then
   echo "The DATABASE_URL does not seem like a JDBC connection string" 1>&2 && exit 1
fi

runtime_profile=$(hostname | md5sum | head -c 32)
_gen_configuration > ./config/application-${runtime_profile}.properties

# Run

main_class=eu.opertusmundi.message.Application
default_java_opts="-server -Djava.security.egd=file:///dev/urandom -Xms128m"
exec java ${JAVA_OPTS:-${default_java_opts}} -cp "/app/classes:/app/dependency/*" ${main_class} \
    --spring.profiles.active=production,${runtime_profile}

