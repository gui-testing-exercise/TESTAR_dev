/***************************************************************************************************
 *
 * Copyright (c) 2020 Open Universiteit - www.ou.nl
 * Copyright (c) 2020 Universitat Politecnica de Valencia - www.upv.es
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package org.testar.monkey;

import org.testar.monkey.alayer.SUT;
import org.testar.monkey.alayer.Tags;
import org.testar.monkey.alayer.exceptions.SystemStartException;
import org.testar.plugin.NativeLinker;

import java.util.List;

public class WindowsProcessNameSutConnector implements SutConnector {

    private String processName;
    private double maxEngageTime;

    public WindowsProcessNameSutConnector(String processName, double maxEngageTime) {
        this.processName = processName;
        this.maxEngageTime = maxEngageTime;
    }

    @Override
    public SUT startOrConnectSut() throws SystemStartException {
        Assert.hasTextSetting(processName, "SUTConnectorValue");
        List<SUT> suts = null;
        long now = System.currentTimeMillis();
        do{
            Util.pauseMs(100);
            suts = NativeLinker.getNativeProcesses();
            if (suts != null){
                String desc;
                for (SUT theSUT : suts){
                    desc = theSUT.get(Tags.Desc, null);
                    if (desc != null && desc.contains(processName)){
                        System.out.println("SUT with Process Name -" + processName + "- DETECTED!");
                        return theSUT;
                    }
                }
            }
        } while (System.currentTimeMillis() - now < maxEngageTime);
        throw new SystemStartException("SUT Process Name not found!: -" + processName + "-");
    }

}
