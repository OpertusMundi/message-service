#!/bin/sh
set -u -e -o pipefail

[[ "${DEBUG:-false}" != "false" || "${XTRACE:-false}" != "false" ]] && set -x

function _gen_configuration()
{
    if echo ${DATABASE_URL} | grep -v -q -e '^jdbc:postgresql:[/][/]' ; then
        echo "DATABASE_URL does not seem like a JDBC PostgreSQL connection string" 1>&2 && exit 1 
    fi
    database_url=${DATABASE_URL}
    database_username=${DATABASE_USERNAME}
    database_password=$(cat ${DATABASE_PASSWORD_FILE} | tr -d '\n')

    jwt_secret=$(cat ${JWT_SECRET_FILE} | tr -d '\n')
    
    cat <<-EOD
	spring.datasource.url = ${database_url}
	spring.datasource.username = ${database_username}
	spring.datasource.password = ${database_password}
	
	opertus-mundi.security.jwt.secret = ${jwt_secret}
	EOD
}

runtime_profile=$(hostname | md5sum | head -c10)
_gen_configuration > ./config/application-${runtime_profile}.properties

logging_config="classpath:config/log4j2.xml"
if [[ -f "./config/log4j2.xml" ]]; then
    logging_config="file:config/log4j2.xml"
fi

# Run

main_class=eu.opertusmundi.message.Application
default_java_opts="-server -Djava.security.egd=file:///dev/urandom -Xms128m"
exec java ${JAVA_OPTS:-${default_java_opts}} -cp "/app/classes:/app/dependency/*" ${main_class} \
    --spring.profiles.active=production,${runtime_profile} --logging.config=${logging_config}

