/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package broker;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;


/**
 *
 * @author osman
 */
public class MappingBroker extends DatacenterBroker {

    public MappingBroker(String name) throws Exception {
        super(name);
    }

    public void setMapping(int[] mapping) {
        int i = 0;
        for (Cloudlet cl : getCloudletList()) {
            cl.setVmId(mapping[i++]);
        }
    }
    
}
