package husacct.graphics.presentation.dialogs;

import husacct.ServiceProvider;
import husacct.control.IControlService;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

public class GraphicsOptionsDialog extends JDialog {
	private static final long serialVersionUID = 4794939901459687332L;

	protected IControlService controlService;
	protected Logger logger = Logger.getLogger(GraphicsOptionsDialog.class);
	
	private GridLayout layout = new GridLayout(0,2);
	private JPanel settingsPanel, actionsPanel, zoomPanel;
	
	private int menuItemMaxHeight = 45;

	private JButton zoomInButton, zoomOutButton, refreshButton, exportToImageButton;
	private JCheckBox showDependenciesOptionMenu, showViolationsOptionMenu, contextUpdatesOptionMenu;
	private JComboBox layoutStrategyOptions;
	private JSlider zoomSlider;
	
	private int width, height;
	
	public GraphicsOptionsDialog(){
		super();
		width = 500;
		height = 200;
		
		controlService = ServiceProvider.getInstance().getControlService();
		initGUI();
	}
	
	public void showDialog(){
//		setResizable(true);
		setSize(width, height);
		setModal(true);
		setVisible(true);
		ServiceProvider.getInstance().getControlService().centerDialog(this);
	}
	
	public void initGUI() {
		actionsPanel = new JPanel();
		settingsPanel = new JPanel();
		zoomPanel = new JPanel();
		
		zoomInButton = new JButton();
		actionsPanel.add(zoomInButton);
		
		zoomOutButton = new JButton();
		actionsPanel.add(zoomOutButton);
		
		refreshButton = new JButton();
		actionsPanel.add(refreshButton);
		
		exportToImageButton = new JButton();
		actionsPanel.add(exportToImageButton);
		
		add(actionsPanel, BorderLayout.NORTH);
		
		settingsPanel.setSize(width,height);
		settingsPanel.setLayout(layout);
		
		settingsPanel.add(new JLabel("Show dependencies"));
		showDependenciesOptionMenu = new JCheckBox();
		showDependenciesOptionMenu.setSize(40, menuItemMaxHeight);
		settingsPanel.add(showDependenciesOptionMenu);
		
		settingsPanel.add(new JLabel("Show violations"));
		showViolationsOptionMenu = new JCheckBox();
		showViolationsOptionMenu.setSize(40, menuItemMaxHeight);
		settingsPanel.add(showViolationsOptionMenu);
		
		settingsPanel.add(new JLabel("Line context updates"));
		contextUpdatesOptionMenu = new JCheckBox();
		contextUpdatesOptionMenu.setSize(40, menuItemMaxHeight);
		settingsPanel.add(contextUpdatesOptionMenu);
		
		settingsPanel.add(new JLabel("Layout strategy"));
		layoutStrategyOptions = new JComboBox();
		settingsPanel.add(layoutStrategyOptions);
		
		layout.layoutContainer(settingsPanel);
		add(settingsPanel, BorderLayout.CENTER);
		
		zoomPanel.add(new JLabel("Zoom"));
		zoomSlider = new JSlider(25, 175, 100);
		zoomSlider.setSize(50, width);
		zoomPanel.add(zoomSlider);
		
		add(zoomPanel, BorderLayout.SOUTH);
	}
	
	public void setLocale(HashMap<String, String> menuBarLocale) {
		try {
			zoomOutButton.setText(menuBarLocale.get("LevelUp"));
			refreshButton.setText(menuBarLocale.get("Refresh"));
			exportToImageButton.setText(menuBarLocale.get("ExportToImage"));
			showDependenciesOptionMenu.setText(menuBarLocale.get("ShowDependencies"));
			showViolationsOptionMenu.setText(menuBarLocale.get("ShowViolations"));
			contextUpdatesOptionMenu.setText(menuBarLocale.get("LineContextUpdates"));
		} catch (NullPointerException e) {
			logger.warn("Locale is not set properly.");
		}
	}

	public void setLevelUpAction(ActionListener listener) {
		zoomOutButton.addActionListener(listener);
	}

	public void setRefreshAction(ActionListener listener) {
		refreshButton.addActionListener(listener);
	}

	public void setExportToImageAction(ActionListener listener) {
		exportToImageButton.addActionListener(listener);
	}

	public void setToggleContextUpdatesAction(ActionListener listener) {
		contextUpdatesOptionMenu.addActionListener(listener);
	}
	
	public void setLayoutStrategyAction(ActionListener listener) {
		layoutStrategyOptions.addActionListener(listener);
	}

	public void setContextUpdatesToggle(boolean setting) {
		contextUpdatesOptionMenu.setSelected(setting);
	}

	public void setToggleDependenciesAction(ActionListener listener) {
		showDependenciesOptionMenu.addActionListener(listener);
	}

	public void setToggleViolationsAction(ActionListener listener) {
		showViolationsOptionMenu.addActionListener(listener);
	}

	public void setZoomChangeListener(ChangeListener listener) {
		zoomSlider.addChangeListener(listener);
	}

	public void setLayoutStrategyItems(String[] layoutStrategyItems) {
		layoutStrategyOptions.removeAllItems();
		for (String item : layoutStrategyItems) {
			layoutStrategyOptions.addItem(item);
		}
	}

	public void setSelectedLayoutStrategyItem(String item) {
		layoutStrategyOptions.setSelectedItem(item);
	}
	
	public String getSelectedLayoutStrategyItem() {
		return (String) layoutStrategyOptions.getSelectedItem();
	}
	
	public void setViolationToggle(boolean setting) {
		showViolationsOptionMenu.setSelected(setting);
	}

	public void setDependencyToggle(boolean setting) {
		showDependenciesOptionMenu.setSelected(setting);
	}
	
}
