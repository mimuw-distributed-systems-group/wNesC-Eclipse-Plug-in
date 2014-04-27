package pl.edu.mimuw.nesc.plugin.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class NescWizardNewProjectMainConfiguartionPage extends WizardPage {
  private Text mainConfiguration;
  private Button tinyOsProject;
  private Text tinyOsPlatform;
  private Composite container;
  
  private Listener pageModifyListener = new Listener() {
      public void handleEvent(Event e) {
        boolean valid = validatePage();
        setPageComplete(valid);      
      }
  };

  public NescWizardNewProjectMainConfiguartionPage(String pageName) {
    super(pageName);
    setTitle(pageName);
  }

  @Override
  public void createControl(Composite parent) {
    container = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    container.setLayout(layout);
    layout.numColumns = 2;
    
    Label configurationLabel = new Label(container, SWT.NONE);
    configurationLabel.setText("Main configuration:");
    mainConfiguration = new Text(container, SWT.BORDER | SWT.SINGLE);
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    mainConfiguration.setLayoutData(data);
    mainConfiguration.setText("");
    mainConfiguration.addListener(SWT.Modify, pageModifyListener);
    
    Label labelTinyOS = new Label(container, SWT.NONE);
    labelTinyOS.setText("TinyOS project:");
    tinyOsProject = new Button(container, SWT.CHECK);
    tinyOsProject.setSelection(false);
    tinyOsProject.addSelectionListener(new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (((Button)e.widget).getSelection()) {
				if (tinyOsPlatform != null) {
					tinyOsPlatform.setEnabled(true);
				}
			} else {
				if (tinyOsPlatform != null) {
					tinyOsPlatform.setEnabled(false);
				}
			}
			
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
    	
    });
    tinyOsProject.addListener(SWT.Modify, pageModifyListener);
    
    Label labelPlatform = new Label(container, SWT.NONE);
    labelPlatform.setText("Platform:");
    tinyOsPlatform = new Text(container, SWT.BORDER | SWT.SINGLE);
    data = new GridData(GridData.FILL_HORIZONTAL);
    tinyOsPlatform.setLayoutData(data);
    tinyOsPlatform.setText("");
    tinyOsPlatform.setEnabled(false);
    tinyOsPlatform.addListener(SWT.Modify, pageModifyListener);
    
    // Required to avoid an error in the system
    setControl(container);
    setPageComplete(validatePage());
  }
  
  public boolean validatePage() {
	  /**
	   * Add support for tinyOs projects
	   * */
	  boolean isValid = false;
	  String configuration = mainConfiguration.getText();
	  if (!configuration.isEmpty()) {
		  return true;
	  }
	  return isValid;
  }

   public String getMainConfiguration() {
      if (mainConfiguration != null) {
         return mainConfiguration.getText();
      }
      return "";
   }
   
   public boolean getTinyOsProject() {
	   if (tinyOsProject != null) {
		   return tinyOsProject.getSelection();
	   }
	   return false;
   }
   
   public String getTinyOsPlatform() {
	   if (tinyOsPlatform != null) {
		   return tinyOsPlatform.getText();
	   }
	   return "";
   }
}