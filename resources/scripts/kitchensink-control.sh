#! /bin/bash
# kitchensink.sh

# Used to deploy and undeploy the Wildfly Kitchensink demo application into a running Wildfly instance and
# execute member registration against the deployed app.

# Usage './kitchensink [deploy|undeploy|redeploy|register] <n>'
# If the number of re-deploys is not specified the default (1000) is used.

JBOSS_CLI=/opt/java/wildfly/bin/jboss-cli.sh
HOST_IP=192.168.59.103
APP_URL=http://$HOST_IP:8180/wildfly-kitchensink
APP_NAME=wildfly-kitchensink.war
WAR=`pwd`/$APP_NAME

# Set a default number of iterations
if [ "$2" -eq "$2" ] 2>/dev/null
then
   NUMBER_OF_ITERATIONS="$2"
else
   NUMBER_OF_ITERATIONS=1000
fi

function registerMember {
  for j in $(eval echo {1..$1})
  do
  	jsf_token=`curl -s -c cookies.txt $APP_URL/index.jsf | xmlstarlet sel -N x="http://www.w3.org/1999/xhtml" -t -m "//x:input[6]" -v @value -n`
    # Need to use the HTML form to register data, since memory leak is only triggerd this way. JSON request take different code path
    curl -s -b cookies.txt --data "reg=reg&reg:name=John%20Doe&reg:email=john_$j@mailinator.com&reg:phoneNumber=00000000000&reg:register=Register&javax.faces.ViewState=$jsf_token" $APP_URL/index.jsf > /dev/null
    echo "   Registering member $j"
    #printf "   "
    #curl http://$HOST_IP:8180/wildfly-kitchensink/rest/members/$j
    #printf "\n"
  done
  rm cookies.txt
}

function deploy {
  echo "Deploying $APP_NAME"
  $JBOSS_CLI -u=admin -p=admin --connect --controller=$HOST_IP:10090 --command="deploy --force $WAR"
}

function undeploy {
  echo "Undeploying $APP_NAME"
  $JBOSS_CLI -u=admin -p=admin --connect --controller=$HOST_IP:10090 --command="undeploy $APP_NAME"
}

function redeploy {
  printf "\n"
  for i in $(eval echo {1..$1})
  do
    deploy
    registerMember 3
    undeploy
    printf "\n"
  done
}

case "$1" in
  'deploy')
    deploy
    ;;
  'undeploy')
    undeploy
    ;;
  'redeploy')
    redeploy $NUMBER_OF_ITERATIONS
    ;;
  'register')
    registerMember $NUMBER_OF_ITERATIONS
    ;;
  *) echo "Usage: $0 [deploy|undeploy|redeploy|register] [n]"
    ;;
esac



