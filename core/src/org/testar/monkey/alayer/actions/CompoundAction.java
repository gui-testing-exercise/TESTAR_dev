/***************************************************************************************************
*
* Copyright (c) 2013, 2014, 2015, 2016, 2017, 2019 Universitat Politecnica de Valencia - www.upv.es
* Copyright (c) 2019 Open Universiteit - www.ou.nl
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


/**
 *  @author Sebastian Bauersfeld
 */
package org.testar.monkey.alayer.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.testar.monkey.Assert;
import org.testar.monkey.Util;
import org.testar.monkey.alayer.Action;
import org.testar.monkey.alayer.Role;
import org.testar.monkey.alayer.SUT;
import org.testar.monkey.alayer.State;
import org.testar.monkey.alayer.TaggableBase;
import org.testar.monkey.alayer.Tags;
import org.testar.monkey.alayer.Widget;

/**
 * An action that is composed of several other actions.
 */
public final class CompoundAction extends TaggableBase implements Action {

	private static final long serialVersionUID = -5836624942752268573L;
	private final List<Action> actions;
	private final List<Double> relativeDurations;
	
	public static final class Builder{
		private List<Double> relativeDurations = Util.newArrayList();
		private List<Action> actions = Util.newArrayList();
		double durationSum = 0.0;
		
		public Builder add(Action a, double relativeDuration){
			Assert.notNull(a);
			Assert.isTrue(relativeDuration >= 0);
			relativeDurations.add(relativeDuration);
			actions.add(a);
			durationSum += relativeDuration;
			return this;
		}
				
		public CompoundAction build(){
			Assert.isTrue(durationSum > 0.0, "Sum of durations needs to be larger than 0!");

			// normalize
			for(int i = 0; i < relativeDurations.size(); i++)
				relativeDurations.set(i, relativeDurations.get(i) / durationSum);
			return new CompoundAction(this);
		}

		public CompoundAction build(Widget originWidget){
		    CompoundAction compoundAction = build();
		    compoundAction.set(Tags.OriginWidget, originWidget);
		    return compoundAction;
		}
	}
	
	private CompoundAction(Builder b){
		this.actions = b.actions;
		this.relativeDurations = b.relativeDurations;
		this.set(Tags.Role, ActionRoles.CompoundAction);
		
		int index = 1;
		StringBuilder sbDesc = new StringBuilder("");
		for(Action a : actions)
			sbDesc.append("[(Action n" + (index++) + ") " + a.get(Tags.Desc,"") + "] ");
		this.set(Tags.Desc, sbDesc.toString());
	}
	
	public CompoundAction(Action... actions){
		Assert.notNull((Object)actions);
		this.actions = Arrays.asList(actions);
		this.relativeDurations = Util.newArrayList();
		
		for(int i = 0; i < actions.length; i++)
			relativeDurations.add(1.0 / actions.length);
	}
	
	public List<Action> getActions() {
		return Collections.unmodifiableList(actions);
	}
			
	public void run(SUT system, State state, double duration) {		
		for(int i = 0; i < actions.size(); i++)
			actions.get(i).run(system, state, relativeDurations.get(i) * duration);
	}
		
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Compound Action =");
		for (Action a : actions)
			sb.append(Util.lineSep()).append(a.toString());
		return sb.toString();
	}
	
	@Override
	public String toString(Role... discardParameters) {
		StringBuilder sb = new StringBuilder();
		sb.append("Compound Action =");
		for (Action a : actions)
			sb.append(Util.lineSep()).append(a.toString(discardParameters));
		return sb.toString();
	}	

	@Override
	public String toShortString() {
		StringBuilder sb = new StringBuilder();
		Role r = get(Tags.Role, null);
		if (r != null)
			sb.append(r.toString());
		else
			sb.append("UNDEF");
		HashSet<String> parameters = new HashSet<>();
		for (Action a : actions)
			parameters.add(a.toParametersString());
		for (String p : parameters)
			sb.append(p);
		return sb.toString();
	}

	@Override
	public String toParametersString() {
		StringBuilder params = new StringBuilder("");
		for (Action a : actions)
			params.append(a.toParametersString());
		return params.toString();
	}
}
