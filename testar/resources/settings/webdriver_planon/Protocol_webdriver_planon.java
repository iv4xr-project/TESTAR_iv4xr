/*
 * *
 * COPYRIGHT (2017):                                                                     *
 * Universitat Politecnica de Valencia                                                   *
 * Camino de Vera, s/n                                                                   *
 * 46022 Valencia, Spain                                                                 *
 * www.upv.es                                                                            *
 *                                                                                       *
 * D I S C L A I M E R:                                                                  *
 * This software has been developed by the Universitat Politecnica de Valencia (UPV)     *
 * in the context of the TESTAR Proof of Concept project:                                *
 * "UPV, Programa de Prueba de Concepto 2014, SP20141402"                                *
 * This software is distributed FREE of charge under the TESTAR license, as an open      *
 * source project under the BSD3 licence (http://opensource.org/licenses/BSD-3-Clause)   *                                                                                        *
 * *
 *
 */

/*
 *  @author (base) Sebastian Bauersfeld
 *  @author Govert Buijs
 */

import es.upv.staq.testar.NativeLinker;
import es.upv.staq.testar.protocols.ClickFilterLayerProtocol;
import org.fruit.Pair;
import org.fruit.alayer.*;
import org.fruit.alayer.actions.*;
import org.fruit.alayer.exceptions.ActionBuildException;
import org.fruit.alayer.exceptions.StateBuildException;
import org.fruit.alayer.exceptions.SystemStartException;
import org.fruit.alayer.webdriver.*;
import org.fruit.alayer.webdriver.enums.WdRoles;
import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Settings;

import java.util.*;

import static org.fruit.alayer.Tags.Blocked;
import static org.fruit.alayer.Tags.Enabled;
import static org.fruit.alayer.webdriver.Constants.scrollArrowSize;
import static org.fruit.alayer.webdriver.Constants.scrollThick;


public class Protocol_webdriver_planon extends ClickFilterLayerProtocol {
  // Classes that are deemed clickable by the web framework
  private static List<String> clickableClasses = Arrays.asList(
      "planon-logo",
      "pn-reference-date-tooltip",
      "pn-reference-date-today",
      "pn-truncate-text",
      "profile-photo",
      "k-image",
      "k-item",
      "k-link",
      "closebutton",
      "storeItem",
      "pss_action_label"
      );
  // Img alts that are deemed clickable by the web framework
  private static List<String> clickableImgAlt = Arrays.asList(
      "editmodebutton",
      "closeeditmodebutton");

  // Disallow links and pages with these extensions
  // Set to null to ignore this feature
  private static List<String> deniedExtensions = Arrays.asList(
      "pdf", "jpg", "png");

  // Define a whitelist of allowed domains for links and pages
  // An empty list will be filled with the domain from the sut connector
  // Set to null to ignore this feature
  private static List<String> domainsAllowed = null;

  // If true, follow links opened in new tabs
  // If false, stay with the original (ignore links opened in new tabs)
  private static boolean followLinks = true;

  // URL + form name, username input id + value, password input id + value
  // Set login to null to disable this feature
  private static Pair<String, String> login = Pair.from(
      "kaheuv-prod.pdit.cloud", "loginForm");
  private static Pair<String, String> username = Pair.from("j_username", "");
  private static Pair<String, String> password = Pair.from("j_password", "");

  // List of atributes to identify and close policy popups
  // Set to null to disable this feature
  private static Map<String, String> policyAttributes =
      new HashMap<String, String>() {{
        put("id", "_cookieDisplay_WAR_corpcookieportlet_okButton");
      }};

  /**
   * Called once during the life time of TESTAR
   * This method can be used to perform initial setup work
   *
   * @param settings the current TESTAR settings as specified by the user.
   */
  protected void initialize(Settings settings) {
    NativeLinker.addWdDriverOS();
    super.initialize(settings);
    ensureDomainsAllowed();

    // Propagate followLinks setting
    WdDriver.followLinks = followLinks;
  }

