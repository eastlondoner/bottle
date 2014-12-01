#!/bin/shrack_region="LON"
input_data_container="Container c"
output_folder="results"
status_file="COMPLETED"
mr_options=""
jar_container="z_DO_NOT_DELETE_scorpio_JARS"
cluster_name="1020b160-7729-11e4-a919-ddcd92b2ecbb"
input_jar="JAR 1"
input_file="input.txt"
output_data_container="Container c-out"
## Shebang line deliberately missing because this is a template


#
# Enable logging
#
exec >> "/var/log/post-install.out" 2>&1


#
# Install sysstat and enable stats collection every minute
#
echo -e "\n\n[$(date)]   Installing and configuring sysstat"
yum -y install sysstat
sed -i 's/\*\/10/\*\/1/' /etc/cron.d/sysstat


#
# Install the swift client
#
echo -e "\n\n[$(date)]   Installing the swift client"
pip install python-swiftclient
[ "$?" != "0" ] && echo "Could not install swift client" && exit 1


#
# Extract Cloud credentials
#
echo -e "\n\n[$(date)]   Extracting credentials"
export OS_USERNAME=$(grep -A1 fs.swift.service.rack-$(echo ${rack_region,,}).username /etc/hadoop/conf/core-site.xml|grep value|grep -oPm1 "(?<=<value>)[^<]+")
export OS_PASSWORD=$(grep -A1 fs.swift.service.rack-$(echo ${rack_region,,}).apikey /etc/hadoop/conf/core-site.xml|grep value|grep -oPm1 "(?<=<value>)[^<]+")
export OS_AUTH_URL=$(grep -A1 fs.swift.service.rack-$(echo ${rack_region,,}).auth.url /etc/hadoop/conf/core-site.xml|grep value|grep -oPm1 "(?<=<value>)[^<]+")
export OS_REGION_NAME=$(grep -A1 fs.swift.service.rack-$(echo ${rack_region,,}).region /etc/hadoop/conf/core-site.xml|grep value|grep -oPm1 "(?<=<value>)[^<]+")
export CLUSTER_NAME=$cluster_name


#
# Increase Swift timeouts
#
echo -e "\n\n[$(date)]   Increasing Swift timeouts"
python << END

import sys, os, re

with open(os.path.expanduser("/etc/hadoop/conf/core-site.xml"), 'r') as f:
    core_site = f.read()

new_core_site = re.sub('</configuration>', '  <property>\n    <name>fs.swift.connect.retry.count</name>\n    <value>10</value>\n  </property>\n  <property>\n    <name>fs.swift.socket.timeout</name>\n    <value>150000</value>\n  </property>\n\n</configuration>', core_site)

with open(os.path.expanduser("/etc/hadoop/conf/core-site.xml"), 'w') as f:
    f.write(new_core_site)
END


#
# On non Gateway nodes, wait for the status file and upload the logs
#
if [ $(hostname) != "GATEWAY-1" ]; then

echo -e "\n\n[$(date)]   Checking status file"
while true; do
    swift -s -A https://auth.api.rackspacecloud.com/v1.0 -U $OS_USERNAME -K $OS_PASSWORD list $output_data_container|grep $output_folder/$status_file
    [ "$?" == "0" ] && echo -e "\n\n[$(date)]   File found" && break
    echo "Waiting for another 20 seconds"
    sleep 20
done

echo -e "\n\n[$(date)]   Uploading logs"
mkdir -p $output_folder/logs/$(hostname)
cp -a /var/log/messages $output_folder/logs/$(hostname)
cp -a /var/log/nova-agent.log $output_folder/logs/$(hostname)
cp -a /var/log/lava-agent.log $output_folder/logs/$(hostname)
LC_ALL=C sar -A > $output_folder/logs/$(hostname)/sar
tar -pcf $output_folder/logs/$(hostname)/hadoop.tar -C /var/log/ hadoop hadoop-hdfs hadoop-mapreduce hadoop-yarn
cp -a /var/log/post-install.out $output_folder/logs/$(hostname)

swift -s -A https://auth.api.rackspacecloud.com/v1.0 -U $OS_USERNAME -K $OS_PASSWORD upload $output_data_container $output_folder/logs
fi


#
# On the Gateway node do all the work
#
if [ $(hostname) == "GATEWAY-1" ]; then

#
# Download the job's jar file
#
echo -e "\n\n[$(date)]   Downloading the jar file"
swift -s -A https://auth.api.rackspacecloud.com/v1.0 -U $OS_USERNAME -K $OS_PASSWORD download $jar_container $input_jar
mv $input_jar /tmp

