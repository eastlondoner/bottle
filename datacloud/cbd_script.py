#!/usr/bin/env python

import time
import urllib2
import json
import sys
import os
import subprocess


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
        print returned_data
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
    except Exception, e:
        print(e)
        sys.exit(1)


if __name__ == '__main__':

    try:
        if "OS_USERNAME" in os.environ and "OS_PASSWORD" in os.environ and "OS_REGION_NAME" in os.environ:
            username = os.environ['OS_USERNAME']
            apikey = os.environ['OS_PASSWORD']
            region = os.environ['OS_REGION_NAME']
        else:
            print("OS_USERNAME, OS_PASSWORD, OS_REGION_NAME environment variables are required")
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


    # create profile
    print("Creating profile...")
    profile_username = "alexandru"
    profile_password = "AsdQwe!23"
    try:
        body = {"profile": {
            "username": profile_username,
            "password": profile_password,
            "cloudCredentials": {
                "username": str(username),
                "apikey": str(apikey)
            }}}
        request_string = json.dumps(body)
        request = urllib2.Request(endpoint + "/profile", data=request_string)
        add_headers(request)
        response = urllib2.urlopen(request)
        returned_data = json.loads(response.read())
        cluster_data['credentials'] = {'username': profile_username, 'password': profile_password}
    except Exception, e:
        print(e)
        sys.exit(1)


    # build cluster
    cluster_name = "alex-test"
    cluster_type = "HADOOP_HDP2_1"
    cluster_flavor = "hadoop1-7"
    cluster_nodes = 2
    try:
        body = {"cluster": {
            "name": cluster_name,
            "clusterType": cluster_type,
            "flavorId": cluster_flavor,
            "nodeCount": cluster_nodes
        }}

        # check if cluster already exists
        cluster_data['id'] = ""
        request = urllib2.Request(endpoint + "/clusters")
        add_headers(request)
        response = urllib2.urlopen(request)
        returned_data = json.loads(response.read())
        for cluster in returned_data['clusters']:
            if cluster['name'] == cluster_name:
                print("Cluster {0} already exists!".format(cluster_name))
                cluster_data['id'] = cluster['id']
        if not cluster_data['id']:
            print("Building cluster...")
            request_string = json.dumps(body)
            request = urllib2.Request(endpoint + "/clusters", data=request_string)
            add_headers(request)
            response = urllib2.urlopen(request)
            returned_data = json.loads(response.read())
            cluster_data['id'] = returned_data['cluster']['id']
    except Exception, e:
        print(e)
        sys.exit(1)


    # wait for the cluster to be created
    print("Waiting for the cluster to be built...")
    try:
        request = urllib2.Request(endpoint + "/clusters/" + cluster_data['id'])
        add_headers(request)
        cluster_data['status'] = "BUILDING"

        remaining_wait_time = 30
        while remaining_wait_time > 0:
            response = urllib2.urlopen(request)
            returned_data = json.loads(response.read())
            if returned_data['cluster']['status'] == "ACTIVE":
                cluster_data['status'] = "ACTIVE"
                break
            print("Time left to wait is {0} minutes".format(remaining_wait_time))
            time.sleep(60)
            remaining_wait_time -= 1

        if cluster_data['status'] != "ACTIVE":
            print("Cluster not built yet. Exiting...")
            sys.exit(1)

    except Exception, e:
        print(e)
        sys.exit(1)


    # get nodes details
    print("Getting the nodes details...")
    try:
        request = urllib2.Request(endpoint + "/clusters/" + cluster_data['id'] + "/nodes")
        add_headers(request)
        response = urllib2.urlopen(request)
        returned_data = json.loads(response.read())
        cluster_data['datanodes'] = []
        for node in returned_data['nodes']:
            node_info = {
                    'name': node['name'],
                    'public_v4_address': node['addresses']['public'][0]['addr'],
                    'private_v4_address': node['addresses']['private'][0]['addr']
            }
            if node['role'] == "GATEWAY":
                cluster_data['gateway'] = node_info
            if node['role'] == "NAMENODE":
                cluster_data['namenode'] = node_info
            if node['role'] == "SECONDARY-NAMENODE":
                cluster_data['secondary-namenode'] = node_info
            if node['role'] == "DATANODE":
                cluster_data['datanodes'].append(node_info)
    except Exception, e:
        print(e)
        sys.exit(1)


    # run job
    data_input = "swift://testhadoop.rack-lon/test_file.txt"
    data_output = "swift://testhadoop.rack-lon/out"
    if subprocess.call("sshpass -p '" + cluster_data['credentials']['password'] + "' ssh -o StrictHostKeyChecking=no "
                       + cluster_data['credentials']['username'] + "@" + cluster_data['gateway']['public_v4_address']
                       + " 'hadoop jar /usr/lib/hadoop-mapreduce/hadoop-mapreduce-examples.jar wordcount "
                       + data_input + " " + data_output + "' > /dev/null",
                       shell=True) != 0:
        print("Error executing the job. Exiting...")
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