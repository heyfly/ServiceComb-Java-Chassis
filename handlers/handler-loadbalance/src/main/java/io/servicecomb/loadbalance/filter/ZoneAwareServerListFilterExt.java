/*
 * Copyright 2017 Huawei Technologies Co., Ltd
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

package io.servicecomb.loadbalance.filter;

import java.util.ArrayList;
import java.util.List;

import com.netflix.loadbalancer.Server;

import io.servicecomb.loadbalance.CseServer;
import io.servicecomb.loadbalance.ServerListFilterExt;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;

public class ZoneAwareServerListFilterExt implements ServerListFilterExt {

  @Override
  public List<Server> getFilteredListOfServers(List<Server> list) {
    List<Server> result = new ArrayList<>();
    MicroserviceInstance myself = RegistryUtils.getMicroserviceInstance();
    boolean find = false;
    for (Server server : list) {
      CseServer cseServer = (CseServer) server;
      if (regionAndAZMatch(myself, cseServer.getInstance())) {
        result.add(cseServer);
        find = true;
      }
    }

    if (!find) {
      for (Server server : list) {
        CseServer cseServer = (CseServer) server;
        if (regionMatch(myself, cseServer.getInstance())) {
          result.add(cseServer);
          find = true;
        }
      }
    }

    if (!find) {
      result = list;
    }
    return result;
  }

  private boolean regionAndAZMatch(MicroserviceInstance myself, MicroserviceInstance target) {
    if (myself.getDataCenterInfo() != null && target.getDataCenterInfo() != null) {
      return myself.getDataCenterInfo().getRegion().equals(target.getDataCenterInfo().getRegion()) &&
          myself.getDataCenterInfo().getAvailableZone().equals(target.getDataCenterInfo().getAvailableZone());
    }
    return false;
  }

  private boolean regionMatch(MicroserviceInstance myself, MicroserviceInstance target) {
    if (myself.getDataCenterInfo() != null && target.getDataCenterInfo() != null) {
      return myself.getDataCenterInfo().getRegion().equals(target.getDataCenterInfo().getRegion());
    }
    return false;
  }
}