  /**
   * This method is called when TESTAR starts the System Under Test (SUT). The method should
   * take care of
   * 1) starting the SUT (you can use TESTAR's settings obtainable from <code>settings()</code> to find
   * out what executable to run)
   * 2) bringing the system into a specific start state which is identical on each start (e.g. one has to delete or restore
   * the SUT's configuratio files etc.)
   * 3) waiting until the system is fully loaded and ready to be tested (with large systems, you might have to wait several
   * seconds until they have finished loading)
   *
   * @return a started SUT, ready to be tested.
   */
  protected SUT startSystem() throws SystemStartException {
    SUT sut = super.startSystem();

    // See remarks in WdMouse
    mouse = sut.get(Tags.StandardMouse);

    // Override ProtocolUtil to allow WebDriver screenshots
    protocolUtil = new WdProtocolUtil(sut);

    return sut;
  }

  /**
   * This method is invoked each time the TESTAR starts the SUT to generate a new sequence.
   * This can be used for example for bypassing a login screen by filling the username and password
   * or bringing the system into a specific start state which is identical on each start (e.g. one has to delete or restore
   * the SUT's configuration files etc.)
   */
  protected void beginSequence(SUT system, State state) {
    super.beginSequence(system, state);
  }

  /**
   * This method is called when TESTAR requests the state of the SUT.
   * Here you can add additional information to the SUT's state or write your
   * own state fetching routine. The state should have attached an oracle
   * (TagName: <code>Tags.OracleVerdict</code>) which describes whether the
   * state is erroneous and if so why.
   *
   * @return the current state of the SUT with attached oracle.
   */
  protected State getState(SUT system) throws StateBuildException {
    State state = super.getState(system);

    return state;
  }

  /**
   * This is a helper method used by the default implementation of <code>buildState()</code>
   * It examines the SUT's current state and returns an oracle verdict.
   *
   * @return oracle verdict, which determines whether the state is erroneous and why.
   */
  protected Verdict getVerdict(State state) {

    Verdict verdict = super.getVerdict(state); // by urueda
    // system crashes, non-responsiveness and suspicious titles automatically detected!

    //-----------------------------------------------------------------------------
    // MORE SOPHISTICATED ORACLES CAN BE PROGRAMMED HERE (the sky is the limit ;-)
    //-----------------------------------------------------------------------------

    // ... YOU MAY WANT TO CHECK YOUR CUSTOM ORACLES HERE ...

    return verdict;
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
  protected Set<Action> deriveActions(SUT system, State state)
      throws ActionBuildException {
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
      addSlidingActions(actions, ac, scrollArrowSize, scrollThick, widget, state);

      // If the element is blocked, Testar can't click on or type in the widget
      if (widget.get(Blocked, false)) {
        continue;
      }

      // type into text boxes
      if (isAtBrowserCanvas(widget) && (whiteListed(widget) || isTypeable(widget))) {
        actions.add(ac.clickTypeInto(widget, this.getRandomText(widget),true));
      }

      // left clicks, but ignore links outside domain
      if (isAtBrowserCanvas(widget) && (whiteListed(widget) || isClickable(widget))) {
        if (!isLinkDenied(widget)) {
          actions.add(ac.leftClickAt(widget));
        }
      }
    }

    return actions;
  }

  /*
   * Check the state if we need to force an action
   */
  private Set<Action> detectForcedActions(State state, StdActionCompiler ac) {
    Set<Action> actions = detectForcedDeniedUrl();
    if (actions != null && actions.size() > 0) {
      return actions;
    }

    actions = detectForcedLogin(state);
    if (actions != null && actions.size() > 0) {
      return actions;
    }

    actions = detectForcedPopupClick(state, ac);
    if (actions != null && actions.size() > 0) {
      return actions;
    }

    return null;
  }

