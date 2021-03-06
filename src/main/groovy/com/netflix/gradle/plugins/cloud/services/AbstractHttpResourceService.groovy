/*
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package com.netflix.gradle.plugins.cloud.services

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest
import com.amazonaws.services.ec2.model.IpPermission
import com.netflix.gradle.plugins.cloud.model.DeploymentExtension

abstract class AbstractHttpResourceService extends AbstractResourceService<DeploymentExtension> {
  String lastAmi

  AbstractHttpResourceService(DeploymentExtension ext) {
    super(ext)
  }

  @Override
  protected int getCheckPort() {
    ext.getCheckPort()
  }

  @Override
  protected void configureSecurityGroup() {
    try {
      ec2.describeSecurityGroups(new DescribeSecurityGroupsRequest(groupNames: [getSecurityGroup()]))
    } catch (AmazonServiceException e) {
      ec2.createSecurityGroup(new CreateSecurityGroupRequest(getSecurityGroup(), "${getName()} Security Group"))
    }
    try {
      ec2.authorizeSecurityGroupIngress(new AuthorizeSecurityGroupIngressRequest(getSecurityGroup(),
          [new IpPermission(fromPort: 22, toPort: 22, ipRanges: ['0.0.0.0/0'], ipProtocol: 'tcp'),
           new IpPermission(fromPort: 8080, toPort: 8080, ipRanges: ['0.0.0.0/0'], ipProtocol: 'tcp')]))
    } catch (AmazonServiceException e) {
      println("Could not authorize SSH access to Bakery security group!")
      e.printStackTrace()
    }
  }
}
