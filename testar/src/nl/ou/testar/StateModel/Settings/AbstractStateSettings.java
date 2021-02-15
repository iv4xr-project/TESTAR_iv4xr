package nl.ou.testar.StateModel.Settings;

import es.upv.staq.testar.ActionManagementTags;
import es.upv.staq.testar.StateManagementTags;
import org.fruit.alayer.Tag;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class AbstractStateSettings extends JDialog {

    private Tag<?>[] allStateTags;
    private Tag<?>[] currentlySelectedStateTags;
    private Tag<?>[] defaultStateTags;
    
    private Tag<?>[] allActionTags;
    private Tag<?>[] currentlySelectedActionTags;
    private Tag<?>[] defaultActionTags;

    private JLabel label1 = new JLabel("Please choose the widget attributes to use in "
    + "creating the abstract state model. Control + Left Click");
    private JLabel label2 = new JLabel("Windows attributes");
    private JLabel label3 = new JLabel("WebDrivers attributes");
    private JLabel label4 = new JLabel("iv4XR State attr");
    
    private JLabel label5 = new JLabel("iv4XR Action attr");

    private JList generalList;
    private JList webdriverList;
    private JList iv4xrStateList;
    
    private JList iv4xrActionList;

    private JButton confirmButton = new JButton("Confirm");
    private JButton resetToDefaultsButton = new JButton("Reset to defaults");

    Window window = SwingUtilities.getWindowAncestor(this);

    public AbstractStateSettings(Tag<?>[] allStateTags, Tag<?>[] currentlySelectedStateTags, Tag<?>[] defaultStateTags,
    		Tag<?>[] allActionTags, Tag<?>[] currentlySelectedActionTags, Tag<?>[] defaultActionTags) {
        // State Tags
    	this.allStateTags = Arrays.stream(allStateTags).sorted(Comparator.comparing(Tag::name)).toArray(Tag<?>[]::new);
        this.currentlySelectedStateTags = currentlySelectedStateTags;
        this.defaultStateTags = defaultStateTags;
        // Action Tags
        this.allActionTags = Arrays.stream(allActionTags).sorted(Comparator.comparing(Tag::name)).toArray(Tag<?>[]::new);
        this.currentlySelectedActionTags = currentlySelectedActionTags;
        this.defaultActionTags = defaultActionTags;
        
        setSize(1000, 600);
        setLayout(null);
        setVisible(true);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // tell the manager to shut down its connection
                super.windowClosing(e);
            }
        });

        init();
    }

    private void init() {
        label1.setBounds(10, 10, 600, 27);
        add(label1);

        /////// GENERAL STATE MANAGEMENT TAGS ////////
        label2.setBounds(10, 50, 250, 27);
        add(label2);

        generalList = new JList(Arrays.stream(allStateTags).filter(tag -> StateManagementTags.getTagGroup(tag).equals(StateManagementTags.Group.General)).toArray()); //data has type Object[]
        generalList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        generalList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        generalList.setVisibleRowCount(-1);

        JScrollPane listScrollerGeneral = new JScrollPane(generalList);
        listScrollerGeneral.setPreferredSize(new Dimension(250, 350));
        listScrollerGeneral.setBounds(10, 80, 250, 350);
        add(listScrollerGeneral);

        ///////// WEB DRIVER STATE MANAGEMENT TAGS /////////
        label3.setBounds(360, 50, 250, 27);
        add(label3);

        webdriverList = new JList(Arrays.stream(allStateTags).filter(tag -> StateManagementTags.getTagGroup(tag).equals(StateManagementTags.Group.WebDriver)).toArray()); //data has type Object[]
        webdriverList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        webdriverList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        webdriverList.setVisibleRowCount(-1);

        JScrollPane listScrollerWebdriver = new JScrollPane(webdriverList);
        listScrollerWebdriver.setPreferredSize(new Dimension(250, 350));
        listScrollerWebdriver.setBounds(360, 80, 250, 350);
        add(listScrollerWebdriver);
        
        ///////// iv4XR STATE MANAGEMENT TAGS /////////
        label4.setBounds(710, 50, 250, 27);
        add(label4);
        
        iv4xrStateList = new JList(Arrays.stream(allStateTags).filter(tag -> StateManagementTags.getTagGroup(tag).equals(StateManagementTags.Group.iv4xr)).toArray()); //data has type Object[]
        iv4xrStateList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        iv4xrStateList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        iv4xrStateList.setVisibleRowCount(-1);

        JScrollPane listScrollerIv4xrState = new JScrollPane(iv4xrStateList);
        listScrollerIv4xrState.setPreferredSize(new Dimension(250, 250));
        listScrollerIv4xrState.setBounds(710, 80, 250, 250);
        add(listScrollerIv4xrState);
        
        ///////// iv4XR ACTION MANAGEMENT TAGS /////////
        label5.setBounds(710, 350, 250, 27);
        add(label5);
        
        iv4xrActionList = new JList(Arrays.stream(allActionTags).filter(tag -> ActionManagementTags.getTagGroup(tag).equals(ActionManagementTags.Group.iv4xrAction)).toArray()); //data has type Object[]
        iv4xrActionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        iv4xrActionList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        iv4xrActionList.setVisibleRowCount(-1);

        JScrollPane listScrollerIv4xrAction = new JScrollPane(iv4xrActionList);
        listScrollerIv4xrAction.setPreferredSize(new Dimension(250, 150));
        listScrollerIv4xrAction.setBounds(710, 400, 250, 150);
        add(listScrollerIv4xrAction);

        // init the selection based on the currently selected state management tags
        populateLists();

        /////////// CONFIRM BUTTON ////////////
        confirmButton.setBounds(10, 480, 250, 27);
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	
            	// State Tags List
            	Stream allListConcatenated = Stream.concat(
            			Stream.concat(generalList.getSelectedValuesList().stream(),
            					webdriverList.getSelectedValuesList().stream()),
            			iv4xrStateList.getSelectedValuesList().stream());
            	
                currentlySelectedStateTags = (Tag<?>[]) allListConcatenated.toArray(Tag<?>[]::new);
                
                // Action Tags List
                Stream actionList = iv4xrActionList.getSelectedValuesList().stream();
                currentlySelectedActionTags = (Tag<?>[]) actionList.toArray(Tag<?>[]::new);

                dispatchEvent(new WindowEvent(window ,WindowEvent.WINDOW_CLOSING));
                dispose();
            }
        });
        add(confirmButton);

        ///////////// DEFAULTS BUTTON //////////////////
        resetToDefaultsButton.setBounds(10, 520, 250, 27);
        resetToDefaultsButton.addActionListener(e -> {
            currentlySelectedStateTags = defaultStateTags;
            currentlySelectedActionTags = defaultActionTags;
            populateLists();
        });
        add(resetToDefaultsButton);
    }

    public Tag<?>[] getCurrentlySelectedStateTags() {
        return currentlySelectedStateTags;
    }
    
    public Tag<?>[] getCurrentlySelectedActionTags() {
        return currentlySelectedActionTags;
    }

    private void populateLists() {
        Set<Tag<?>> tagSet = new HashSet<>(Arrays.asList(currentlySelectedStateTags));
        ListModel<Tag<?>> listModel = generalList.getModel();
        List<Integer> selectedIndices = new ArrayList<>();
        for (int i=0; i < listModel.getSize(); i++) {
            if (tagSet.contains(listModel.getElementAt(i))) {
                selectedIndices.add(i);
            }
        }

        generalList.setSelectedIndices(selectedIndices.stream().mapToInt(i -> i).toArray());

        listModel = webdriverList.getModel();
        selectedIndices = new ArrayList<>();
        for (int i=0; i < listModel.getSize(); i++) {
            if (tagSet.contains(listModel.getElementAt(i))) {
                selectedIndices.add(i);
            }
        }

        webdriverList.setSelectedIndices(selectedIndices.stream().mapToInt(i -> i).toArray());
        
        listModel = iv4xrStateList.getModel();
        selectedIndices = new ArrayList<>();
        for (int i=0; i < listModel.getSize(); i++) {
            if (tagSet.contains(listModel.getElementAt(i))) {
                selectedIndices.add(i);
            }
        }

        iv4xrStateList.setSelectedIndices(selectedIndices.stream().mapToInt(i -> i).toArray());
        
        // Populate Action Tags
        Set<Tag<?>> tagActionSet = new HashSet<>(Arrays.asList(currentlySelectedActionTags));
        ListModel<Tag<?>> actionListModel = iv4xrActionList.getModel();
        List<Integer> selectedActionIndices = new ArrayList<>();
        for (int i=0; i < actionListModel.getSize(); i++) {
            if (tagActionSet.contains(actionListModel.getElementAt(i))) {
            	selectedActionIndices.add(i);
            }
        }

        iv4xrActionList.setSelectedIndices(selectedActionIndices.stream().mapToInt(i -> i).toArray());
    }

}