  /*
   * Detect and perform login if defined
   */
  private Set<Action> detectForcedLogin(State state) {
    if (login == null || username == null || password == null) {
      return null;
    }

    // Check if the current page is a login page
    String currentUrl = WdDriver.getCurrentUrl();
    if (currentUrl.startsWith(login.left())) {
      CompoundAction.Builder builder = new CompoundAction.Builder();
      // Set username and password
      for (Widget widget : state) {
        WdWidget wdWidget = (WdWidget) widget;
        if (username.left().equals(wdWidget.getAttribute("id"))) {
          builder.add(new WdAttributeAction(
              username.left(), "value", username.right()), 1);
        }
        else if (password.left().equals(wdWidget.getAttribute("id"))) {
          builder.add(new WdAttributeAction(
              password.left(), "value", password.right()), 1);
        }
      }
      // Submit form
      builder.add(new WdSubmitAction(login.right()), 1);
      return new HashSet<>(Collections.singletonList(builder.build()));
    }

    return null;
  }

  /*
   * Force closing of Policies Popup
   */
  private Set<Action> detectForcedPopupClick(State state,
                                             StdActionCompiler ac) {
    if (policyAttributes == null || policyAttributes.size() == 0) {
      return null;
    }

    for (Widget widget : state) {
      if (!widget.get(Enabled, true) || widget.get(Blocked, false)) {
        continue;
      }

      WdElement element = ((WdWidget) widget).element;
      boolean isPopup = true;
      for (Map.Entry<String, String> entry : policyAttributes.entrySet()) {
        String attribute = element.attributeMap.get(entry.getKey());
        isPopup &= entry.getValue().equals(attribute);
      }
      if (isPopup) {
        return new HashSet<>(Collections.singletonList(ac.leftClickAt(widget)));
      }
    }

    return null;
  }

  /*
   * Force back action due to disallowed domain or extension
   */
  private Set<Action> detectForcedDeniedUrl() {
    String currentUrl = WdDriver.getCurrentUrl();

    // Don't get caught in PDFs etc. and non-whitelisted domains
    if (isUrlDenied(currentUrl) || isExtensionDenied(currentUrl)) {
      // If opened in new tab, close it
      if (WdDriver.getWindowHandles().size() > 1) {
        return new HashSet<>(Collections.singletonList(new WdCloseTabAction()));
      }
      // Single tab, go back to previous page
      else {
        return new HashSet<>(Collections.singletonList(new WdHistoryBackAction()));
      }
    }

    return null;
  }

  /*
   * Check if the current address has a denied extension (PDF etc.)
   */
  private boolean isExtensionDenied(String currentUrl) {
    // If the current page doesn't have an extension, always allow
    if (!currentUrl.contains(".")) {
      return false;
    }

    if (deniedExtensions == null || deniedExtensions.size() == 0) {
      return false;
    }

    // Deny if the extension is in the list
    String ext = currentUrl.substring(currentUrl.lastIndexOf(".") + 1);
    ext = ext.replace("/", "").toLowerCase();
    return deniedExtensions.contains(ext);
  }

  /*
   * Check if the URL is denied
   */
  private boolean isUrlDenied(String currentUrl) {
    if (currentUrl.startsWith("mailto:")) {
      return true;
    }

    // Always allow local file
    if (currentUrl.startsWith("file:///")) {
      return false;
    }

    // User wants to allow all
    if (domainsAllowed == null) {
      return false;
    }

    // Only allow pre-approved domains
    String domain = getDomain(currentUrl);
    return !domainsAllowed.contains(domain);
  }

  /*
   * Check if the widget has a denied URL as hyperlink
   */
  private boolean isLinkDenied(Widget widget) {
    String linkUrl = widget.get(Tags.ValuePattern, "");

    // Not a link or local file, allow
    if (linkUrl == null || linkUrl.startsWith("file:///")) {
      return false;
    }

    // Mail link, deny
    if (linkUrl.startsWith("mailto:")) {
      return true;
    }

    // Not a web link (or link to the same domain), allow
    if (!(linkUrl.startsWith("https://") || linkUrl.startsWith("http://"))) {
      return false;
    }

    // User wants to allow all
    if (domainsAllowed == null) {
      return false;
    }

    // Only allow pre-approved domains if
    String domain = getDomain(linkUrl);
    return !domainsAllowed.contains(domain);
  }

