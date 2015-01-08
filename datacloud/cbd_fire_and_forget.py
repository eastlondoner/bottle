#!/usr/bin/env python

import time
import urllib2
import json
import sys
import os
import random
import string

# variables
cluster_name = os.environ.get("cluster_name") or "scorpio-test"
cluster_type = os.environ.get("cluster_type") or "HADOOP_HDP2_1"
cluster_flavor = os.environ.get("cluster_flavor") or "hadoop1-7"
cluster_nodes = os.environ.get("cluster_nodes") or 2
post_install_script = os.environ["post_install_script"]
profile_username = os.environ.get("profile_username") or ''.join([random.choice(string.ascii_letters + string.digits) for n in xrange(16)])
profile_password = os.environ.get("profile_password") or "ILikeRandomPasswordsThatIncludeAtLeastOneUpperCaseOneLoverCase1DigitAndOneSpecialCharacter!"
wait = True
wait_time = 40  # minutes

print(profile_username)

def auth():
    print("Authenticating to Rackspace cloud...")

    try:

        if password:
            print "using password auth"
            request_string = '{"auth":{"passwordCredentials":{"username":"' + \
                             str(username) + '", "password":"' + str(password) + '"}}}'
        else:
            print "using api key auth"
            request_string = '{"auth":{"RAX-KSKEY:apiKeyCredentials":{"username":"' + \
                             str(username) + '", "apiKey":"' + str(api_key) + '"}}}'

        request = urllib2.Request("https://identity.api.rackspacecloud.com/v2.0/tokens", data=request_string)
        request.add_header('Accept', 'application/json')
        request.add_header('Content-Type', 'application/json')
        response = urllib2.urlopen(request)
        returned_data = json.loads(response.read())
        print(returned_data)
        token = returned_data['access']['token']['id']
        id = returned_data['access']['token']['tenant']['id']
        userId = returned_data['access']['user']['id']
        endpoint = ""
        for catalog in returned_data['access']['serviceCatalog']:
            if catalog['name'] == 'cloudBigData':
                for e in catalog['endpoints']:
                    if e['region'] == region:
                        endpoint = e['publicURL']
                break
        if token and region and endpoint and id:
            print "Authentication was successful."
            return [token, region, endpoint, userId]
        else:
            print "Authentication was not successful."
            sys.exit(1)
    except Exception, e:
        print(e)
        sys.exit(1)


if __name__ == '__main__':

    print("Deploying to Rackspace cloud...")
    try:
        if "OS_USERNAME" in os.environ and ("OS_PASSWORD"  in os.environ or "OS_API_KEY" in os.environ) and "OS_REGION_NAME" in os.environ:
            username = os.environ['OS_USERNAME']
            password = os.environ['OS_PASSWORD']
            api_key = os.environ['OS_API_KEY']
            region = os.environ['OS_REGION_NAME']
        else:
            print("OS_USERNAME, OS_PASSWORD/OS_API_KEY, OS_REGION_NAME environment variables are required")
            sys.exit(1)
    except Exception, e:
        print(e)
        sys.exit(1)

    token, region, endpoint, userId = auth()
    cluster_data = {}

    def add_headers(request):
        request.add_header('X-Auth-Token', str(token))
        request.add_header('Accept', 'application/json')
        request.add_header('Content-Type', 'application/json')

    # get API key
    apiEndpoint = "https://identity.api.rackspacecloud.com/v2.0/users/" + userId + "/OS-KSADM/credentials/RAX-KSKEY:apiKeyCredentials"
    request = urllib2.Request(apiEndpoint)
    add_headers(request)
    response = urllib2.urlopen(request)
    returned_data = json.loads(response.read())
    cloud_username = returned_data["RAX-KSKEY:apiKeyCredentials"]["username"]
    cloud_apikey = returned_data["RAX-KSKEY:apiKeyCredentials"]["apiKey"]

    # create profile
    print("Creating profile...")
    try:
        ##TODO: if the profile already exists we get an error
        body = {"profile": {
            "username": profile_username,
            "password": profile_password,
            "sshkeys" : [],
            "cloudCredentials": {
                "username": str(cloud_username),
                "apikey": str(cloud_apikey)
            }}}
        request_string = json.dumps(body)
        print(endpoint + "/profile")
        print(request_string)
        request = urllib2.Request(endpoint + "/profile", data=request_string)
        add_headers(request)
        response = urllib2.urlopen(request)
        returned_data = json.loads(response.read())
        cluster_data['credentials'] = {'username': profile_username, 'password': profile_password}
    except Exception, e:
        print("Error creating profile")
        print(e)
        sys.exit(1)


    # build cluster
    try:
        body = {"cluster": {
            "name": cluster_name,
            "clusterType": cluster_type,
            "flavorId": cluster_flavor,
            "nodeCount": cluster_nodes
        }}
        if post_install_script:
            body['cluster']["postInitScript"] = post_install_script
            print("Post Install Script: " + post_install_script)

        ##TODO: if the account big data node limit is reached we get an error

        # check if cluster already exists
        cluster_data['id'] = ""
        request = urllib2.Request(endpoint + "/clusters")
        add_headers(request)
        response = urllib2.urlopen(request)
        returned_data = json.loads(response.read())
        for cluster in returned_data['clusters']:
            if cluster['name'] == cluster_name and cluster.status == "ACTIVE":
                print("Cluster {0} already exists!".format(cluster_name))
                cluster_data['id'] = cluster['id']
        if not cluster_data['id']:
            print("Building cluster...")
            request_string = json.dumps(body)
            print(request_string)
            request = urllib2.Request(endpoint + "/clusters", data=request_string)
            add_headers(request)
            response = urllib2.urlopen(request)
            returned_data = json.loads(response.read())
            print(returned_data)
            cluster_data['id'] = returned_data['cluster']['id']
    except Exception, e:
        print(e)
        sys.exit(1)

    if wait:
        # wait for the cluster to be created
        print("Waiting for the cluster to be built...")
        try:
            request = urllib2.Request(endpoint + "/clusters/" + cluster_data['id'])
            add_headers(request)
            cluster_data['status'] = "BUILDING"

            remaining_wait_time = wait_time
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

            if post_install_script:
                print("Waiting for the post install script to finish")
                while remaining_wait_time > 0:
                    cluster_data['id'] = ""
                    request = urllib2.Request(endpoint + "/clusters")
                    add_headers(request)
                    response = urllib2.urlopen(request)
                    returned_data = json.loads(response.read())
                    print(returned_data)
                    for cluster in returned_data['clusters']:
                        if cluster['name'] == cluster_name:
                            cluster_data['id'] = cluster['id']
                            cluster_data['status'] = cluster['status']
                    if not cluster_data['id'] or cluster_data['status'] == "DELETING":
                        print "Cluster was deleted."
                        sys.exit(0)

                    request = urllib2.Request(endpoint + "/clusters/" + cluster_data['id'])
                    add_headers(request)
                    response = urllib2.urlopen(request)
                    returned_data = json.loads(response.read())
                    if returned_data['cluster']['postInitScriptStatus'] == "SUCCEEDED":
                        print("Postinstall script executed successfully")
                        break
                    elif returned_data['cluster']['postInitScriptStatus'] == "FAILED":
                        print("Postinstall script executed with an error")
                        sys.exit(1)
                    print("Time left to wait is {0} minutes".format(remaining_wait_time))
                    time.sleep(60)
                    remaining_wait_time -= 1
                if cluster_data['status'] != "SUCCEEDED":
                    print("Post install script didn't complete successfully...")
                    sys.exit(1)

        except Exception, e:
            print(e)
            sys.exit(1)