#
# Execute MR job
#
echo -e "\n\n[$(date)]   Executing the MR job"
su - hdfs -c 'hdfs dfs -mkdir /user/root; hdfs dfs -chown root /user/root'
hadoop jar /tmp/$input_jar $mr_options swift://$input_data_container.rack-$(echo ${rack_region,,})/$input_file swift://$output_data_container.rack-$(echo ${rack_region,,})/$output_folder

#
# Make a status file
#
echo -e "\n\n[$(date)]   Uploading the status file"
mkdir -p $output_folder/logs/$(hostname)
touch $output_folder/$status_file
swift -s -A https://auth.api.rackspacecloud.com/v1.0 -U $OS_USERNAME -K $OS_PASSWORD upload $output_data_container $output_folder/$status_file

#
# Gather all logs and upload them
#
echo -e "\n\n[$(date)]   Uploading logs"
mkdir -p $output_folder/logs/$(hostname)
cp -a /var/log/messages $output_folder/logs/$(hostname)
cp -a /var/log/nova-agent.log $output_folder/logs/$(hostname)
cp -a /var/log/lava-agent.log $output_folder/logs/$(hostname)
LC_ALL=C sar -A > $output_folder/logs/$(hostname)/sar
tar -pcf $output_folder/logs/$(hostname)/hadoop.tar -C /var/log/ hadoop-hdfs hadoop-mapreduce hadoop-yarn
cp -a /var/log/post-install.out $output_folder/logs/$(hostname)

swift -s -A https://auth.api.rackspacecloud.com/v1.0 -U $OS_USERNAME -K $OS_PASSWORD upload $output_data_container $output_folder/logs

#
# Wait for the other nodes to upload the logs
#
echo -e "\n\n[$(date)]   Waiting for the other nodes to upload the logs"
sleep 120

#
# Delete the cluster
#
echo -e "\n\n[$(date)]   Deleting the cluster"
python << END

import urllib2
import json
import sys
import os


def auth():
    print("Authenticating to Rackspace cloud...")

    try:
        request_string = '{"auth":{"RAX-KSKEY:apiKeyCredentials":{"username":"' + \
                         str(username) + '", "apiKey":"' + str(apikey) + '"}}}'
        request = urllib2.Request("https://identity.api.rackspacecloud.com/v2.0/tokens", data=request_string)
        request.add_header('Accept', 'application/json')
        request.add_header('Content-Type', 'application/json')
        response = urllib2.urlopen(request)
        returned_data = json.loads(response.read())
        token = returned_data['access']['token']['id']
        id = returned_data['access']['token']['tenant']['id']
        endpoint = ""
        for catalog in returned_data['access']['serviceCatalog']:
            if catalog['name'] == 'cloudBigData':
                for e in catalog['endpoints']:
                    if e['region'] == region:
                        endpoint = e['publicURL']
                break
        if token and region and endpoint and id:
            print "Authentication was successful."
            return [token, region, endpoint]
        else:
            print "Authentication was not successful."
            sys.exit(1)
    except Exception, e:
        print(e)
        sys.exit(1)


if __name__ == '__main__':

    try:
        if "OS_USERNAME" in os.environ and "OS_PASSWORD" in os.environ and "OS_REGION_NAME" in os.environ and "CLUSTER_NAME" in os.environ:
            username = os.environ['OS_USERNAME']
            apikey = os.environ['OS_PASSWORD']
            region = os.environ['OS_REGION_NAME']
            cluster_name = os.environ['CLUSTER_NAME']
        else:
            print("OS_USERNAME, OS_PASSWORD, OS_REGION_NAME, CLUSTER_NAME environment variables are required")
            sys.exit(1)
    except Exception, e:
        print(e)
        sys.exit(1)

    token, region, endpoint = auth()
    cluster_data = {}

    def add_headers(request):
        request.add_header('X-Auth-Token', str(token))
        request.add_header('Accept', 'application/json')
        request.add_header('Content-Type', 'application/json')

    # get cluster id
    try:
        cluster_data['id'] = ""
        request = urllib2.Request(endpoint + "/clusters")
        add_headers(request)
        response = urllib2.urlopen(request)
        returned_data = json.loads(response.read())
        for cluster in returned_data['clusters']:
            if cluster['name'] == cluster_name:
                cluster_data['id'] = cluster['id']
        if not cluster_data['id']:
            print("Could not find cluster {0}".format(cluster_name))
            sys.exit(1)
    except Exception, e:
        print(e)
        sys.exit(1)

    # delete cluster
    print("Deleting cluster...")
    try:
        request = urllib2.Request(endpoint + "/clusters/" + cluster_data['id'])
        add_headers(request)
        request.get_method = lambda: 'DELETE'
        response = urllib2.urlopen(request)
        returned_data = json.loads(response.read())
    except Exception, e:
        print(e)
        sys.exit(1)
END

fi