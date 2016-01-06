package husacct.control.presentation.taskbar;

import husacct.ServiceProvider;
import husacct.common.locale.ILocaleService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DesktopManager;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

public class ToolBarButtonContextMenu extends JPopupMenu{
	private static final long serialVersionUID = 1L;
	
	private Logger logger = Logger.getLogger(ToolBarButtonContextMenu.class);
	
	private JInternalFrame internalFrame;
	
	private JMenuItem maximize;
	private JMenuItem restore;
	private JMenuItem minimize;
	private JMenuItem close;
	
	public ToolBarButtonContextMenu(JInternalFrame internalFrame){
		this.internalFrame = internalFrame;
		addComponents();
		setListeners();
	}
	
	private void addComponents() {
		ILocaleService localeService = ServiceProvider.getInstance().getLocaleService();
		
		maximize = new JMenuItem(localeService.getTranslatedString("Maximize"));
		restore = new JMenuItem(localeService.getTranslatedString("Restore"));
		minimize = new JMenuItem(localeService.getTranslatedString("Minimize"));
		close = new JMenuItem(localeService.getTranslatedString("Close"));
		
		add(maximize);
		add(restore);
		add(minimize);
		add(close);
	}
	
	private void setListeners() {
		maximize.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					internalFrame.setMaximum(true);
				} catch (Exception e) {
					logger.debug(e.getMessage());
				}
				activateFrame(internalFrame);
			}
		});
		
		restore.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					internalFrame.setMaximum(false);
				} catch (Exception e) {
					logger.debug(e.getMessage());
				}
				activateFrame(internalFrame);
			}
		});
		
		minimize.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					internalFrame.setIcon(true);
				} catch (Exception e) {
					logger.debug(e.getMessage());
				}
				deactivateFrame(internalFrame);
			}
		});
		
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				internalFrame.dispose();
			}
		});
	}
	
	private void activateFrame(JInternalFrame internalFrame){
		try {
			internalFrame.setVisible(true);
			internalFrame.setIcon(false);
			internalFrame.toFront();
			internalFrame.setSelected(true);
			DesktopManager manager = internalFrame.getDesktopPane().getDesktopManager();
			manager.activateFrame(internalFrame);
		} catch (Exception event) {
			logger.debug(event.getMessage());
		}
	}
	
	private void deactivateFrame(JInternalFrame internalFrame){
		try {
			internalFrame.setVisible(false);
			internalFrame.setSelected(false);
			DesktopManager manager = internalFrame.getDesktopPane().getDesktopManager();
			manager.deactivateFrame(internalFrame);
		} catch (Exception event) {
			logger.debug(event.getMessage());
		}		
	}
}