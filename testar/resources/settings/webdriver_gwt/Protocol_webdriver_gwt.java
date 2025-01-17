/**
 * Copyright (c) 2018 - 2021 Open Universiteit - www.ou.nl
 * Copyright (c) 2019 - 2021 Universitat Politecnica de Valencia - www.upv.es
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
 *
 */

import org.testar.monkey.alayer.*;
import org.testar.monkey.alayer.actions.AnnotatingActionCompiler;
import org.testar.monkey.alayer.actions.StdActionCompiler;
import org.testar.monkey.alayer.exceptions.ActionBuildException;
import org.testar.monkey.alayer.webdriver.WdElement;
import org.testar.monkey.alayer.webdriver.WdWidget;
import org.testar.monkey.alayer.webdriver.enums.WdRoles;
import org.testar.monkey.alayer.webdriver.enums.WdTags;
import org.testar.plugin.NativeLinker;
import org.testar.monkey.Settings;
import org.testar.protocols.WebdriverProtocol;

import java.util.*;

import static org.testar.monkey.alayer.Tags.Blocked;
import static org.testar.monkey.alayer.Tags.Enabled;


public class Protocol_webdriver_gwt extends WebdriverProtocol {

	/**
	 * Called once during the life time of TESTAR
	 * This method can be used to perform initial setup work
	 *
	 * @param settings the current TESTAR settings as specified by the user.
	 */
	@Override
	protected void initialize(Settings settings) {
		super.initialize(settings);

		/**
		 * Classes that are deemed clickable by the web framework
		 */
		// clickableClasses are initialized in the WebdriverProtocol with the test.settings file
		//clickableClasses = settings.get(ConfigTags.ClickableClasses);

		// User can also use this Java protocol to customize them (need code re-compilation)
		/*clickableClasses = Arrays.asList(
				// Dropdown op top right
				"selectItemLiteText",
				// Menu items on the left
				"etreeCell", "etreeCellSelected", "etreeCellSelectedOver",
				// Checkboxes
				"checkboxFalse", "checkboxFalseOver", "checkboxTrue", "checkboxTrueOver",
				// Tiles
				"showcaseTileIcon",
				// Scrolling stuff
				"vScrollStart", "vScrollEnd"
				);
		 */

		/**
		 * Disallow links and pages with these extensions
		 * Set to null to ignore this feature
		 */
		// deniedExtensions are initialized in the WebdriverProtocol with the test.settings file
		//deniedExtensions = settings.get(ConfigTags.DeniedExtensions);

		// User can also use this Java protocol to customize them (need code re-compilation)
		//deniedExtensions = Arrays.asList("pdf", "jpg", "png", "jsp");

		/**
		 * Define a whitelist of allowed domains for links and pages
		 * An empty list will be filled with the domain from the sut connector
		 * Set to null to ignore this feature
		 */
		// domainsAllowed are initialized in the WebdriverProtocol with the test.settings file 
		//domainsAllowed = settings.get(ConfigTags.DomainsAllowed);

		// User can also use this Java protocol to customize them (need code re-compilation)
		//domainsAllowed = Arrays.asList("www.smartclient.com");

		/**
		 * List of attributes to identify and close policy popups
		 * Set to null to disable this feature
		 */
		// Currently only at Java protocol level (need code re-compilation)
		policyAttributes = new HashMap<String, String>() {{
		    put("class", "iAgreeButton");
		}};
	}

	/**
	 * This method is used by TESTAR to determine the set of currently available actions.
	 * You can use the SUT's current state, analyze the widgets and their properties to create
	 * a set of sensible actions, such as: "Click every Button which is enabled" etc.
	 * The return value is supposed to be non-null. If the returned set is empty, TESTAR
	 * will stop generation of the current action and continue with the next one.
	 *
	 * @param system the SUT
	 * @param state  the SUT's current state
	 * @return a set of actions
	 */
	@Override
	protected Set<Action> deriveActions(SUT system, State state) throws ActionBuildException {
		// Kill unwanted processes, force SUT to foreground
		Set<Action> actions = super.deriveActions(system, state);

		// create an action compiler, which helps us create actions
		// such as clicks, drag&drop, typing ...
		StdActionCompiler ac = new AnnotatingActionCompiler();

		// Check if forced actions are needed to stay within allowed domains
		Set<Action> forcedActions = detectForcedActions(state, ac);
		if (forcedActions != null && forcedActions.size() > 0) {
			return forcedActions;
		}

		// iterate through all widgets
		for (Widget widget : state) {
			// only consider enabled and non-tabu widgets
			if (!widget.get(Enabled, true) || blackListed(widget)) {
				continue;
			}

			// slides can happen, even though the widget might be blocked
			// addSlidingActions(actions, ac, scrollArrowSize, scrollThick, widget,
			//     state);

			// If the element is blocked, Testar can't click on or type in the widget
			if (widget.get(Blocked, false) && !widget.get(WdTags.WebIsShadow, false)) {
				continue;
			}

			// type into text boxes
			if (isAtBrowserCanvas(widget) && isTypeable(widget) && (whiteListed(widget) || isUnfiltered(widget))) {
				actions.add(ac.clickTypeInto(widget, this.getRandomText(widget), true));
			}

			// left clicks, but ignore links outside domain
			if (isAtBrowserCanvas(widget) && isClickable(widget) && (whiteListed(widget) || isUnfiltered(widget))) {
				if (!isLinkDenied(widget)) {
					actions.add(ac.leftClickAt(widget));
				}
			}
		}

		return actions;
	}

	@Override
	protected boolean isClickable(Widget widget) {
		Role role = widget.get(Tags.Role, Roles.Widget);
		if (Role.isOneOf(role, NativeLinker.getNativeClickableRoles())) {
			/*
      // Input type are special...
      if (role.equals(WdRoles.WdINPUT)) {
        String type = ((WdWidget) widget).element.type;
        return WdRoles.clickableInputTypes().contains(type);
      }
			 */
			return true;
		}

		WdElement element = ((WdWidget) widget).element;
		if (element.isClickable) {
			return true;
		}

		Set<String> clickSet = new HashSet<>(clickableClasses);
		clickSet.retainAll(element.cssClasses);
		return clickSet.size() > 0;
	}

}