  /*
   * Get the domain from a full URL
   */
  private String getDomain(String url) {
    if (url == null) {
      return null;
    }

    // When serving from file, 'domain' is filesystem
    if (url.startsWith("file://")) {
      return "file://";
    }

    url = url.replace("https://", "").replace("http://", "").replace("file://", "");
    return (url.split("/")[0]).split("\\?")[0];
  }

  /*
   * If domainsAllowed not set, allow the domain from the SUT Connector
   */
  private void ensureDomainsAllowed() {
    // Not required or already defined
    if (domainsAllowed == null || domainsAllowed.size() > 0) {
      return;
    }

    String[] parts = settings().get(ConfigTags.SUTConnectorValue).split(" ");
    String url = parts[parts.length - 1].replace("\"", "");
    domainsAllowed = Arrays.asList(getDomain(url));
  }

  /*
   * We need to check if click position is within the canvas
   */
  private boolean isAtBrowserCanvas(Widget widget) {
    Shape shape = widget.get(Tags.Shape, null);
    if (shape == null) {
      return false;
    }

    // Widget must be completely visible on viewport for screenshots
    return shape.x() >= 0 && shape.x() + shape.width() < CanvasDimensions.getCanvasWidth() &&
           shape.y() >= 0 && shape.y() + shape.height() < CanvasDimensions.getInnerHeight();
  }

  protected boolean isClickable(Widget widget) {
    Role role = widget.get(Tags.Role, Roles.Widget);
    if (Role.isOneOf(role, NativeLinker.getNativeClickableRoles())) {
      // Input type are special...
      if (role.equals(WdRoles.WdINPUT)) {
        String type = ((WdWidget) widget).element.type;
        return WdRoles.clickableInputTypes().contains(type);
      }
      return true;
    }

    WdElement element = ((WdWidget) widget).element;
    if (element.isClickable) {
      return true;
    }

    String alt = element.attributeMap.get("alt");
    if (clickableImgAlt.contains(alt)) {
      return true;
    }

    Set<String> clickSet = new HashSet<>(clickableClasses);
    clickSet.retainAll(element.cssClasses);
    return clickSet.size() > 0;
  }

  protected boolean isTypeable(Widget widget) {
    Role role = widget.get(Tags.Role, Roles.Widget);
    if (Role.isOneOf(role, NativeLinker.getNativeTypeableRoles())) {
      // Input type are special...
      if (role.equals(WdRoles.WdINPUT)) {
        String type = ((WdWidget) widget).element.type;
        return WdRoles.typeableInputTypes().contains(type);
      }
      return true;
    }

    return false;
  }

  /**
   * Select one of the possible actions (e.g. at random)
   *
   * @param state   the SUT's current state
   * @param actions the set of available actions as computed by <code>buildActionsSet()</code>
   * @return the selected action (non-null!)
   */
  protected Action selectAction(State state, Set<Action> actions) {
    return super.selectAction(state, actions);
  }

  /**
   * Execute the selected action.
   *
   * @param system the SUT
   * @param state  the SUT's current state
   * @param action the action to execute
   * @return whether or not the execution succeeded
   */
  protected boolean executeAction(SUT system, State state, Action action) {
    return super.executeAction(system, state, action);
  }

  /**
   * TESTAR uses this method to determine when to stop the generation of actions for the
   * current sequence. You could stop the sequence's generation after a given amount of executed
   * actions or after a specific time etc.
   *
   * @return if <code>true</code> continue generation, else stop
   */
  protected boolean moreActions(State state) {
    return super.moreActions(state);
  }

  /**
   * This method is invoked each time after TESTAR finished the generation of a sequence.
   */
  protected void finishSequence() {
    super.finishSequence();
  }

  /**
   * TESTAR uses this method to determine when to stop the entire test.
   * You could stop the test after a given amount of generated sequences or
   * after a specific time etc.
   *
   * @return if <code>true</code> continue test, else stop
   */
  protected boolean moreSequences() {
    return super.moreSequences();
  }
}
