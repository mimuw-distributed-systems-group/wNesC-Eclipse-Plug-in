package pl.edu.mimuw.nesc.plugin.projects.pages;

import pl.edu.mimuw.nesc.plugin.projects.util.CommentGroupWrapper;
import pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.osgi.service.prefs.BackingStoreException;

import static org.eclipse.swt.SWT.*;

/**
 * Project specific settings related to the comments templates.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */
public final class NescProjectCommentsTemplatesSettingsPage extends NescPropertyPage {
    /**
     * Various texts that appear in this page.
     */
    private static final String TITLE_HEAD_GROUP = "Head comment";
    private static final String TITLE_ENTITY_GROUP = "Interface/component comment";
    private static final String ERRMSG_HEAD_COMMENT = "Entered head comment is invalid.";
    private static final String ERRMSG_ENTITY_COMMENT = "Entered interface and component comment is invalid.";

    /**
     * Wrappers of the controls that allow to set the comment settings.
     */
    private CommentGroupWrapper headCommentGroup;
    private CommentGroupWrapper entityCommentGroup;

    @Override
    protected Control createContents(Composite parent) {
        // Create the main control
        final Composite pageComposite = new Composite(parent, NONE);
        final GridLayout pageLayout = new GridLayout();
        pageLayout.marginHeight = pageLayout.marginWidth = 0;
        pageComposite.setLayout(pageLayout);

        // Create the groups
        headCommentGroup = new CommentGroupWrapper(pageComposite, TITLE_HEAD_GROUP,
                new GridData(FILL, FILL, true, true));
        entityCommentGroup = new CommentGroupWrapper(pageComposite, TITLE_ENTITY_GROUP,
                new GridData(FILL, FILL, true, true));

        // Final actions
        initializeValues();
        registerErrorListeners();

        return pageComposite;
    }

    /**
     * Registers listeners that listen for changes in the data entered by the
     * user to check their validity.
     */
    private void registerErrorListeners() {
        final CommentGroupWrapper.Listener listener = new CommentGroupWrapper.Listener() {
            @Override
            public void change() {
                updateErrorStatus();
            }
        };

        headCommentGroup.addListener(listener);
        entityCommentGroup.addListener(listener);
    }

    /**
     * Updates error message and validity page depending on values in the
     * wrappers.
     */
    private void updateErrorStatus() {
        if (!headCommentGroup.validate()) {
            setErrorMessage(ERRMSG_HEAD_COMMENT);
            setValid(false);
            return;
        }

        if (!entityCommentGroup.validate()) {
            setErrorMessage(ERRMSG_ENTITY_COMMENT);
            setValid(false);
            return;
        }

        setErrorMessage(null);
        setValid(true);
    }

    @Override
    protected void initializeDefaults() {
        headCommentGroup.setInherit();
        entityCommentGroup.setInherit();
    }

    @Override
    protected void initializeValues() {
       final IProject project = getProject();
       if (project == null) {
           setErrorMessage("Cannot find the project to load the settings from.");
           return;
       }

       headCommentGroup.loadFromProjectPreferences(project, NescProjectPreferences.COMMENT_HEAD);
       entityCommentGroup.loadFromProjectPreferences(project, NescProjectPreferences.COMMENT_ENTITY);
    }

    @Override
    protected void storeValues() {
        final IProject project = getProject();
        if (project == null) {
            setErrorMessage("Cannot find the project to save the settings in.");
            return;
        }

        try {
            headCommentGroup.saveInProjectPreferences(project, NescProjectPreferences.COMMENT_HEAD);
            entityCommentGroup.saveInProjectPreferences(project, NescProjectPreferences.COMMENT_ENTITY);
        } catch (BackingStoreException e) {
            setErrorMessage("Cannot save the settings in the project preferences.");
        }
    }
}
